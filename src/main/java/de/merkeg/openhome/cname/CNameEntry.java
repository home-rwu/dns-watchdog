package de.merkeg.openhome.cname;

import de.merkeg.openhome.dns.DnsEntry;
import de.merkeg.openhome.powerdns.api.Changetype;
import de.merkeg.openhome.powerdns.api.RRSet;
import de.merkeg.openhome.powerdns.api.Record;
import de.merkeg.openhome.powerdns.api.RecordType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity(name = "cname")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CNameEntry extends PanacheEntityBase {

  @Id
  String fqdn;

  @ManyToOne
  DnsEntry dns;


  @Transient
  public RRSet toUpdateSet() {
    return RRSet.builder()
            .name(getFqdn() + ".")
            .type(RecordType.CNAME)
            .ttl(300)
            .account("")
            .changetype(Changetype.REPLACE)
            .records(Set.of(new Record(this.dns.getFqdn(), false)))
            .build();
  }

}
