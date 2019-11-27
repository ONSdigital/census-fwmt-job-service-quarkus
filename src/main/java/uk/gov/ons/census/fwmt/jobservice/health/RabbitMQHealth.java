package uk.gov.ons.census.fwmt.jobservice.health;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.services.QueueService;

import javax.inject.Inject;

import static uk.gov.ons.census.fwmt.jobservice.logging.GatewayEventLogger.RABBIT_QUEUE_DOWN;
import static uk.gov.ons.census.fwmt.jobservice.logging.GatewayEventLogger.RABBIT_QUEUE_UP;

@Liveness
public class RabbitMQHealth implements HealthCheck {
  @Inject QueueService queueService;

  @Inject GatewayEventManager gatewayEventManager;

  private void tryGetCount(String name, Promise<Void> promise) {
    queueService.client.messageCount(name, gaResult -> {
      if (gaResult.succeeded()) {
        promise.complete();
      } else {
        promise.fail((String) null);
      }
    });
  }

  @Override
  public HealthCheckResponse call() {
    // We start off with a response set to UP. It might later be set to DOWN.
    HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("RabbitMQ").up();

    responseBuilder.withData("client connected", queueService.client.isConnected());
    if (queueService.client.isConnected()) {
      // check the Gateway.Action Queue
      Future<Void> gaFuture = Future.future(promise -> {
        tryGetCount(queueService.gaName, promise);
      });
      responseBuilder.withData(queueService.gaName + " ping successful", gaFuture.succeeded());
      // check the Gateway.Action DLQ
      Future<Void> gaDLQFuture = Future.future(promise -> {
        tryGetCount(queueService.gaDLQName, promise);
      });
      responseBuilder.withData(queueService.gaDLQName + " ping successful", gaDLQFuture.succeeded());
      // check the overall status
      if (gaFuture.failed() || gaDLQFuture.failed()) {
        responseBuilder.down();
      }
    } else {
      responseBuilder.down();
    }

    HealthCheckResponse response = responseBuilder.build();

    if (response.getState() == HealthCheckResponse.State.UP) {
      gatewayEventManager.triggerEvent("<N/A>", RABBIT_QUEUE_UP);
    } else {
      gatewayEventManager.triggerErrorEvent(this.getClass(), "Cannot reach RabbitMQ", "<NA>", RABBIT_QUEUE_DOWN);
    }

    return response;
  }
}

