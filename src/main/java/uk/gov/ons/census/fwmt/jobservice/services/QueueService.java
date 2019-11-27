package uk.gov.ons.census.fwmt.jobservice.services;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQOptions;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class QueueService {
  @ConfigProperty(name = "queues.host") String host;
  @ConfigProperty(name = "queues.port") int port;
  @ConfigProperty(name = "queues.user") String user;
  @ConfigProperty(name = "queues.password") String password;
  @ConfigProperty(name = "queues.gateway-actions.name") public String gaName;
  @ConfigProperty(name = "queues.gateway-actions.dlq") public String gaDLQName;
  @ConfigProperty(name = "queues.gateway-actions.routing-key") public String gaRoutingKey;

  @Inject public Vertx vertx;

  private final Logger log = LoggerFactory.getLogger(QueueService.class);

  public RabbitMQOptions clientOptions;
  public RabbitMQClient client;

  private RabbitMQConsumer consumer;

  public void pauseConsumer() {
    if (consumer != null) {
      consumer.pause();
    } else {
      log.warn("Consumer not initialized");
    }
  }

  public void resumeConsumer() {
    if (consumer != null) {
      consumer.resume();
    } else {
      log.warn("Consumer not initialized");
    }
  }

  private void singleDLQtoQueue() {
    if (client.isConnected()) {
      client.basicGet(gaDLQName, false, getResult -> {
        if (getResult.succeeded()) {
          JsonObject msg = getResult.result();
          client.basicPublish("", gaName, msg, pubResult -> {
            if (pubResult.failed()) {
              log.warn("Publishing to queue failed");
            } else {
              client.basicAck(msg.getLong("deliveryTag"), false, asyncResult -> {});
            }
          });
        } else {
          log.warn("Consuming from DLQ failed");
        }
      });
    } else {
      log.warn("Client not connected");
    }
  }

  public void dumpDLQtoQueue() {
    JsonObject gaDLQConfig = new JsonObject();
    if (client.isConnected()) {
      client.messageCount(gaDLQName, countResult -> {
        if (countResult.succeeded()) {
          long messageCount = countResult.result();
          for (long i = 0; i < messageCount; i++) {
            singleDLQtoQueue();
          }
        }
      });
    } else {
      log.warn("Client not connected");
    }
  }

  void onStart(@Observes StartupEvent ev) {
    clientOptions = new RabbitMQOptions()
        .setHost(host)
        .setPort(port)
        .setUser(user)
        .setPassword(password)
        .setVirtualHost("/");
    client = RabbitMQClient.create(vertx, clientOptions);

    JsonObject gaConfig = new JsonObject();
    gaConfig.put("x-dead-letter-exchange", "");
    gaConfig.put("x-dead-letter-routing-key", gaDLQName);

    JsonObject gaDLQConfig = new JsonObject();

    QueueOptions gaConsumerOptions = new QueueOptions()
        .setMaxInternalQueueSize(1000)
        .setKeepMostRecent(true);

    client.start(startResult -> {
      if (startResult.failed()) {
        log.error("Failed to connect to RabbitMQ: {}", startResult.cause().toString());
      } else {
        client.queueDeclare(gaName, true, false, false, gaConfig, queueResult -> {
          if (queueResult.failed()) {
            log.error("Failed to set up the Gateway Actions Queue: {}", queueResult.cause().toString());
          }
        });

        client.queueDeclare(gaDLQName, true, false, false, gaDLQConfig, queueResult -> {
          if (queueResult.failed()) {
            log.error("Failed to set up the Gateway Actions DLQ: {}", queueResult.cause().toString());
          }
        });

        client.basicConsumer(gaName, gaConsumerOptions, consumerResult -> {
          if (consumerResult.failed()) {
            consumerResult.cause().printStackTrace();
            log.error("Failed to set up the Gateway Actions Queue consumer: {}", consumerResult.cause().toString());
          } else {
            RabbitMQConsumer mqConsumer = consumerResult.result();
            this.consumer = mqConsumer;
            mqConsumer.handler(message -> {
              log.info("Got message: " + message.body().toString());
            });
          }
        });
      }
    });
  }

  void onStop(@Observes ShutdownEvent ev) {
    consumer.cancel(consumerCancelResult -> {
      client.stop(stoppedResult -> {});
    });
  }
}
