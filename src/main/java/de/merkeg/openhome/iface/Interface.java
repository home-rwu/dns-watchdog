package de.merkeg.openhome.iface;

import de.merkeg.openhome.dns.DnsEntry;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity( name = "iface")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Interface extends PanacheEntityBase {

  @Id
  String iface;
  String zone;
  String prefix;

  @OneToMany(mappedBy = "iface")
  Set<DnsEntry> entries;

  public static Set<String> getEnabledInterfaces() {
    return Interface.listAll().stream().map(iface -> ((Interface)iface).getIface()).collect(Collectors.toSet());
  }

  public static Set<String> getUsedZones() {
    return Interface.listAll().stream().map(iface -> ((Interface)iface).getZone()).collect(Collectors.toSet());
  }

}
