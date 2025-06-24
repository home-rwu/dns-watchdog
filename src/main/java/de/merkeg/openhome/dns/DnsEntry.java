package de.merkeg.openhome.dns;

import de.merkeg.openhome.cname.CNameEntry;
import de.merkeg.openhome.iface.Interface;
import de.merkeg.openhome.opnsense.DHCPLeases;
import de.merkeg.openhome.powerdns.api.Changetype;
import de.merkeg.openhome.powerdns.api.RRSet;
import de.merkeg.openhome.powerdns.api.Record;
import de.merkeg.openhome.powerdns.api.RecordType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity(name = "dns")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DnsEntry extends PanacheEntityBase {

  @Id
  String mac;

  String hostname;
  String address;

  @ManyToOne
  Interface iface;

  @OneToMany(mappedBy = "dns", orphanRemoval = true, cascade = CascadeType.REMOVE)
  Set<CNameEntry> cnames;


  public String getFqdn() {
    return String.join(".", new String[]{!hostname.isBlank() ? hostname : mac.replaceAll(":", "-"), iface.getPrefix(), iface.getZone()}) + ".";
  }

  public static DnsEntry fromLease(DHCPLeases.Lease lease) {
    return DnsEntry.builder()
            .mac(lease.getMac())
            .hostname(lease.getHostname())
            .address(lease.getAddress())
            .iface(Interface.findById(lease.getInterfaceName()))
            .build();
  }

  @Transient
  public RRSet toUpdateSet() {
    return RRSet.builder()
            .name(getFqdn())
            .type(RecordType.A)
            .ttl(300)
            .account("")
            .changetype(Changetype.REPLACE)
            .records(Set.of(new Record(this.getAddress(), false)))
            .build();
  }

  @Transient
  public RRSet toDeletionSet() {
    return RRSet.builder()
            .name(getFqdn())
            .type(RecordType.A)
            .changetype(Changetype.DELETE)
            .build();
  }

  @Transient
  public boolean equalsEntry(DHCPLeases.Lease entry) {
    if(!this.getHostname().equals(entry.getHostname())) return false;
    if(!this.getMac().equals(entry.getMac())) return false;
    if(!this.getIface().getIface().equals(entry.getInterfaceName())) return false;
    if(!this.getAddress().equals(entry.getAddress())) return false;

    return true;
  }



}
