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

package com.epam.digital.data.platform.notification.email.audit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.digital.data.platform.notification.dto.audit.DeliveryAuditDto;
import com.epam.digital.data.platform.notification.dto.audit.NotificationAuditDto;
import com.epam.digital.data.platform.notification.dto.email.EmailNotificationDto;
import com.epam.digital.data.platform.notification.dto.email.EmailNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.email.EmailRecipientDto;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.epam.digital.data.platform.starter.audit.model.AuditEvent;
import com.epam.digital.data.platform.starter.audit.model.Step;
import com.epam.digital.data.platform.starter.audit.service.AuditService;
import java.time.Clock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailNotificationAuditFacadeTest {

  @Mock
  private AuditService auditService;
  private EmailNotificationAuditFacade auditFacade;

  @BeforeEach
  void init() {
    auditFacade = new EmailNotificationAuditFacade(auditService, "app", Clock.systemUTC());
  }

  @Test
  void testAuditOnSuccess() {
    var auditEventArgumentCaptor = ArgumentCaptor.forClass(AuditEvent.class);
    var emailNotificationMsg = createMessage();

    auditFacade.sendAuditOnSuccess(Channel.EMAIL, emailNotificationMsg);

    verify(auditService, times(1)).sendAudit(auditEventArgumentCaptor.capture());
    var auditEvent = auditEventArgumentCaptor.getValue();
    var notificationAudit = (NotificationAuditDto) auditEvent.getContext().get("notification");
    assertThat(notificationAudit.getChannel()).isEqualTo(Channel.EMAIL.getValue());
    assertThat(notificationAudit.getSubject()).isEqualTo(
        emailNotificationMsg.getNotification().getSubject());
    assertThat(notificationAudit.getMessage()).isEqualTo(
        emailNotificationMsg.getNotification().getMessage());
    assertThat(notificationAudit.getRecipient().getId()).isEqualTo(
        emailNotificationMsg.getRecipient().getId());
    assertThat(notificationAudit.getRecipient().getEmail()).isEqualTo(
        emailNotificationMsg.getRecipient().getEmail());
  }

  @Test
  void testAuditOnFailure() {
    var auditEventArgumentCaptor = ArgumentCaptor.forClass(AuditEvent.class);
    var emailNotificationMsg = createMessage();

    auditFacade.sendAuditOnFailure(Channel.EMAIL, emailNotificationMsg, Step.AFTER, "fail reason");

    verify(auditService, times(1)).sendAudit(auditEventArgumentCaptor.capture());
    var auditEvent = auditEventArgumentCaptor.getValue();
    var deliveryAuditDto = (DeliveryAuditDto) auditEvent.getContext().get("delivery");
    assertThat(deliveryAuditDto.getFailureReason()).isEqualTo("fail reason");
  }

  private EmailNotificationMessageDto createMessage() {
    return EmailNotificationMessageDto.builder()
        .notification(EmailNotificationDto.builder()
            .subject("test subject")
            .message("msg")
            .build())
        .recipient(EmailRecipientDto.builder()
            .email("test@test.com")
            .id("testuser")
            .build())
        .build();
  }

}