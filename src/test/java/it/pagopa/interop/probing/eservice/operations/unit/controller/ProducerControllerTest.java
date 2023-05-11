package it.pagopa.interop.probing.eservice.operations.unit.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import it.pagopa.interop.probing.eservice.operations.dtos.Producer;
import it.pagopa.interop.probing.eservice.operations.service.ProducerService;

@SpringBootTest
@AutoConfigureMockMvc
class ProducerControllerTest {


  @Value("${api.eservices.producers.url}")
  private String apiGetEservicesProducersUrl;

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ProducerService service;

  private List<Producer> producerExpectedList;

  @Test
  @DisplayName("given a valid producer name, then returns a non-empty list")
  void testGetEservicesProducers_whenGivenValidProducerName_thenReturnsSearchProducerNameResponseList()
      throws Exception {
    Producer producer = Producer.builder().build();

    producerExpectedList = List.of(producer);
    Mockito.when(service.getEservicesProducers(2, 0, "ProducerName-Test"))
        .thenReturn(producerExpectedList);
    MockHttpServletResponse response = mockMvc
        .perform(get(apiGetEservicesProducersUrl)
            .params(getMockRequestParamsGetEservicesProducers("2", "0", "ProducerName-Test")))
        .andReturn().getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.getContentAsString()).isNotEmpty();
    assertThat(response.getContentAsString()).contains("label");
    assertThat(response.getContentAsString()).contains("value");
  }

  @Test
  @DisplayName("given a valid producer name with no matching records, then returns an empty list")
  void testGetEservicesProducers_whenGivenValidProducerName_thenReturnsSearchProducerNameResponseListEmpty()
      throws Exception {
    Mockito.when(service.getEservicesProducers(2, 0, "ProducerName-Test"))
        .thenReturn(new ArrayList<Producer>());
    MockHttpServletResponse response = mockMvc
        .perform(get(apiGetEservicesProducersUrl)
            .params(getMockRequestParamsGetEservicesProducers("2", "0", "ProducerName-Test")))
        .andReturn().getResponse();
    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.getContentAsString().equals("[]"));
  }

  private LinkedMultiValueMap<String, String> getMockRequestParamsGetEservicesProducers(
      String limit, String offset, String producerName) {
    LinkedMultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
    requestParams.add("offset", offset);
    requestParams.add("limit", limit);
    requestParams.add("producerName", producerName);
    return requestParams;
  }

}
