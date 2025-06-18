package de.merkeg.openhome.watchdog;

import de.merkeg.openhome.config.AppConfig;
import de.merkeg.openhome.config.DNSConfig;
import de.merkeg.openhome.opnsense.DHCPLeases;
import de.merkeg.openhome.opnsense.OPNSenseLeasesService;
import de.merkeg.openhome.powerdns.api.PowerDNSZoneService;
import de.merkeg.openhome.powerdns.api.RRSet;
import de.merkeg.openhome.powerdns.api.Zone;
import de.merkeg.openhome.powerdns.api.ZonePatchRequest;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class WatchdogService {

  @RestClient
  OPNSenseLeasesService opnsenseLeasesService;

  @RestClient
  PowerDNSZoneService powerDNSZoneService;

  @Inject
  AppConfig appConfig;

  @Scheduled(every = "${app.schedule}")
  public void cronJob(){
    log.debug("Searching for dns changes");
    ChangeMap changeMap = this.createChangemap();
    this.executeDnsChanges(changeMap);
    log.debug("Finished searching for dns changes");
  }

  public ChangeMap createChangemap() {
    ChangeMap changeMap = new ChangeMap();

    DHCPLeases leases = opnsenseLeasesService.getLeases();
    for(DHCPLeases.Lease lease : leases.getRows()) {
      changeMap.getCurrentDhcp().add(Entry.fromLease(lease));
    }

    String serverId = appConfig.powerdns().serverId();
    for(Zone z : powerDNSZoneService.getZones(serverId)) {
      for(RRSet rrSet : powerDNSZoneService.getZone(serverId, z.getId()).getRrsets()) {
        Entry entry = Entry.fromRRSet(rrSet);
        if(entry != null) {
          changeMap.getCurrentDns().add(entry);
        }
      }
    }
    List<String> enabledInterfaces = getEnabledInterfaces();

    for(Entry entry : changeMap.getCurrentDhcp()) {
      if(!enabledInterfaces.contains(entry.getIface())) {
        continue;
      }
      changeMap.getRequiredDhcp().add(entry);
    }

    compareChanges(changeMap);

    for(String zone : getUsedZones()) {
      Set<RRSet> mappings = new HashSet<>();

      changeMap.getUpdate().stream().filter(e -> getInterfaceConfig(e.getIface()).zone().equals(zone)).forEach(e -> {
        DNSConfig.DnsInterface interfaceConfig = getInterfaceConfig(e.getIface());
        mappings.add(e.toRRSet(interfaceConfig.prefix(), interfaceConfig.zone()));
      });

      changeMap.getDeletion().stream().filter(e -> getInterfaceConfig(e.getIface()).zone().equals(zone)).forEach(e -> {
        DNSConfig.DnsInterface interfaceConfig = getInterfaceConfig(e.getIface());
        mappings.add(e.toDeletionRRSet(interfaceConfig.prefix(), interfaceConfig.zone()));
      });

      changeMap.mappings.put(zone, mappings);
    }

    return changeMap;
  }

  public void executeDnsChanges(ChangeMap changeMap) {
    String serverId = appConfig.powerdns().serverId();

    for(String zone : changeMap.mappings.keySet()) {
      Set<RRSet> mappings = changeMap.mappings.get(zone);

      if(mappings.isEmpty()){
        continue;
      }

      log.info("Executing dns changes for zone '{}' - Total changes: {}", zone, mappings.size());
      mappings.forEach(m -> log.debug("{} '{}' -> {} [{}]", m.getType(), m.getName(), m.getRecords().iterator().next().getContent(), m.getComments().iterator().next().getContent()));
      powerDNSZoneService.patchZone(serverId, zone, ZonePatchRequest.builder().rrsets(mappings).build());
    }
  }

  private void compareChanges(ChangeMap changeMap) {

    // Deletion
    for(Entry entry : changeMap.getCurrentDns()) {
      Entry found = changeMap.requiredDhcp.stream()
              .filter(req -> req.getHostname().equals(entry.getHostname()) && req.getIface().equals(entry.getIface())).findFirst()
              .orElse(null);

      if(found != null) {
        continue;
      }
      changeMap.getDeletion().add(entry);
    }

    // Creation or edit
    for(Entry entry : changeMap.getRequiredDhcp()) {
      Entry found = changeMap.getCurrentDns().stream()
              .filter(req -> req.getHostname().equals(entry.getHostname()) && req.getIface().equals(entry.getIface())).findFirst()
              .orElse(null);

      if(found == null) {
        changeMap.getUpdate().add(entry);
        continue;
      }

      if(found.equals(entry)) {
        continue;
      }

      changeMap.getUpdate().add(entry);
    }
  }


  public List<String> getEnabledInterfaces() {
    return appConfig.dns().interfaces().stream().map(DNSConfig.DnsInterface::iface).toList();
  }

  public Set<String> getUsedZones() {
    return appConfig.dns().interfaces().stream().map(DNSConfig.DnsInterface::zone).collect(Collectors.toSet());
  }

  public DNSConfig.DnsInterface getInterfaceConfig(String iface) {
    return appConfig.dns().interfaces().stream().filter(i -> i.iface().equals(iface)).findFirst().orElse(null);
  }

}
