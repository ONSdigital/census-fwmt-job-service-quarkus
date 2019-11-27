package uk.gov.ons.census.fwmt.jobservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class JobServiceUtils {
  private static final Logger log = LoggerFactory.getLogger(JobServiceUtils.class);

  private static String addIfNotEmpty(List<String> addressLines, String addressLine) {
    if (addressLine != null && !addressLine.trim().isEmpty()) {
      addressLines.add(addressLine);
    }
    return addressLine;
  }

  public static <T> void printJSON(T javaObject) {
    ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
    try {
      String JSON = objectWriter.writeValueAsString(javaObject);
      log.debug(JSON);
    } catch (JsonProcessingException e) {
      log.error("Failed to process to JSON", e);
    }
  }
}
