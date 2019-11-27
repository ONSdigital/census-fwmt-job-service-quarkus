package uk.gov.ons.census.fwmt.jobservice.services;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import uk.gov.ons.census.fwmt.common.data.modelcase.CasePauseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;

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

  @GET
  @Path("/cases/{case_id}")
  @Produces("application/json")
  ModelCase getById(@PathParam String case_id);

  @PUT
  @Path("/cases/{case_id}")
  @Produces("application/json")
  Response create(@PathParam String case_id, CaseRequest request);

  @PUT
  @Path("/cases/{case_id}/pause")
  @Produces("application/json")
  Response createPause(@PathParam String case_id, CasePauseRequest request);
}
