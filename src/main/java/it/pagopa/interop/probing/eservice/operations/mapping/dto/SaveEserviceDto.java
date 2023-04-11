package it.pagopa.interop.probing.eservice.operations.mapping.dto;

import java.util.UUID;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.pagopa.interop.probing.eservice.operations.dtos.EserviceStateBE;
import it.pagopa.interop.probing.eservice.operations.dtos.EserviceTechnology;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Builder
public class SaveEserviceDto {

  @NotBlank
  @JsonProperty("name")
  private String name;

  @NotBlank
  @JsonProperty("producerName")
  private String producerName;

  @NotEmpty
  @JsonProperty("basePath")
  private String[] basePath;

  @NotNull
  @JsonProperty("technology")
  private EserviceTechnology technology;

  @NotNull
  @JsonProperty("eserviceId")
  private UUID eserviceId;

  @NotNull
  @JsonProperty("versionId")
  private UUID versionId;

  @NotNull
  @JsonProperty("state")
  private EserviceStateBE state;

  @NotBlank
  @JsonProperty("versionNumber")
  private Integer versionNumber;
}