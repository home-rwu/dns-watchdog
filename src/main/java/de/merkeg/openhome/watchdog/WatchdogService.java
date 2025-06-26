package de.merkeg.openhome.watchdog;

import de.merkeg.openhome.config.AppConfig;
import de.merkeg.openhome.dns.DnsEntry;
import de.merkeg.openhome.iface.Interface;
import de.merkeg.openhome.opnsense.DHCPLeases;
import de.merkeg.openhome.opnsense.OPNSenseLeasesService;
import de.merkeg.openhome.powerdns.PowerDNSService;
import de.merkeg.openhome.powerdns.api.PowerDNSZoneService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@ApplicationScoped
public class WatchdogService {

  @RestClient
  OPNSenseLeasesService opnsenseLeasesService;

  @Inject
  PowerDNSService powerDNSService;

  @Inject
  AppConfig appConfig;

  @Scheduled(every = "${app.schedule}")
  @Transactional
  public void cronJob(){
    log.debug("Searching for dns changes");

    Set<DHCPLeases.Lease> dhcpState = getCurrentDHCPState();
    Set<DHCPLeases.Lease> requiredChanges = getRequiredChanges(dhcpState);

    for(DHCPLeases.Lease lease : requiredChanges) {
      DnsEntry dnsEntry = DnsEntry.findById(lease.getMac());
      if(dnsEntry == null) { // For new entries
        DnsEntry newEntry = DnsEntry.fromLease(lease);
        powerDNSService.createOrUpdateDnsEntry(newEntry);
        newEntry.persist();
        continue;
      }

      if(dnsEntry.equalsEntry(lease)) continue; // Continue if nothing changed

      dnsEntry.setAddress(lease.getAddress());
      dnsEntry.setHostname(lease.getHostname());
      dnsEntry.setIface(Interface.findById(lease.getInterfaceName()));
      powerDNSService.createOrUpdateDnsEntry(dnsEntry);
    }

    DnsEntry.listAll().stream().filter(entry -> {
      return !requiredChanges.stream().map(DHCPLeases.Lease::getMac).toList().contains(((DnsEntry)entry).getMac());
    }).forEach(entry -> {
      if(((DnsEntry) entry).getLastSeen().plus(10, ChronoUnit.MINUTES).isBefore(LocalDateTime.now())) {
        log.info("Deleting Host entry {} - {} ({}) as their lease isn't there for longer than 10 minutes", ((DnsEntry) entry).getFqdn(), ((DnsEntry) entry).getAddress(), ((DnsEntry) entry).getMac());
        powerDNSService.deleteDnsEntry((DnsEntry)entry);
        entry.delete();
        return;
      }
      log.info("Host lease is expired {} - {} ({}) waiting a few minutes more before deleting", ((DnsEntry) entry).getFqdn(), ((DnsEntry) entry).getAddress(), ((DnsEntry) entry).getMac());


    });

    log.debug("Finished searching for dns changes");
  }



  private Set<DHCPLeases.Lease> getCurrentDHCPState() {
    DHCPLeases leases = opnsenseLeasesService.getLeases();
    return new HashSet<>(leases.getRows());
  }

  private Set<DHCPLeases.Lease> getRequiredChanges(Set<DHCPLeases.Lease> currentDhcp) {
    Set<DHCPLeases.Lease> entries = new HashSet<>();

    Set<String> enabledInterfaces = Interface.getEnabledInterfaces();
    for(DHCPLeases.Lease lease : currentDhcp) {

      if(!enabledInterfaces.contains(lease.getInterfaceName())) {
        log.trace("Excluding Host {} - {} ({}) as their interface is not included: {}", lease.getHostname(), lease.getAddress(), lease.getMac(), lease.getInterfaceName());
        continue;
      }

      DnsEntry dnsEntry = DnsEntry.findById(lease.getMac());
      if(dnsEntry != null) {
        dnsEntry.setLastSeen(LocalDateTime.now());
      }

      entries.add(lease);
    }

    return entries;
  }
}
