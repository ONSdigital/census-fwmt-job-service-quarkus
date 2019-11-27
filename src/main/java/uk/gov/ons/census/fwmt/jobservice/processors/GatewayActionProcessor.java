package uk.gov.ons.census.fwmt.jobservice.processors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ons.census.fwmt.canonical.v1.CancelFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.UpdateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.services.QueueService;

import javax.inject.Inject;
import java.io.IOException;

import static uk.gov.ons.census.fwmt.jobservice.logging.GatewayEventLogger.CANONICAL_CANCEL_RECEIVED;
import static uk.gov.ons.census.fwmt.jobservice.logging.GatewayEventLogger.CANONICAL_CREATE_JOB_RECEIVED;
import static uk.gov.ons.census.fwmt.jobservice.logging.GatewayEventLogger.CANONICAL_UPDATE_RECEIVED;
import static uk.gov.ons.census.fwmt.jobservice.logging.GatewayEventLogger.INVALID_CANONICAL_ACTION;

public class GatewayActionProcessor {

  private final Logger log = LoggerFactory.getLogger(QueueService.class);

  @Inject
  private JobService jobService;

  @Inject
  private GatewayEventManager gatewayEventManager;

  @Inject
  private ObjectMapper mapper = new ObjectMapper();

  public <T> T convertMessageToDTO(Class<T> klass, String message) throws GatewayException {
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    try {
      return mapper.readValue(message, klass);
    } catch (IOException e) {
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, "Failed to convert message into DTO.", e);
    }
  }

  private void process(RabbitMQMessage message) throws GatewayException {
    log.info("received a message from RM-Adapter");
    String messageString = message.body().toString();
    JsonObject messageJson = new JsonObject(messageString);
//    try {
//      actualMessageRootNode = mapper.readTree(message);
//    } catch (IOException e) {
//      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, "Cannot process message JSON");
//    }
    String gatewayType = messageJson.getString("gatewayType");
    String caseId = messageJson.getString("caseId");

    switch (gatewayType) {
    case "Create":
      CreateFieldWorkerJobRequest fwmtCreateJobRequest = convertMessageToDTO(CreateFieldWorkerJobRequest.class, messageString);
      gatewayEventManager.triggerEvent(String.valueOf(fwmtCreateJobRequest.getCaseId()), CANONICAL_CREATE_JOB_RECEIVED);
      jobService.createJob(fwmtCreateJobRequest);
      break;
    case "Cancel":
      CancelFieldWorkerJobRequest fwmtCancelJobRequest = convertMessageToDTO(CancelFieldWorkerJobRequest.class, messageString);
      gatewayEventManager.triggerEvent(String.valueOf(fwmtCancelJobRequest.getCaseId()), CANONICAL_CANCEL_RECEIVED);
      jobService.cancelJob(fwmtCancelJobRequest);
      break;
    case "Update":
      UpdateFieldWorkerJobRequest fwmtUpdateJobRequest = convertMessageToDTO(UpdateFieldWorkerJobRequest.class, messageString);
      gatewayEventManager.triggerEvent(String.valueOf(fwmtUpdateJobRequest.getCaseId()), CANONICAL_UPDATE_RECEIVED);
      jobService.updateJob(fwmtUpdateJobRequest);
      break;
    default:
      String errorMsg = "Invalid Canonical Action.";
      gatewayEventManager.triggerErrorEvent(this.getClass(), errorMsg, "<UNKNOWN_CASE_ID>", INVALID_CANONICAL_ACTION);
      throw new GatewayException(GatewayException.Fault.BAD_REQUEST, errorMsg);
    }
    log.info("Sending " + caseId + " job to TM");
  }
}
