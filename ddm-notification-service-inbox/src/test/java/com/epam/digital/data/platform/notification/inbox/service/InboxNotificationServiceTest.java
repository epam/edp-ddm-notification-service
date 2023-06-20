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

package com.epam.digital.data.platform.notification.inbox.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.integration.idm.model.IdmUser;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import com.epam.digital.data.platform.notification.core.service.IdmServiceProvider;
import com.epam.digital.data.platform.notification.core.template.FreemarkerTemplateResolver;
import com.epam.digital.data.platform.notification.dto.Recipient.RecipientRealm;
import com.epam.digital.data.platform.notification.dto.audit.NotificationDto;
import com.epam.digital.data.platform.notification.dto.inbox.InboxNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.inbox.InboxNotificationResponseDto;
import com.epam.digital.data.platform.notification.entity.InboxNotification;
import com.epam.digital.data.platform.notification.exception.ForbiddenNotificationActionException;
import com.epam.digital.data.platform.notification.inbox.repository.InboxNotificationRepository;
import com.epam.digital.data.platform.notification.model.JwtClaims;
import com.epam.digital.data.platform.notification.template.NotificationTemplateService;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class InboxNotificationServiceTest {

  static final UUID ID = UUID.fromString("10e23e2a-6830-42a6-bf21-d0a4a90b5706");

  @Mock
  private NotificationTemplateService<String> templateService;
  @Mock
  private FreemarkerTemplateResolver templateResolver;
  @Mock
  private InboxNotificationRepository inboxNotificationRepository;
  @Mock
  private TokenParserService tokenParserService;
  @Mock
  private IdmService idmService;
  @Mock
  IdmServiceProvider idmServiceProvider;
  @InjectMocks
  private InboxNotificationService service;

  @Test
  void testNotify() {
    var subject = "subject";
    var recipientId = "recipientId";
    var message = "message";
    var notification = NotificationDto.builder().subject(subject)
        .message(message).build();
    var msgDto = InboxNotificationMessageDto.builder()
        .notification(notification)
        .recipientName(recipientId)
        .recipientRealm(RecipientRealm.CITIZEN)
        .build();
    when(idmServiceProvider.getIdmService(RecipientRealm.CITIZEN)).thenReturn(idmService);
    when(idmService.getUserByUserName("recipientId"))
        .thenReturn(List.of(
            IdmUser.builder()
                .id("recipientId")
                .build()));

    service.notify(msgDto);

    verify(inboxNotificationRepository, times(1))
        .save(InboxNotification.builder()
            .recipientId(recipientId)
            .subject(subject)
            .message(message)
            .build());
  }

  @Test
  void prepareInboxBody() {
    var data = new HashMap<String, Object>();
    when(templateService.getContentByNameAndChannel("name", Channel.INBOX)).thenReturn("content");

    service.prepareInboxBody("name", Map.of());

    verify(templateService, times(1)).getContentByNameAndChannel("name", Channel.INBOX);
    verify(templateResolver, times(1)).resolve("name", "content", data);
  }


  @Test
  void validAcknowledgeNotification() {
    var inboxNotification = InboxNotification.builder()
        .id(ID)
        .recipientId("recipient-id")
        .build();
    when(tokenParserService.parseClaims("userToken")).thenReturn(new JwtClaims("recipient-id"));
    when(inboxNotificationRepository.findById(ID)).thenReturn(Optional.of(inboxNotification));

    service.acknowledgeNotification(ID, "userToken");

    assertTrue(inboxNotification.isAcknowledged());
    verify(inboxNotificationRepository).save(inboxNotification);
  }

  @Test
  void acknowledgeNotificationShouldDoNotingWithAbsentNotificationById() {
    service.acknowledgeNotification(ID, "userToken");

    verify(inboxNotificationRepository, never()).save(any());
  }

  @Test
  void shouldThrowForbiddenNotificationActionException() {
    var inboxNotification = InboxNotification.builder()
        .id(ID)
        .recipientId("recipient-id")
        .build();
    when(tokenParserService.parseClaims("userToken")).thenReturn(
        new JwtClaims("another-recipient-id"));
    when(inboxNotificationRepository.findById(ID)).thenReturn(Optional.of(inboxNotification));

    var exception = assertThrows(ForbiddenNotificationActionException.class,
        () -> service.acknowledgeNotification(ID, "userToken"));

    assertThat(exception.getMessage()).isEqualTo("Forbidden. Notification state can't be updated.");
  }

  @Test
  void validGetInboxNotifications() {
    when(tokenParserService.parseClaims("userToken")).thenReturn(new JwtClaims("recipient-id"));
    when(inboxNotificationRepository.findByRecipientId(eq("recipient-id"), any()))
        .thenReturn(getInboxNotificationEntities());

    List<InboxNotificationResponseDto> inboxNotifications = service.getInboxNotifications(
        "userToken",
        PageRequest.of(1, 1));

    assertEquals(getInboxNotificationResponseDto(), inboxNotifications);
  }

  private List<InboxNotification> getInboxNotificationEntities() {
    return List.of(
        InboxNotification.builder()
            .id(ID)
            .recipientId("recipient-id")
            .build(),
        InboxNotification.builder()
            .id(UUID.fromString("06209610-91d4-4416-af01-d078e5f32294"))
            .recipientId("recipient-id")
            .build());
  }

  private List<InboxNotificationResponseDto> getInboxNotificationResponseDto() {
    return List.of(
        InboxNotificationResponseDto.builder()
            .id(ID)
            .build(),
        InboxNotificationResponseDto.builder()
            .id(UUID.fromString("06209610-91d4-4416-af01-d078e5f32294"))
            .build()
    );
  }
}