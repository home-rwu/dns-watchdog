package de.merkeg.openhome;

import de.merkeg.openhome.powerdns.api.PowerDNSZoneService;
import de.merkeg.openhome.powerdns.api.Zone;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/")
public class TestResource {

  @RestClient
  PowerDNSZoneService powerDNSZoneService;

  @GET
  public Zone getTest() {
    return powerDNSZoneService.getZone("localhost", "intern.home.rwu.de");
  }
}
