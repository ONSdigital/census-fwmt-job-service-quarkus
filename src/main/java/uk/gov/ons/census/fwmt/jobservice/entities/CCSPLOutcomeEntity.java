package uk.gov.ons.census.fwmt.jobservice.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CCSPLOutcomeEntity implements Serializable {

  private String caseId;

  private String ccsPropertyListingOutcome;

}
