package it.pagopa.interop.probing.eservice.operations.util.logging.impl;

import it.pagopa.interop.probing.eservice.operations.dtos.EserviceInteropState;
import it.pagopa.interop.probing.eservice.operations.dtos.EserviceMonitorState;
import it.pagopa.interop.probing.eservice.operations.model.Eservice;
import it.pagopa.interop.probing.eservice.operations.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;

import java.time.OffsetTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class LoggerImpl implements Logger {

  @Override
  public void logMessageEserviceStateUpdated(Eservice eServiceToUpdate) {
    log.info(
        "e-service has been updated to new state. eserviceId={}, versionId={}, eserviceState={}",
        eServiceToUpdate.eserviceId(),
        eServiceToUpdate.versionId(), eServiceToUpdate.state());
  }

  @Override
  public void logMessageEserviceProbingStateUpdated(Eservice eServiceToUpdate) {
    log.info(
        "e-service probing state has been updated to new state. eserviceId={}, versionId={}, probingEnabled={}",
        eServiceToUpdate.eserviceId(),
        eServiceToUpdate.versionId(), eServiceToUpdate.probingEnabled());
  }

  @Override
  public void logMessageEservicePollingConfigUpdated(Eservice eServiceToUpdate) {
    log.info(
        "e-service polling data have been updated. eserviceId={}, versioneId={}, startTime={}, endTime={}, frequency={}",
        eServiceToUpdate.eserviceId(), eServiceToUpdate.versionId(),
        eServiceToUpdate.pollingStartTime(), eServiceToUpdate.pollingEndTime(),
        eServiceToUpdate.pollingFrequency());
  }

  @Override
  public void logMessageSearchEservice(Integer limit, Integer offset, String eserviceName,
      String producerName, Integer versionNumber, List<EserviceMonitorState> state) {
    log.info(
        "Searching e-services, limit={}, offset={}, producerName={}, eserviceName={}, versionNumber={}, stateList={}",
        limit, offset, producerName, eserviceName, versionNumber, Arrays.toString(state.toArray()));
  }

  @Override
  public void logMessageSearchProducer(String producerName) {
    log.info("Searching producer, producerName={}", producerName);
  }

  @Override
  public void logMessageEserviceSaved(Eservice eServiceToUpdate) {
    log.info("e-service has been saved. eserviceId={}, versionId={}", eServiceToUpdate.eserviceId(),
        eServiceToUpdate.versionId());
  }

  @Override
  public void logMessageException(Exception exception) {
    log.error(ExceptionUtils.getStackTrace(exception));
  }
}