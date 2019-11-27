package uk.gov.ons.census.fwmt.jobservice.processors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ons.census.fwmt.jobservice.services.QueueService;

import javax.inject.Inject;
import java.io.IOException;

public class GatewayActionProcessor {

  private final Logger log = LoggerFactory.getLogger(QueueService.class);

  @Inject
  private JobService jobService;

  @Inject
  private ObjectMapper mapper = new ObjectMapper();

}
