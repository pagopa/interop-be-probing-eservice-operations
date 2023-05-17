package it.pagopa.interop.probing.eservice.operations.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import it.pagopa.interop.probing.eservice.operations.dtos.EserviceContent;
import it.pagopa.interop.probing.eservice.operations.dtos.EserviceMonitorState;
import it.pagopa.interop.probing.eservice.operations.dtos.MainDataEserviceResponse;
import it.pagopa.interop.probing.eservice.operations.dtos.PollingEserviceResponse;
import it.pagopa.interop.probing.eservice.operations.dtos.ProbingDataEserviceResponse;
import it.pagopa.interop.probing.eservice.operations.dtos.SearchEserviceResponse;
import it.pagopa.interop.probing.eservice.operations.exception.EserviceNotFoundException;
import it.pagopa.interop.probing.eservice.operations.mapping.dto.SaveEserviceDto;
import it.pagopa.interop.probing.eservice.operations.mapping.dto.UpdateEserviceFrequencyDto;
import it.pagopa.interop.probing.eservice.operations.mapping.dto.UpdateEserviceLastRequestDto;
import it.pagopa.interop.probing.eservice.operations.mapping.dto.UpdateEserviceProbingStateDto;
import it.pagopa.interop.probing.eservice.operations.mapping.dto.UpdateEserviceResponseReceivedDto;
import it.pagopa.interop.probing.eservice.operations.mapping.dto.UpdateEserviceStateDto;
import it.pagopa.interop.probing.eservice.operations.mapping.mapper.AbstractMapper;
import it.pagopa.interop.probing.eservice.operations.model.Eservice;
import it.pagopa.interop.probing.eservice.operations.model.EserviceProbingRequest;
import it.pagopa.interop.probing.eservice.operations.model.EserviceProbingResponse;
import it.pagopa.interop.probing.eservice.operations.model.Eservice_;
import it.pagopa.interop.probing.eservice.operations.model.view.EserviceView;
import it.pagopa.interop.probing.eservice.operations.repository.EserviceProbingRequestRepository;
import it.pagopa.interop.probing.eservice.operations.repository.EserviceProbingResponseRepository;
import it.pagopa.interop.probing.eservice.operations.repository.EserviceRepository;
import it.pagopa.interop.probing.eservice.operations.repository.EserviceViewRepository;
import it.pagopa.interop.probing.eservice.operations.repository.query.builder.EserviceContentQueryBuilder;
import it.pagopa.interop.probing.eservice.operations.repository.query.builder.EserviceViewQueryBuilder;
import it.pagopa.interop.probing.eservice.operations.repository.specs.EserviceViewSpecs;
import it.pagopa.interop.probing.eservice.operations.service.EserviceService;
import it.pagopa.interop.probing.eservice.operations.util.EnumUtilities;
import it.pagopa.interop.probing.eservice.operations.util.OffsetLimitPageable;
import it.pagopa.interop.probing.eservice.operations.util.constant.ErrorMessages;
import it.pagopa.interop.probing.eservice.operations.util.logging.Logger;

@Service
public class EserviceServiceImpl implements EserviceService {

  @Autowired
  private EnumUtilities enumUtilities;

  @Autowired
  private Logger logger;

  @Autowired
  private EserviceRepository eserviceRepository;

  @Autowired
  private EserviceViewRepository eserviceViewRepository;

  @Autowired
  private EserviceProbingRequestRepository eserviceProbingRequestRepository;

  @Autowired
  private EserviceProbingResponseRepository eserviceProbingResponseRepository;

  @Autowired
  private EserviceViewQueryBuilder eserviceViewQueryBuilder;

  @Autowired
  private EserviceContentQueryBuilder eserviceContentQueryBuilder;

  @Autowired
  private AbstractMapper mapper;

  @Value("${toleranceMultiplierInMinutes}")
  private int toleranceMultiplierInMinutes;

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public void updateEserviceState(UpdateEserviceStateDto inputData)
      throws EserviceNotFoundException {
    Optional<Eservice> queryResult = eserviceRepository
        .findByEserviceIdAndVersionId(inputData.getEserviceId(), inputData.getVersionId());

    Eservice eServiceToUpdate = queryResult
        .orElseThrow(() -> new EserviceNotFoundException(ErrorMessages.ELEMENT_NOT_FOUND));

    eServiceToUpdate.state(inputData.getNewEServiceState());
    eserviceRepository.save(eServiceToUpdate);
    logger.logMessageEserviceStateUpdated(eServiceToUpdate);
  }

  @Override
  public void updateEserviceProbingState(UpdateEserviceProbingStateDto inputData)
      throws EserviceNotFoundException {

    Optional<Eservice> queryResult = eserviceRepository
        .findByEserviceIdAndVersionId(inputData.getEserviceId(), inputData.getVersionId());

    Eservice eServiceToUpdate = queryResult
        .orElseThrow(() -> new EserviceNotFoundException(ErrorMessages.ELEMENT_NOT_FOUND));

    eServiceToUpdate.probingEnabled(inputData.isProbingEnabled());
    eserviceRepository.save(eServiceToUpdate);
    logger.logMessageEserviceProbingStateUpdated(eServiceToUpdate);
  }

  @Override
  public void updateEserviceFrequency(UpdateEserviceFrequencyDto inputData)
      throws EserviceNotFoundException {

    Optional<Eservice> queryResult = eserviceRepository
        .findByEserviceIdAndVersionId(inputData.getEserviceId(), inputData.getVersionId());

    Eservice eServiceToUpdate = queryResult
        .orElseThrow(() -> new EserviceNotFoundException(ErrorMessages.ELEMENT_NOT_FOUND));

    eServiceToUpdate.pollingFrequency(inputData.getNewPollingFrequency())
        .pollingStartTime(inputData.getNewPollingStartTime())
        .pollingEndTime(inputData.getNewPollingEndTime());
    eserviceRepository.save(eServiceToUpdate);
    logger.logMessageEservicePollingConfigUpdated(eServiceToUpdate);
  }

  @Override
  public SearchEserviceResponse searchEservices(Integer limit, Integer offset, String eserviceName,
      String producerName, Integer versionNumber, List<EserviceMonitorState> state) {

    logger.logMessageSearchEservice(limit, offset, eserviceName, producerName, versionNumber,
        state);

    Page<EserviceView> eserviceViewPagable;
    List<String> stateBE = Objects.isNull(state) || state.isEmpty() ? List.of()
        : enumUtilities.convertListFromMonitorToPdnd(state);

    if (Objects.isNull(state) || state.isEmpty()
        || (state.contains(EserviceMonitorState.N_D) && state.contains(EserviceMonitorState.ONLINE)
            && state.contains(EserviceMonitorState.OFFLINE))) {
      eserviceViewPagable = eserviceViewRepository.findAll(
          EserviceViewSpecs.searchSpecBuilder(eserviceName, producerName, versionNumber),
          new OffsetLimitPageable(offset, limit, Sort.by(Eservice_.ESERVICE_NAME).ascending()));
    } else if (state.contains(EserviceMonitorState.N_D)) {
      eserviceViewPagable = eserviceViewQueryBuilder.findAllWithNDState(limit, offset, eserviceName,
          producerName, versionNumber, stateBE, toleranceMultiplierInMinutes);
    } else {
      eserviceViewPagable = eserviceViewQueryBuilder.findAllWithoutNDState(limit, offset,
          eserviceName, producerName, versionNumber, stateBE, toleranceMultiplierInMinutes);
    }

    List<EserviceContent> eserviceContentList = eserviceViewPagable.getContent().stream()
        .map(e -> mapper.toSearchEserviceContent(e)).toList();

    return SearchEserviceResponse.builder().content(eserviceContentList)
        .offset(eserviceViewPagable.getNumber()).limit(eserviceViewPagable.getSize())
        .totalElements(eserviceViewPagable.getTotalElements()).build();
  }

  @Override
  public Long saveEservice(SaveEserviceDto inputData) {
    Eservice eServiceToUpdate = eserviceRepository
        .findByEserviceIdAndVersionId(inputData.getEserviceId(), inputData.getVersionId())
        .orElseGet(() -> Eservice.builder().eserviceId(inputData.getEserviceId())
            .versionId(inputData.getVersionId()).lockVersion(1)
            .versionNumber(inputData.getVersionNumber()).build());

    eServiceToUpdate.eserviceName(inputData.getName()).producerName(inputData.getProducerName())
        .basePath(inputData.getBasePath()).technology(inputData.getTechnology())
        .state(inputData.getState());

    Long id = eserviceRepository.save(eServiceToUpdate).eserviceRecordId();

    logger.logMessageEserviceSaved(eServiceToUpdate);

    return id;
  }

  @Override
  public PollingEserviceResponse getEservicesReadyForPolling(Integer limit, Integer offset) {

    logger.logMessageEserviceReadyForPolling(limit, offset);

    Page<EserviceContent> pollingActiveEservicePagable =
        eserviceContentQueryBuilder.findAllEservicesReadyForPolling(limit, offset);

    return PollingEserviceResponse.builder().content(pollingActiveEservicePagable.getContent())
        .totalElements(pollingActiveEservicePagable.getTotalElements()).build();
  }

  @Override
  public void updateLastRequest(UpdateEserviceLastRequestDto inputData)
      throws EserviceNotFoundException {
    Optional<EserviceProbingRequest> queryResult =
        eserviceProbingRequestRepository.findById(inputData.getEserviceRecordId());

    EserviceProbingRequest eServiceToUpdate = queryResult.orElseGet(() -> {
      Optional<Eservice> e = eserviceRepository.findById(inputData.getEserviceRecordId());

      return EserviceProbingRequest.builder().eservice(e.get()).build();
    });

    eServiceToUpdate.lastRequest(inputData.getLastRequest());

    eserviceProbingRequestRepository.save(eServiceToUpdate);
    logger.logMessageLastRequestUpdated(eServiceToUpdate);
  }

  @Override
  public void updateResponseReceived(UpdateEserviceResponseReceivedDto inputData)
      throws EserviceNotFoundException {
    Optional<EserviceProbingResponse> queryResult =
        eserviceProbingResponseRepository.findById(inputData.getEserviceRecordId());

    EserviceProbingResponse eserviceToUpdate = queryResult.orElseGet(() -> {
      Optional<Eservice> e = eserviceRepository.findById(inputData.getEserviceRecordId());

      return EserviceProbingResponse.builder().eservice(e.get()).build();
    });

    eserviceToUpdate.responseReceived(inputData.getResponseReceived());
    eserviceToUpdate.responseStatus(inputData.getStatus());

    eserviceProbingResponseRepository.save(eserviceToUpdate);
    logger.logMessageResponseReceivedUpdated(eserviceToUpdate);
  }

  @Override
  public MainDataEserviceResponse getEserviceMainData(Long eserviceRecordId)
      throws EserviceNotFoundException {
    logger.logMessageEserviceMainData(eserviceRecordId);
    Eservice eService = eserviceRepository.findById(eserviceRecordId)
        .orElseThrow(() -> new EserviceNotFoundException(ErrorMessages.ELEMENT_NOT_FOUND));
    return MainDataEserviceResponse.builder().eserviceName(eService.eserviceName())
        .versionNumber(eService.versionNumber()).producerName(eService.producerName()).build();
  }

  @Override
  public ProbingDataEserviceResponse getEserviceProbingData(Long eserviceRecordId)
      throws EserviceNotFoundException {
    logger.logMessageGetEserviceProbingData(eserviceRecordId);
    EserviceView eServiceView = eserviceViewRepository.findById(eserviceRecordId)
        .orElseThrow(() -> new EserviceNotFoundException(ErrorMessages.ELEMENT_NOT_FOUND));
    return mapper.toProbingDataEserviceResponse(eServiceView);
  }

}
