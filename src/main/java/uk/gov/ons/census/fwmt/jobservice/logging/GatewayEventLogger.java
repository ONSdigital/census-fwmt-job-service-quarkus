package uk.gov.ons.census.fwmt.jobservice.logging;

import io.quarkus.arc.DefaultBean;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class GatewayEventLogger {
  public static final String CANONICAL_CREATE_JOB_RECEIVED = "CANONICAL_CREATE_JOB_RECEIVED";
  public static final String CANONICAL_CANCEL_RECEIVED = "CANONICAL_CANCEL_RECEIVED";
  public static final String CANONICAL_UPDATE_RECEIVED = "CANONICAL_UPDATE_RECEIVED";
  public static final String COMET_CREATE_SENT = "COMET_CREATE_SENT";
  public static final String COMET_CREATE_ACK = "COMET_CREATE_ACK";
  public static final String COMET_CANCEL_SENT = "COMET_CANCEL_SENT";
  public static final String COMET_CANCEL_ACK = "COMET_CANCEL_ACK";
  public static final String COMET_UPDATE_SENT = "COMET_UPDATE_SENT";
  public static final String COMET_UPDATE_ACK = "COMET_UPDATE_ACK";
  public static final String TM_SERVICE_UP = "TM_SERVICE_UP";
  public static final String RABBIT_QUEUE_UP = "RABBIT_QUEUE_UP";
  public static final String REDIS_SERVICE_UP = "REDIS_SERVICE_UP";

  public static final String FAILED_TO_UNMARSHAL_CANONICAL = "FAILED_TO_UNMARSHAL_CANONICAL";
  public static final String INVALID_CANONICAL_ACTION = "INVALID_CANONICAL_ACTION";
  public static final String FAILED_TM_AUTHENTICATION = "FAILED_TM_AUTHENTICATION";
  public static final String FAILED_TO_CREATE_TM_JOB = "FAILED_TO_CREATE_TM_JOB";
  public static final String FAILED_TO_CANCEL_TM_JOB = "FAILED_TO_CANCEL_TM_JOB";
  public static final String FAILED_TO_UPDATE_TM_JOB = "FAILED_TO_UPDATE_TM_JOB";
  public static final String TM_SERVICE_DOWN = "TM_SERVICE_DOWN";
  public static final String RABBIT_QUEUE_DOWN = "RABBIT_QUEUE_DOWN";
  public static final String REDIS_SERVICE_DOWN = "REDIS_SERVICE_DOWN";

  // @Bean
  @Produces
  @DefaultBean
  public GatewayEventManager gatewayEventManager() {
    GatewayEventManager gatewayEventManager = new GatewayEventManager();
//    gatewayEventManager.setSource(Application.APPLICATION_NAME);
    gatewayEventManager.addEventTypes(new String[] {CANONICAL_CREATE_JOB_RECEIVED, CANONICAL_CANCEL_RECEIVED,
        CANONICAL_UPDATE_RECEIVED, COMET_CREATE_SENT, COMET_CREATE_ACK, COMET_CANCEL_SENT, COMET_CANCEL_ACK,
        COMET_UPDATE_SENT, COMET_UPDATE_ACK, TM_SERVICE_UP, RABBIT_QUEUE_UP, REDIS_SERVICE_UP});
    gatewayEventManager.addErrorEventTypes(new String[] {FAILED_TO_UNMARSHAL_CANONICAL, INVALID_CANONICAL_ACTION,
        FAILED_TM_AUTHENTICATION, FAILED_TO_CREATE_TM_JOB, FAILED_TO_CANCEL_TM_JOB, FAILED_TO_UPDATE_TM_JOB,
        TM_SERVICE_DOWN, RABBIT_QUEUE_DOWN, RABBIT_QUEUE_DOWN, REDIS_SERVICE_DOWN});

    return gatewayEventManager;
  }


}
