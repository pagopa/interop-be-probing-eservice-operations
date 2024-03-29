package it.pagopa.interop.probing.eservice.operations.model;

import java.io.Serializable;
import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import it.pagopa.interop.probing.eservice.operations.dtos.EserviceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity
@Table(name = "eservice_probing_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true, fluent = true)
public class EserviceProbingResponse implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  private long eserviceRecordId;

  @Column(name = "response_received", columnDefinition = "TIMESTAMP WITH TIME ZONE")
  @NotNull(message = "must not be null")
  private OffsetDateTime responseReceived;

  @NotNull(message = "must not be null")
  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private EserviceStatus responseStatus;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "eservices_record_id")
  private Eservice eservice;

}
