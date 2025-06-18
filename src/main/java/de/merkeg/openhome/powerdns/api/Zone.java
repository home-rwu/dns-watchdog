package de.merkeg.openhome.powerdns.api;

import lombok.Data;

import java.util.Set;

@Data
public class Zone {
  String id;
  String name;
  String type;
  String url;
  String kind;
  Set<RRSet> rrsets;
}
