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

package com.epam.digital.data.platform.notification.inbox.audit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.digital.data.platform.notification.dto.NotificationContextDto;
import com.epam.digital.data.platform.notification.dto.audit.DeliveryAuditDto;
import com.epam.digital.data.platform.notification.dto.audit.NotificationAuditDto;
import com.epam.digital.data.platform.notification.dto.inbox.InboxNotificationDto;
import com.epam.digital.data.platform.notification.dto.inbox.InboxNotificationMessageDto;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.epam.digital.data.platform.starter.audit.model.AuditEvent;
import com.epam.digital.data.platform.starter.audit.model.AuditSourceInfo;
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
class InboxNotificationAuditFacadeTest {

  @Mock
  private AuditService auditService;
  private InboxNotificationAuditFacade auditFacade;

  @BeforeEach
  void init() {
    auditFacade = new InboxNotificationAuditFacade(auditService, "app", Clock.systemUTC());
  }

  @Test
  void testAuditOnSuccess() {
    var auditEventArgumentCaptor = ArgumentCaptor.forClass(AuditEvent.class);
    var notificationMsg = createMessage();

    auditFacade.sendAuditOnSuccess(Channel.INBOX, notificationMsg);

    verify(auditService, times(1)).sendAudit(auditEventArgumentCaptor.capture());
    var auditEvent = auditEventArgumentCaptor.getValue();
    var notificationAudit = (NotificationAuditDto) auditEvent.getContext().get("notification");
    assertThat(notificationAudit.getChannel()).isEqualTo(Channel.INBOX.getValue());
    assertThat(notificationAudit.getSubject()).isEqualTo(
        notificationMsg.getNotification().getSubject());
    assertThat(notificationAudit.getMessage()).isEqualTo(
        notificationMsg.getNotification().getMessage());
    assertSourceInfo(auditEvent.getSourceInfo(), notificationMsg.getContext());
  }

  @Test
  void testAuditOnFailure() {
    var auditEventArgumentCaptor = ArgumentCaptor.forClass(AuditEvent.class);
    var notificationMsg = createMessage();

    auditFacade.sendAuditOnFailure(Channel.INBOX, notificationMsg, Step.AFTER, "fail reason");

    verify(auditService, times(1)).sendAudit(auditEventArgumentCaptor.capture());
    var auditEvent = auditEventArgumentCaptor.getValue();
    var deliveryAuditDto = (DeliveryAuditDto) auditEvent.getContext().get("delivery");
    assertThat(deliveryAuditDto.getFailureReason()).isEqualTo("fail reason");
    assertSourceInfo(auditEvent.getSourceInfo(), notificationMsg.getContext());
  }

  private InboxNotificationMessageDto createMessage() {
    return InboxNotificationMessageDto.builder()
        .notification(InboxNotificationDto.builder()
            .subject("test subject")
            .message("msg")
            .build())
        .recipientName("testuser")
        .context(NotificationContextDto.builder()
            .application("application")
            .businessActivity("activity")
            .businessActivityInstanceId("instanceId")
            .businessProcess("process")
            .businessProcessDefinitionId("processDefinitionId")
            .system("system")
            .build())
        .build();
  }

  private void assertSourceInfo(AuditSourceInfo sourceInfo, NotificationContextDto contextDto) {
    assertThat(sourceInfo.getApplication()).isEqualTo(
        contextDto.getApplication());
    assertThat(sourceInfo.getSystem()).isEqualTo(
        contextDto.getSystem());
    assertThat(sourceInfo.getBusinessProcessDefinitionId()).isEqualTo(
        contextDto.getBusinessProcessDefinitionId());
    assertThat(sourceInfo.getBusinessActivity()).isEqualTo(
        contextDto.getBusinessActivity());
    assertThat(sourceInfo.getBusinessProcess()).isEqualTo(
        contextDto.getBusinessProcess());
    assertThat(sourceInfo.getBusinessProcessInstanceId()).isEqualTo(
        contextDto.getBusinessProcessInstanceId());
    assertThat(sourceInfo.getBusinessActivityInstanceId()).isEqualTo(
        contextDto.getBusinessActivityInstanceId());
  }
}