package uk.gov.ons.census.fwmt.jobservice.resources;

import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.jobservice.services.QueueService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/queues")
public class QueueResource {
  @Inject
  QueueService service;

  @GET
  @Path("/processDLQ")
  public String startDLQProcessor() throws GatewayException {
    service.dumpDLQtoQueue();
    return "Transferring Gateway Action DLQ to Gateway Action Queue";
  }

  @GET
  @Path("/startListener")
  public String startListener() {
    service.resumeConsumer();
    return "Queue listener started";
  }

  @GET
  @Path("/stopListener")
  public String stopListener() {
    service.pauseConsumer();
    return "Queue listener stopped";
  }
}

