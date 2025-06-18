package de.merkeg.openhome.powerdns.api;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class ZonePatchRequest {
  Set<RRSet> rrsets;
}
