package uk.gov.ons.census.fwmt.jobservice.services;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ons.census.fwmt.common.error.GatewayException;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RegisterForReflection
public class CometHeadersFactory implements ClientHeadersFactory {

  private final Logger log = LoggerFactory.getLogger(QueueService.class);

  // O365 credentials for authentication w/o login prompt
  @ConfigProperty(name = "totalmobile.resource") String resource;

  // Azure Directory OAUTH 2.0 AUTHORIZATION ENDPOINT
  @ConfigProperty(name = "totalmobile.authority") String authority;

  @ConfigProperty(name = "totalmobile.client_id") private transient String clientID;
  @ConfigProperty(name = "totalmobile.client_secret") private transient String clientSecret;

  private transient AuthenticationResult auth;
  private transient MultivaluedMap<String, String> newHeaders = new MultivaluedHashMap<>();

  private boolean isAuthed() {
    return this.auth != null && auth.getExpiresOnDate().after(new Date());
  }

  private void auth() throws GatewayException {
    ExecutorService service = Executors.newFixedThreadPool(1);
    try {
      AuthenticationContext context = new AuthenticationContext(authority, false, service);
      ClientCredential cc = new ClientCredential(clientID, clientSecret);

      Future<AuthenticationResult> future = context.acquireToken(resource, cc, null);
      this.auth = future.get();
      newHeaders.putSingle("Authorization", "Bearer " + auth.getAccessToken());
    } catch (MalformedURLException | InterruptedException | ExecutionException e) {
      String errorMsg = "Failed to Authenticate with Totalmobile";
      log.error(errorMsg);
      // GatewayEventsConfig.FAILED_TM_AUTHENTICATION
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, errorMsg, e);
    } finally {
      service.shutdown();
    }
  }

  @Override
  public MultivaluedMap<String, String> update(
      MultivaluedMap<String, String> incomingHeaders,
      MultivaluedMap<String, String> outgoingHeaders) {
    if (!clientID.isEmpty() && !clientSecret.isEmpty() && !isAuthed()) {
      try {
        auth();
      } catch (GatewayException e) {
        log.error(e.toString());
      }
    }
    return newHeaders;
  }
}
