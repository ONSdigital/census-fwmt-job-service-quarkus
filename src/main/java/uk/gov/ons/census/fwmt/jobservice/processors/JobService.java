package uk.gov.ons.census.fwmt.jobservice.processors;

import uk.gov.ons.census.fwmt.canonical.v1.CancelFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.UpdateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CasePauseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
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

  @Inject
  private GatewayEventManager gatewayEventManager;

  private static final List<Response.Status> validResponses = List.of(Response.Status.OK, Response.Status.CREATED, Response.Status.ACCEPTED);

  public void createJob(CreateFieldWorkerJobRequest jobRequest) throws GatewayException {
    final CometConverter cometConverter = cometConverters.get(jobRequest.getCaseType());
    CaseRequest caseRequest = cometConverter.convert(jobRequest);
//    gatewayEventManager.triggerEvent(String.valueOf(jobRequest.getCaseId()), COMET_CREATE_SENT, "Case Ref", jobRequest.getCaseReference());
    Response response = cometService.create(jobRequest.getCaseId().toString(), caseRequest);
//    validateResponse(response, jobRequest.getCaseId(), "Create", FAILED_TO_CREATE_TM_JOB);
//    gatewayEventManager.triggerEvent(String.valueOf(jobRequest.getCaseId()), COMET_CREATE_ACK, "Response Code", response.getStatusInfo().getReasonPhrase());
  }

  public void cancelJob(CancelFieldWorkerJobRequest cancelRequest) throws GatewayException {
    final CometConverter cometConverter = cometConverters.get("HH");
    CasePauseRequest casePauseRequest = cometConverter.convertCancel(cancelRequest);
//    gatewayEventManager.triggerEvent(String.valueOf(cancelRequest.getCaseId()), COMET_CANCEL_SENT);
    Response response = cometService.createPause(cancelRequest.getCaseId().toString(), casePauseRequest);
//    validateResponse(response, cancelRequest.getCaseId(), "Cancel", FAILED_TO_CANCEL_TM_JOB);
//    gatewayEventManager.triggerEvent(String.valueOf(cancelRequest.getCaseId()), COMET_CANCEL_ACK, "Response Code", response.getStatusInfo().getReasonPhrase());
  }

  public void updateJob(UpdateFieldWorkerJobRequest updateRequest) throws GatewayException {
    final CometConverter cometConverter = cometConverters.get("HH");
    ModelCase modelCase = cometService.getById(updateRequest.getCaseId().toString());
    CaseRequest caseRequest = cometConverter.convertUpdate(updateRequest, modelCase);
//    gatewayEventManager.triggerEvent(String.valueOf(updateRequest.getCaseId()), COMET_UPDATE_SENT);
    if (caseRequest.getPause() != null) {
      CasePauseRequest casePauseRequest = caseRequest.getPause();
      Response response = cometService.createPause(updateRequest.getCaseId().toString(), casePauseRequest);
//      validateResponse(response, updateRequest.getCaseId(), "Pause", FAILED_TO_UPDATE_TM_JOB);
    }
    Response response = cometService.create(updateRequest.getCaseId().toString(), caseRequest);
//    validateResponse(response, updateRequest.getCaseId(), "Pause", FAILED_TO_UPDATE_TM_JOB);
//    gatewayEventManager.triggerEvent(String.valueOf(updateRequest.getCaseId()), COMET_UPDATE_ACK, "Response Code", response.getStatusInfo().getReasonPhrase());
  }

  private void validateResponse(Response response, UUID caseId, String verb, String errorCode)
      throws GatewayException {
    if (!validResponses.contains(Response.Status.fromStatusCode(response.getStatus()))) {
      String msg = "Unable to " + verb + " FieldWorkerJobRequest: HTTP_STATUS:" + response.getStatusInfo().getReasonPhrase() + ":" + response.getStatus();
//      gatewayEventManager.triggerErrorEvent(this.getClass(), msg, String.valueOf(caseId), errorCode);
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, msg);
    }
  }

}

