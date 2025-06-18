package de.merkeg.openhome.opnsense;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class DHCPLeases {

  int total;
  int rowCount;
  int current;
  Set<Lease> rows;
  Map<String, String> interfaces;

  @Data
  public static class Lease {
    String address;
    String type;
    String mac;
    String starts;
    String ends;
    String hostname;

    @JsonProperty("descr")
    String description;

    @JsonProperty("if_descr")
    String interfaceDescription;

    @JsonProperty("if")
    String interfaceName;

    String state;
    String status;
    String man;

    @JsonProperty("client-hostname")
    String clientHostname;
    String binding;

    int cltt;
    String uid;
  }
}
