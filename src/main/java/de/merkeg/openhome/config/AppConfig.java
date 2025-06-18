package de.merkeg.openhome.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "app")
public interface AppConfig {
  OPNSenseConfig opnsense();
  PowerDNSConfig powerdns();
  DNSConfig dns();
  String schedule();
}
