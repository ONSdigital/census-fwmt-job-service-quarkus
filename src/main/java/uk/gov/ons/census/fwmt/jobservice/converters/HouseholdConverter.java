package uk.gov.ons.census.fwmt.jobservice.converters;

import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;

@Named("HH")
public class HouseholdConverter implements CometConverter {

  private final Logger log = LoggerFactory.getLogger(HouseholdConverter.class);

  private MapperFacade mapperFacade;


}
