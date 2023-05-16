package it.pagopa.interop.probing.eservice.operations.repository.query.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import it.pagopa.interop.probing.eservice.operations.model.view.EserviceView;
import it.pagopa.interop.probing.eservice.operations.model.view.EserviceView_;

@Repository
public class EserviceViewQueryBuilder {

  @PersistenceContext
  private EntityManager entityManager;

  public Page<EserviceView> findAllWithoutNDState(Integer limit, Integer offset,
      String eserviceName, String producerName, Integer versionNumber, List<String> stateList,
      int minOfTolleranceMultiplier) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<EserviceView> query = cb.createQuery(EserviceView.class);
    Root<EserviceView> root = query.from(EserviceView.class);
    query.distinct(true).select(root);

    Predicate predicate = buildQueryWithoutNDState(cb, root, eserviceName, producerName,
        versionNumber, stateList, minOfTolleranceMultiplier);

    query.where(predicate);
    TypedQuery<EserviceView> q = entityManager.createQuery(query);
    List<EserviceView> content = q.getResultList();
    return new PageImpl<>(content, getPageRequest(limit, offset), content.size());
  }

  public Predicate buildQueryEserviceNameProducerNameVersionNumberEquals(CriteriaBuilder cb,
      Root<EserviceView> root, String eserviceName, String producerName, Integer versionNumber) {
    List<Predicate> predicates = new ArrayList<>();
    if (Objects.nonNull(eserviceName)) {
      predicates.add(cb.equal(root.get(EserviceView_.ESERVICE_NAME), eserviceName));
    }
    if (Objects.nonNull(producerName)) {
      predicates.add(cb.equal(root.get(EserviceView_.PRODUCER_NAME), producerName));
    }
    if (Objects.nonNull(versionNumber)) {
      predicates.add(cb.equal(root.get(EserviceView_.VERSION_NUMBER), versionNumber));
    }
    return cb.and(predicates.toArray(new Predicate[] {}));
  }

  private Predicate buildQueryWithoutNDState(CriteriaBuilder cb, Root<EserviceView> root,
      String eserviceName, String producerName, Integer versionNumber, List<String> stateList,
      int minOfTolleranceMultiplier) {
    Expression<Integer> extractMinute =
        cb.function("extract_minute", Integer.class, root.get(EserviceView_.LAST_REQUEST));
    return cb.and(
        buildQueryEserviceNameProducerNameVersionNumberEquals(cb, root, eserviceName, producerName,
            versionNumber),
        buildProbingEnabledPredicate(cb, root, stateList, extractMinute,
            minOfTolleranceMultiplier));
  }

  public Page<EserviceView> findAllWithNDState(Integer limit, Integer offset, String eserviceName,
      String producerName, Integer versionNumber, List<String> stateList,
      int minOfTolleranceMultiplier) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<EserviceView> query = cb.createQuery(EserviceView.class);
    Root<EserviceView> root = query.from(EserviceView.class);

    Predicate predicate = buildPredicate(cb, root, eserviceName, producerName, versionNumber,
        stateList, minOfTolleranceMultiplier);

    query.where(cb.or(predicate));
    TypedQuery<EserviceView> q = entityManager.createQuery(query);
    List<EserviceView> content = q.getResultList();
    return new PageImpl<>(content, getPageRequest(limit, offset), content.size());
  }

  private Predicate buildPredicate(CriteriaBuilder cb, Root<EserviceView> root, String eserviceName,
      String producerName, Integer versionNumber, List<String> stateList,
      int minOfTolleranceMultiplier) {
    Expression<Integer> extractMinute =
        cb.function("extract_minute", Integer.class, root.get(EserviceView_.LAST_REQUEST));
    return cb.and(
        buildQueryEserviceNameProducerNameVersionNumberEquals(cb, root, eserviceName, producerName,
            versionNumber),
        cb.or(
            buildProbingEnabledPredicate(cb, root, stateList, extractMinute,
                minOfTolleranceMultiplier),
            buildProbingDisabledPredicate(cb, root, extractMinute)));
  }

  private Predicate buildProbingEnabledPredicate(CriteriaBuilder cb, Root<EserviceView> root,
      List<String> stateList, Expression<Integer> extractMinute, int minOfTolleranceMultiplier) {
    return cb.and(root.get(EserviceView_.STATE).as(String.class).in(stateList),
        cb.isTrue(root.get(EserviceView_.PROBING_ENABLED)),
        cb.isNotNull(root.get(EserviceView_.LAST_REQUEST)),
        cb.or(
            cb.lessThan(extractMinute,
                cb.prod(root.get(EserviceView_.POLLING_FREQUENCY), minOfTolleranceMultiplier)),
            cb.greaterThan(root.get(EserviceView_.RESPONSE_RECEIVED),
                root.get(EserviceView_.LAST_REQUEST))),
        cb.isNotNull(root.get(EserviceView_.RESPONSE_RECEIVED)));
  }

  private Predicate buildProbingDisabledPredicate(CriteriaBuilder cb, Root<EserviceView> root,
      Expression<Integer> extractMinute) {
    return cb.or(cb.isFalse(root.get(EserviceView_.PROBING_ENABLED)),
        cb.isNull(root.get(EserviceView_.LAST_REQUEST)),
        cb.and(cb.greaterThan(extractMinute, root.get(EserviceView_.POLLING_FREQUENCY)),
            cb.lessThan(root.get(EserviceView_.RESPONSE_RECEIVED),
                root.get(EserviceView_.LAST_REQUEST))),
        cb.isNull(root.get(EserviceView_.RESPONSE_RECEIVED)));
  }

  private PageRequest getPageRequest(Integer limit, Integer offset) {
    return PageRequest.of(offset, limit, Sort.by(EserviceView_.ESERVICE_NAME).ascending());
  }
}
