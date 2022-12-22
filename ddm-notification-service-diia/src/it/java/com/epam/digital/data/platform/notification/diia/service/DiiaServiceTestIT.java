/*
 * Copyright 2022 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.notification.diia.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.epam.digital.data.platform.notification.diia.BaseIT;
import com.epam.digital.data.platform.notification.dto.NotificationContextDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaNotificationDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaRecipientDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaRecipientDto.KeyValue;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class DiiaServiceTestIT extends BaseIT {

  @Autowired
  DiiaService service;
  @Autowired
  WireMockServer mockDiiaServer;

    @Test
  void shouldSendNotification() {
      mockDiiaServer.
          stubFor(get(urlEqualTo("/api/v1/auth/partner/partnerToken"))
              .willReturn(aResponse()
                  .withStatus(200)
                  .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                  .withBody("{\"token\":\"diia_access_token\"}")
          ));
      
      mockDiiaServer.stubFor(
          post(urlEqualTo("/api/v1/notification/distribution/push"))
              .willReturn(aResponse()
                  .withStatus(200)
                  .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                  .withBody("{\"distributionId\":\"currentDistributionId\"}")));
    
      service.notify(createMessage());

      mockDiiaServer.verify(1,
          getRequestedFor(urlEqualTo("/api/v1/auth/partner/partnerToken")));
      mockDiiaServer.verify(1,
          postRequestedFor(urlEqualTo("/api/v1/notification/distribution/push"))
              .withHeader("Authorization", equalTo("Bearer diia_access_token"))
              .withHeader("Content-Type", equalTo("application/json"))
              .withRequestBody(
                  equalTo("{\"templateId\":\"externalTemplateId\",\"recipients\":" 
                      + "[{\"rnokpp\":\"rnokpp\",\"id\":\"keycloak-username\",\"parameters\":" 
                      + "[{\"key\":\"some-key\",\"value\":\"some-value\"}]}]}")));
  }
  
  private DiiaNotificationMessageDto createMessage() {
      return DiiaNotificationMessageDto.builder()
          .context(NotificationContextDto.builder()
              .application("appName")
              .system("LowCode")
              .build())
          .diiaNotificationDto(DiiaNotificationDto.builder()
              .templateName("templateName")
              .externalTemplateId("externalTemplateId")
              .build())
          .recipient(DiiaRecipientDto.builder()
              .rnokpp("rnokpp")
              .id("keycloak-username")
              .parameters(List.of(new KeyValue("some-key", "some-value")))
              .build())
          .build();
  }
}
