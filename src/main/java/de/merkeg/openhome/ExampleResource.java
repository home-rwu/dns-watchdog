package de.merkeg.openhome;

import de.merkeg.openhome.config.AppConfig;
import de.merkeg.openhome.monitor.ChangeMap;
import de.merkeg.openhome.monitor.MonitorService;
import de.merkeg.openhome.opnsense.DHCPLeases;
import de.merkeg.openhome.opnsense.OPNSenseLeasesService;
import de.merkeg.openhome.powerdns.api.PowerDNSZoneService;
import de.merkeg.openhome.powerdns.api.Zone;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/hello")
@Slf4j
public class ExampleResource {

  @Inject
  MonitorService monitorService;

  @Inject
  AppConfig appConfig;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public ChangeMap hello() {

    return monitorService.createChangemap();
  }

  @Path("exec")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public ChangeMap exec() {
    ChangeMap changeMap = monitorService.createChangemap();

    monitorService.executeDnsChanges(changeMap);

    return changeMap;
  }

}
