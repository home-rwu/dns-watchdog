package de.merkeg.openhome.powerdns.api;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class RRSet {
  String name;
  RecordType type;
  int ttl;
  Changetype changetype;
  Set<Record> records;
  Set<Comment> comments;
  String account;
}
