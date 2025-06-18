package de.merkeg.openhome.config;

import java.util.List;

public interface DNSConfig {

  List<DnsInterface> interfaces();

  public static interface DnsInterface {
    String iface();
    String zone();
    String prefix();
  }
}
