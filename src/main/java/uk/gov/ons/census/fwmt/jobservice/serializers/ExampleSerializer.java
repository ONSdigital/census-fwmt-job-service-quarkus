package uk.gov.ons.census.fwmt.jobservice.serializers;

import io.quarkus.jsonb.JsonbConfigCustomizer;
import javax.inject.Singleton;
import javax.json.bind.JsonbConfig;
import javax.json.bind.serializer.JsonbSerializer;

@Singleton
public class ExampleSerializer implements JsonbConfigCustomizer {

  public void customize(JsonbConfig config) {
//    config.withSerializers(new FooSerializer());
  }
}
