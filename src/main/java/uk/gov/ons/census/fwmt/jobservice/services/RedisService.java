package uk.gov.ons.census.fwmt.jobservice.services;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.net.SocketAddress;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisOptions;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class RedisService {
  @ConfigProperty(name = "redis.host") String host;
  @ConfigProperty(name = "redis.port") int port;
  @ConfigProperty(name = "redis.database") int database;
  @ConfigProperty(name = "redis.password") String password;

  private final Logger log = LoggerFactory.getLogger(RedisService.class);

  @Inject public Vertx vertx;

  public RedisOptions clientOptions;
  public Redis client;

  public Future<String> retrieveCCSOutcomeCache(String caseId) {
    RedisAPI api = RedisAPI.api(client);

    return Future.future(promise -> {
      api.get(caseId, res -> {
        if (res.succeeded()) {
          log.info("Received object from cache with case ID: " + caseId);
          promise.complete(String.valueOf(res.result().toString()));
        } else {
          promise.fail("Not found");
        }
      });
    });
  }

  void onStart(@Observes StartupEvent ev) {
    clientOptions = new RedisOptions().setEndpoint(SocketAddress.inetSocketAddress(port, host)).setSelect(database);
    if (!password.isEmpty())
      clientOptions.setPassword(password);

    client = Redis.createClient(vertx, clientOptions);

    client.connect(connectResult -> {
      if (connectResult.failed()) {
        log.error("Failed to connect to Redis: {}", connectResult.cause().toString());
      } else {
      }
    });
  }

  void onStop(@Observes ShutdownEvent ev) {
    client.close();
  }
}
