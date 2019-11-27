package uk.gov.ons.census.fwmt.jobservice.processors;

import uk.gov.ons.census.fwmt.jobservice.converters.CometConverter;
import uk.gov.ons.census.fwmt.jobservice.services.CometService;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JobService {
  @Inject
  private CometService cometService;

  @Inject
  private Map<String, CometConverter> cometConverters;

  private static final List<Response.Status> validResponses = List.of(Response.Status.OK, Response.Status.CREATED, Response.Status.ACCEPTED);

}

