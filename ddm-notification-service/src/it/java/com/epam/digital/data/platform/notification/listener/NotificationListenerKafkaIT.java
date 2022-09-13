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

package com.epam.digital.data.platform.notification.listener;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.digital.data.platform.notification.BaseKafkaIT;
import com.epam.digital.data.platform.notification.audit.NotificationAuditFacade;
import com.epam.digital.data.platform.notification.dto.NotificationContextDto;
import com.epam.digital.data.platform.notification.dto.NotificationDto;
import com.epam.digital.data.platform.notification.dto.NotificationRecordDto;
import com.epam.digital.data.platform.notification.repository.NotificationTemplateRepository;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.icegreen.greenmail.util.GreenMailUtil;
import java.util.Map;
import org.assertj.core.api.AssertionsForClassTypes;
import org.awaitility.Durations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.SpyBean;

class NotificationListenerKafkaIT extends BaseKafkaIT {

  @Value("${keycloak.system-user.realm}")
  private String realm;
  @Autowired
  private NotificationTemplateRepository repository;
  @Autowired
  private WireMockServer userSettingsWireMock;
  @Autowired
  private WireMockServer keycloakWireMock;
  @SpyBean
  private NotificationListener listener;
  @SpyBean
  private NotificationAuditFacade auditFacade;

  @Captor
  private ArgumentCaptor<NotificationRecordDto> recordCaptor;

  @BeforeEach
  void setup() {
    keycloakWireMock.stubFor(
        post(String.format("/auth/realms/%s/protocol/openid-connect/token", realm))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBody(jsonToStr("/json/keycloakTokenResponse.json"))));
    keycloakWireMock.stubFor(
        get(urlPathEqualTo(String.format("/auth/admin/realms/%s/users", realm)))
            .withQueryParam("username", equalTo("testuser"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBody(jsonToStr("/json/keycloakUserByUsernameResponse.json"))));
  }

  @Test
  void shouldReceiveNotificationRecord() {
    createEmailTemplateInDb("template-id", "<html>Hello ${name}!</html>");
    stubUserSettings("3fa85f64-1234-4562-b3fc-2c963f66afa6", "/json/userSettingsResponse.json");
    var notificationRecord = createNotificationRecord();
    var topic = kafkaProperties.getTopics().get("user-notifications");

    kafkaTemplate.send(topic, notificationRecord);

    await().atMost(Durations.TEN_SECONDS).untilAsserted(() -> {
      verify(listener, times(1)).notify(recordCaptor.capture());
      var record = recordCaptor.getValue();
      assertThat(record).isNotNull();
      assertThat(record).isEqualTo(notificationRecord);

      var receivedMessages = greenMail.getReceivedMessages();
      AssertionsForClassTypes.assertThat(receivedMessages).isNotEmpty();
      var receivedMessage = receivedMessages[0];
      AssertionsForClassTypes.assertThat(GreenMailUtil.getBody(receivedMessage))
          .isEqualTo("<html>Hello John!</html>");
      AssertionsForClassTypes.assertThat(receivedMessage.getSubject())
          .isEqualTo("sign notification");
      AssertionsForClassTypes.assertThat(receivedMessage.getAllRecipients()[0].toString())
          .isEqualTo("test@test.com");

      verify(auditFacade, times(1)).sendAuditOnSuccess(Channel.EMAIL,
          notificationRecord.getNotification());
    });
  }

  private NotificationRecordDto createNotificationRecord() {
    return NotificationRecordDto.builder()
        .context(NotificationContextDto.builder()
            .system("ddm-platform")
            .application("bpms")
            .businessActivity("Activity_1")
            .businessActivityInstanceId("e2503352-bcb2-11ec-b217-0a580a831053")
            .businessProcess("add-lab")
            .businessProcessDefinitionId("add-lab:5:ac2dfa60-bbe2-11ec-8421-0a58")
            .businessProcessInstanceId("e2503352-bcb2-11ec-b217-0a580a831054")
            .build())
        .notification(NotificationDto.builder()
            .recipient("testuser")
            .subject("sign notification")
            .template("template-id")
            .templateModel(Map.of("name", "John"))
            .build())
        .build();
  }

  private void stubUserSettings(String keycloakId, String jsonPath) {
    userSettingsWireMock.stubFor(
        get(urlPathEqualTo(String.format("/api/settings/%s", keycloakId)))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBody(jsonToStr(jsonPath))));
  }
}