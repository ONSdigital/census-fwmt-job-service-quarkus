package uk.gov.ons.census.fwmt.jobservice.services;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@RegisterClientHeaders(CometHeadersFactory.class)
@RegisterRestClient
public interface CometService {
  @GET
  @Path("/swagger-ui.html")
  @Produces("application/json")
  Response getSwagger();
}
