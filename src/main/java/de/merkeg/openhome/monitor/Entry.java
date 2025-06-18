package de.merkeg.openhome.monitor;

import de.merkeg.openhome.config.DNSConfig;
import de.merkeg.openhome.opnsense.DHCPLeases;
import de.merkeg.openhome.powerdns.api.*;
import de.merkeg.openhome.powerdns.api.Record;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Entry {

  public static final String CREATED_PREFIX = "MANAGED";

  String hostname;
  String iface;
  String address;
  String mac;

  public static Entry fromLease(DHCPLeases.Lease lease) {
    return Entry.builder()
            .address(lease.getAddress())
            .hostname(lease.getHostname() != null ? lease.getHostname() : lease.getMac())
            .iface(lease.getInterfaceName())
            .mac(lease.getMac())
            .build();
  }

  public static Entry fromRRSet(RRSet rrSet) {
    Entry entry = new Entry();

    if(rrSet.getComments().isEmpty()) {
      return null;
    }
    String[] comment = rrSet.getComments().iterator().next().getContent().split("\\$");
    if(!comment[0].equals(CREATED_PREFIX)) {
      return null;
    }

    entry.setHostname(comment[1]);
    entry.setIface(comment[2]);
    entry.setMac(comment[3]);

    if(rrSet.getRecords().isEmpty()) {
      return null;
    }

    entry.setAddress(rrSet.getRecords().iterator().next().getContent());

    return entry;
  }

  public RRSet toRRSet(String prefix, String zone) {
    return toRRSet(prefix, zone, 14400);
  }

  public RRSet toRRSet(String prefix, String zone, int ttl) {
    return RRSet.builder()
            .name(buildDomainName(prefix, zone))
            .type(RecordType.A)
            .ttl(ttl)
            .account("")
            .changetype(Changetype.REPLACE)
            .records(Set.of(new Record(this.getAddress(), false)))
            .comments(Set.of(new Comment(buildComment(), "", 0)))
            .build();
  }

  public RRSet toDeletionRRSet(String prefix, String zone) {
    return RRSet.builder()
            .name(buildDomainName(prefix, zone))
            .type(RecordType.A)
            .changetype(Changetype.DELETE)
            .build();
  }

  private String buildComment() {
    return String.join("$", new String[]{CREATED_PREFIX, this.getHostname(), this.getIface(), this.getMac()});
  }

  @Override
  public boolean equals(Object o) {

    if(!(o instanceof Entry entry)) {
      return false;
    }

    if(!this.getHostname().equals(entry.getHostname())) return false;
    if(!this.getMac().equals(entry.getMac())) return false;
    if(!this.getIface().equals(entry.getIface())) return false;
    if(!this.getAddress().equals(entry.getAddress())) return false;

    return true;
  }

  public String buildDomainName(String prefix, String zone) {
    return String.join(".", new String[]{!hostname.isBlank() ? hostname : mac.replaceAll(":", "-"), prefix, zone}) + ".";
  }

}
