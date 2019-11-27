package uk.gov.ons.census.fwmt.jobservice.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.ons.census.fwmt.jobservice.services.RedisService;

import javax.inject.Inject;
import javax.inject.Named;

@Named("CCS")
public class CCSINTConverter implements CometConverter {

  @Inject RedisService redisService;

  private MessageConverter messageConverter;

  private ObjectMapper objectMapper;

}
