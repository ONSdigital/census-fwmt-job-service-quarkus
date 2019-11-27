package uk.gov.ons.census.fwmt.jobservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.Address;
import uk.gov.ons.census.fwmt.common.data.modelcase.Geography;

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

  public static List<String> extractAddressLines(CreateFieldWorkerJobRequest ingest) {
    List<String> addressLines = new ArrayList<>();

    addIfNotEmpty(addressLines, ingest.getAddress().getLine1());
    addIfNotEmpty(addressLines, ingest.getAddress().getLine2());
    addIfNotEmpty(addressLines, ingest.getAddress().getLine3());
    addIfNotEmpty(addressLines, ingest.getAddress().getTownName());

    return addressLines;
  }

  public static Address setAddress(CreateFieldWorkerJobRequest ingest) {
    Address address = new Address();
    Geography geography = new Geography();

    // look into this method - may not require town
    address.setLines(extractAddressLines(ingest));

    // arin not yet part of Comet
    //    try {
    //      address.setArid(Long.valueOf(ingest.getAddress().getArid()));
    //    } catch (Exception e) {
    //      // if a problem resolving ARID, null is fine
    //    }
    Long uprn = null;
    try {
      uprn = Long.valueOf(ingest.getAddress().getUprn());
    } catch (NumberFormatException e) {
      // null is okay
    }
    address.setUprn(uprn);

    address.setTown(ingest.getAddress().getTownName());
    address.setPostcode(ingest.getAddress().getPostCode());

    geography.setOa(ingest.getAddress().getOa());
    address.setGeography(geography);

    return address;
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
