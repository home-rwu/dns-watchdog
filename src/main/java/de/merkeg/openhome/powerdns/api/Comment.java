package de.merkeg.openhome.powerdns.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
  String content;
  String account;

  @JsonProperty("modified_at")
  int modifiedAt;
}
