package uk.gov.ons.census.fwmt.jobservice.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.inject.Inject;
import java.io.IOException;

public class MessageConverter {

  @Inject
  private ObjectMapper mapper = new ObjectMapper();

  public <T> T convertMessageToDTO(Class<T> klass, String message) {
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    T dto;
    try {
      dto = mapper.readValue(message, klass);
      return dto;
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }

}