package de.merkeg.openhome.watchdog;

import de.merkeg.openhome.powerdns.api.RRSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeMap {
  Set<Entry> currentDhcp = new HashSet<>();
  Set<Entry> currentDns = new HashSet<>();

  Set<Entry> requiredDhcp = new HashSet<>();

  Set<Entry> deletion = new HashSet<>();
  Set<Entry> update = new HashSet<>();

  Map<String, Set<RRSet>> mappings = new HashMap<>();

}
