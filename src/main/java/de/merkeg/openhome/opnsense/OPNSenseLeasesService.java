package de.merkeg.openhome.opnsense;

import io.quarkus.rest.client.reactive.ClientBasicAuth;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/dhcpv4/leases")
@RegisterRestClient(configKey="opnsense-leases")
@ClientBasicAuth(username = "${app.opnsense.api-key}", password = "${app.opnsense.secret-key}")
public interface OPNSenseLeasesService {

  @GET
  @Path("search_lease")
  DHCPLeases getLeases();
}
