package uk.gov.ons.census.fwmt.jobservice.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.ons.census.fwmt.canonical.v1.Address;
import uk.gov.ons.census.fwmt.canonical.v1.CancelFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.UpdateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.data.ccs.CCSPropertyListingOutcome;
import uk.gov.ons.census.fwmt.common.data.modelcase.CasePauseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CcsCaseExtension;
import uk.gov.ons.census.fwmt.common.data.modelcase.Contact;
import uk.gov.ons.census.fwmt.common.data.modelcase.Location;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.jobservice.services.RedisService;

import javax.inject.Inject;
import javax.inject.Named;

import static uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest.TypeEnum.CCS;
import static uk.gov.ons.census.fwmt.jobservice.JobServiceUtils.setAddress;

@Named("CCS")
public class CCSINTConverter implements CometConverter {

  @Inject RedisService redisService;

  @Autowired
  private MessageConverter messageConverter;

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public CaseRequest convert(CreateFieldWorkerJobRequest ingest) throws GatewayException {

    CcsCaseExtension ccsCaseExtension = new CcsCaseExtension();
    CCSPropertyListingOutcome ccsPropertyListingCached = getCachedOutcomeDetails(ingest);
    CaseRequest caseRequest = new CaseRequest();
    Location location = new Location();

    caseRequest.setReference(ingest.getCaseReference());
    caseRequest.setType(CCS);
    caseRequest.setSurveyType(ingest.getSurveyType());
    caseRequest.setCategory(ingest.getCategory());
    caseRequest.setRequiredOfficer(ccsPropertyListingCached.getUsername());
    caseRequest.setCoordCode(ingest.getCoordinatorId());
    caseRequest.setContact(setContact(ingest));

    if (ccsPropertyListingCached.getCeDetails() != null
        && ccsPropertyListingCached.getCeDetails().getEstablishmentType().equals("CE")) {
      caseRequest.setEstabType(ccsPropertyListingCached.getCeDetails().getEstablishmentType());
    } else {
      caseRequest.setEstabType("HH");
    }

    location.setLat(ingest.getAddress().getLatitude().floatValue());
    location.set_long(ingest.getAddress().getLongitude().floatValue());
    caseRequest.setLocation(location);
    ccsCaseExtension.setQuestionnaireUrl(ingest.getCcsQuestionnaireURL());
    caseRequest.setCcs(ccsCaseExtension);

    ingest.setAddress(updateAddressWithOa(ingest, ccsPropertyListingCached.getAddress().getOa()));

    caseRequest.setSpecialInstructions(getSpecialInstructions(ccsPropertyListingCached));
    caseRequest.setAddress(setAddress(ingest));
    return caseRequest;
  }

  private Contact setContact(CreateFieldWorkerJobRequest ingest) {
    Contact contact = new Contact();
    contact.setName(ingest.getAddress().getPostCode());
    return contact;
  }

  private CCSPropertyListingOutcome getCachedOutcomeDetails(CreateFieldWorkerJobRequest ingest)
      throws GatewayException {
    // TODO is this safe?
    String retrievedCache = redisService.retrieveCCSOutcomeCache(String.valueOf(ingest.getCaseId())).result();

    return messageConverter.convertMessageToDTO(CCSPropertyListingOutcome.class, retrievedCache);
  }

  private String getSpecialInstructions(CCSPropertyListingOutcome cachedSpecialInstructions) throws GatewayException {
    try {
      return objectMapper.writeValueAsString(cachedSpecialInstructions);
    } catch (JsonProcessingException e) {
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, "Failed to convert CCSPL cached data to string");
    }
  }

  private Address updateAddressWithOa(CreateFieldWorkerJobRequest ingest, String cachedOa) {
    Address updatedAddress = ingest.getAddress();
    updatedAddress.setOa(cachedOa);

    return updatedAddress;
  }

  @Override
  public CasePauseRequest convertCancel(CancelFieldWorkerJobRequest cancelIngest) {
    throw new UnsupportedOperationException();
  }

  @Override
  public CaseRequest convertUpdate(UpdateFieldWorkerJobRequest ingest, ModelCase modelCase) {
    throw new UnsupportedOperationException();
  }
}
