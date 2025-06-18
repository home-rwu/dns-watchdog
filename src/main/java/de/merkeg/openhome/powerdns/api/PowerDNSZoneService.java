package de.merkeg.openhome.powerdns.api;


import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.Set;

@Path("/api/v1/servers/{server_id}/zones")
@RegisterRestClient(configKey="powerdns")
@ClientHeaderParam(name = "X-API-Key", value = "${app.powerdns.api-key}")
public interface PowerDNSZoneService {

  @GET
  public Set<Zone> getZones(@PathParam("server_id") String serverId);

  @GET
  @Path("/{zone_id}")
  public Zone getZone(@PathParam("server_id") String serverId, @PathParam("zone_id") String zoneId);

  @PATCH
  @Path("/{zone_id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public void patchZone(@PathParam("server_id") String serverId, @PathParam("zone_id") String zoneId, ZonePatchRequest body);
}
