/*
 * Copyright 2023 EPAM Systems.
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
import com.epam.digital.data.platform.notification.audit.UserNotificationAuditFacade;
import com.epam.digital.data.platform.notification.core.repository.CoreNotificationTemplateRepository;
import com.epam.digital.data.platform.notification.dto.NotificationContextDto;
import com.epam.digital.data.platform.notification.dto.Recipient;
import com.epam.digital.data.platform.notification.dto.Recipient.RecipientRealm;
import com.epam.digital.data.platform.notification.dto.UserNotificationDto;
import com.epam.digital.data.platform.notification.dto.UserNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.email.EmailNotificationMessageDto;
import com.epam.digital.data.platform.notification.email.audit.EmailNotificationAuditFacade;
import com.epam.digital.data.platform.notification.email.listener.EmailNotificationListener;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.epam.digital.data.platform.starter.audit.model.Step;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.icegreen.greenmail.util.GreenMailUtil;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.AssertionsForClassTypes;
import org.awaitility.Durations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.SpyBean;

class UserNotificationListenerKafkaIT extends BaseKafkaIT {

  @Value("${keycloak.citizen-user.realm}")
  private String realm;
  @Autowired
  private CoreNotificationTemplateRepository repository;
  @Autowired
  private WireMockServer userSettingsWireMock;
  @Autowired
  private WireMockServer keycloakWireMock;
  @SpyBean
  private UserNotificationListener listener;
  @SpyBean
  private EmailNotificationListener emailNotificationListener;
  @SpyBean
  private EmailNotificationAuditFacade emailNotificationAuditFacade;
  @SpyBean
  private UserNotificationAuditFacade userNotificationAuditFacade;

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
    keycloakWireMock.stubFor(
        get(urlPathEqualTo(String.format(
            "/auth/admin/realms/%s/users/3fa85f64-1234-4562-b3fc-2c963f66afa6/role-mappings/realm",
            realm)))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBody(jsonToStr("/json/keycloakUsersRolesResponse.json"))));
  }

  @Test
  void testEmailNotification() {
    var userNotificationMessageCaptor = ArgumentCaptor.forClass(UserNotificationMessageDto.class);
    var emailNotificationMessageCaptor = ArgumentCaptor.forClass(EmailNotificationMessageDto.class);
    createEmailTemplateInDb("template-id", "<html>Hello ${name}!</html>");
    stubUserSettings("3fa85f64-1234-4562-b3fc-2c963f66afa6", "/json/userSettingsResponse.json");
    var userNotificationMessageDto = createMessage();
    var topic = kafkaProperties.getTopics().get("user-notifications");

    kafkaTemplate.send(topic, userNotificationMessageDto);

    await().atMost(Durations.TEN_SECONDS).untilAsserted(() -> {
      verify(listener, times(1)).notify(userNotificationMessageCaptor.capture());
      var userNotificationMessage = userNotificationMessageCaptor.getValue();
      assertThat(userNotificationMessage).isNotNull();
      assertThat(userNotificationMessage).isEqualTo(userNotificationMessageCaptor.getValue());

      verify(emailNotificationListener, times(1)).notify(emailNotificationMessageCaptor.capture());
      var emailNotificationMessage = emailNotificationMessageCaptor.getValue();
      assertThat(emailNotificationMessage).isNotNull();
      assertThat(emailNotificationMessage.getNotification().getMessage()).isNotNull();
      assertThat(emailNotificationMessage.getNotification().getSubject()).isEqualTo("sign notification");
      assertThat(emailNotificationMessage.getRecipient().getId()).isEqualTo("testuser");
      assertThat(emailNotificationMessage.getRecipient().getEmail()).isEqualTo("test@test.com");

      var receivedMessages = greenMail.getReceivedMessages();
      AssertionsForClassTypes.assertThat(receivedMessages).isNotEmpty();
      var receivedMessage = receivedMessages[0];
      AssertionsForClassTypes.assertThat(GreenMailUtil.getBody(receivedMessage))
          .isEqualTo("<html>Hello John!</html>");
      AssertionsForClassTypes.assertThat(receivedMessage.getSubject())
          .isEqualTo("sign notification");
      AssertionsForClassTypes.assertThat(receivedMessage.getAllRecipients()[0].toString())
          .isEqualTo("test@test.com");

      verify(emailNotificationAuditFacade, times(1)).sendAuditOnSuccess(Channel.EMAIL,
          emailNotificationMessage);
      verify(userNotificationAuditFacade, times(1)).sendAuditOnFailure(Channel.INBOX,
          userNotificationMessage, Step.AFTER, "Notification template template-id not found");
    });
  }

  private UserNotificationMessageDto createMessage() {
    return UserNotificationMessageDto.builder()
        .context(NotificationContextDto.builder()
            .system("ddm-platform")
            .application("bpms")
            .businessActivity("Activity_1")
            .businessActivityInstanceId("e2503352-bcb2-11ec-b217-0a580a831053")
            .businessProcess("add-lab")
            .businessProcessDefinitionId("add-lab:5:ac2dfa60-bbe2-11ec-8421-0a58")
            .businessProcessInstanceId("e2503352-bcb2-11ec-b217-0a580a831054")
            .build())
        .notification(UserNotificationDto.builder()
            .title("sign notification")
            .ignoreChannelPreferences(false)
            .templateName("template-id")
            .build())
        .recipients(List.of(
            Recipient.builder()
                .id("testuser")
                .realm(RecipientRealm.CITIZEN)
                .parameters(Map.of("name", "John"))
                .build()))
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