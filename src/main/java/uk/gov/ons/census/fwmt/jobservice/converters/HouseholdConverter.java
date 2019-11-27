package uk.gov.ons.census.fwmt.jobservice.converters;

import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import uk.gov.ons.census.fwmt.canonical.v1.CancelFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.UpdateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.Address;
import uk.gov.ons.census.fwmt.common.data.modelcase.CasePauseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.Contact;
import uk.gov.ons.census.fwmt.common.data.modelcase.Geography;
import uk.gov.ons.census.fwmt.common.data.modelcase.Location;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;

import javax.inject.Named;

import static uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest.TypeEnum.HH;
import static uk.gov.ons.census.fwmt.jobservice.JobServiceUtils.extractAddressLines;

@Named("HH")
public class HouseholdConverter implements CometConverter {

  private final Logger log = LoggerFactory.getLogger(HouseholdConverter.class);

  @Autowired
  private MapperFacade mapperFacade;

  @Override
  public CaseRequest convert(CreateFieldWorkerJobRequest ingest) {
    CaseRequest caseRequest = new CaseRequest();
    caseRequest.setReference(ingest.getCaseReference());
    caseRequest.setType(HH);
    caseRequest.setSurveyType(ingest.getCaseType());
    // Category is not yet in the feed
    caseRequest.setCategory("Household");
    //    caseRequest.setCategory(ingest.getCategory());
    caseRequest.setRequiredOfficer(ingest.getMandatoryResource());
    caseRequest.setEstabType(ingest.getEstablishmentType());
    caseRequest.setCoordCode(ingest.getCoordinatorId());

    Contact contact = new Contact();
    contact.setName(ingest.getAddress().getPostCode());
    if (ingest.getContact() != null) {
      contact.setOrganisationName(ingest.getContact().getOrganisationName());

      contact.setPhone(ingest.getContact().getPhoneNumber());
      contact.setEmail(ingest.getContact().getEmailAddress());
    }
    caseRequest.setContact(contact);

    Address address = new Address();
    // arid not yet part of Comet
    // try {
    //   address.setArid(Long.valueOf(ingest.getAddress().getArid()));
    // } catch (Exception e) {
    //   // if a problem resolving ARID, null is fine
    // }

    try {
      address.setUprn(Long.valueOf(ingest.getAddress().getUprn()));
    } catch (NumberFormatException e) {
      // TODO is this still the case? Is a custom warning fine?
      log.warn("Error parsing a UPRN of '{}'", ingest.getAddress().getUprn());
      // if a problem resolving UPRN, null is fine
      address.setUprn(null);
    }

    address.setLines(extractAddressLines(ingest));
    address.setTown(ingest.getAddress().getTownName());
    address.setPostcode(ingest.getAddress().getPostCode());

    Geography geography = new Geography();
    geography.setOa(ingest.getAddress().getOa());
    address.setGeography(geography);

    caseRequest.setAddress(address);

    Location location = new Location();
    location.setLat(ingest.getAddress().getLatitude().floatValue());
    location.set_long(ingest.getAddress().getLongitude().floatValue());
    caseRequest.setLocation(location);

    // These are still needed as part of a create house hold,
    // unsure where they are derived from.
    caseRequest.setDescription("CENSUS");
    caseRequest.setSpecialInstructions("Special Instructions");
    // caseRequest.setDescription(ingest.getDescription()) ;
    // caseRequest.setSpecialInstructions(ingest.getSpecialInstructions());
    caseRequest.setUaa(ingest.isUua());
    caseRequest.setSai(ingest.isSai());

    return caseRequest;
  }

  @Override
  public CasePauseRequest convertCancel(CancelFieldWorkerJobRequest ingest) {
    CasePauseRequest pauseRequest = new CasePauseRequest();
    pauseRequest.setId(String.valueOf(ingest.getCaseId()));
    pauseRequest.setUntil(ingest.getUntil());
    pauseRequest.setReason(ingest.getReason());

    return pauseRequest;
  }

  @Override
  public CaseRequest convertUpdate(UpdateFieldWorkerJobRequest ingest,
      ModelCase modelCase) {
    CaseRequest updateRequest = mapperFacade.map(modelCase, CaseRequest.class);
    CasePauseRequest casePauseRequest = new CasePauseRequest();

    int dateComparison = 0;

    if (ingest.getAddressType().equals("HH")) {

      if (StringUtils.isEmpty(updateRequest.getPause())) {
        updateRequest.setPause(casePauseRequest);
        dateComparison = 1;
      } else {
        dateComparison = ingest.getHoldUntil().compareTo(updateRequest.getPause().getUntil());
      }

      if (!ingest.isBlankFormReturned() && dateComparison > 0) {
        updateRequest.getPause().setUntil(ingest.getHoldUntil());
        updateRequest.getPause().setReason("HQ Case Pause");
      } else if (ingest.isBlankFormReturned() && !updateRequest.isBlankFormReturned()) {
        updateRequest.getPause().setUntil(ingest.getHoldUntil());
        updateRequest.getPause().setReason("Case reinstated - blank QRE");
      }
    }

    if (ingest.getAddressType().equals("CE")) {
      updateRequest.getCe().setActualResponses(ingest.getCeActualResponses());
      updateRequest.getCe().setCe1Complete(ingest.isCe1Complete());
      updateRequest.getCe().setExpectedResponses(ingest.getCeExpectedResponses());
    }

    return updateRequest;
  }

}
