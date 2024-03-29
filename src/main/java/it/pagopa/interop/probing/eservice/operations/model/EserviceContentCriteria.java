package it.pagopa.interop.probing.eservice.operations.model;

import java.util.List;
import it.pagopa.interop.probing.eservice.operations.dtos.EserviceContent;
import it.pagopa.interop.probing.eservice.operations.dtos.EserviceTechnology;

public class EserviceContentCriteria extends EserviceContent {

  private static final long serialVersionUID = 1L;

  public EserviceContentCriteria(Long eserviceRecordId, EserviceTechnology technology,
      String[] basePath, String[] audience) {
    this.eserviceRecordId(eserviceRecordId);
    this.technology(technology);
    this.basePath(List.of(basePath));
    this.audience(List.of(audience));
  }
}
