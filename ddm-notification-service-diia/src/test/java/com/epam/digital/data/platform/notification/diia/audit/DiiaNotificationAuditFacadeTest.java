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

package com.epam.digital.data.platform.notification.diia.audit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.digital.data.platform.notification.dto.audit.DeliveryAuditDto;
import com.epam.digital.data.platform.notification.dto.audit.DiiaNotificationAuditDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaNotificationDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaRecipientDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaRecipientDto.KeyValue;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.epam.digital.data.platform.starter.audit.model.AuditEvent;
import com.epam.digital.data.platform.starter.audit.model.Step;
import com.epam.digital.data.platform.starter.audit.service.AuditService;
import java.time.Clock;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiiaNotificationAuditFacadeTest {

  @Mock
  private AuditService auditService;
  private DiiaNotificationAuditFacade auditFacade;

  @BeforeEach
  void init() {
    auditFacade = new DiiaNotificationAuditFacade(auditService, "app", Clock.systemUTC());
  }

  @Test
  void testAuditOnSuccess() {
    var auditEventArgumentCaptor = ArgumentCaptor.forClass(AuditEvent.class);
    var recipient = createRecipient();
    var diiaNotificationMsg = createMessage(recipient);

    auditFacade.sendAuditOnSuccess(diiaNotificationMsg, "1000");

    verify(auditService, times(1)).sendAudit(auditEventArgumentCaptor.capture());
    var auditEvent = auditEventArgumentCaptor.getValue();
    var notificationAudit = (DiiaNotificationAuditDto) auditEvent.getContext().get("notification");
    assertThat(notificationAudit.getChannel()).isEqualTo(Channel.DIIA.getValue());
    assertThat(notificationAudit.getTemplateName()).isEqualTo("templateName");
    assertThat(notificationAudit.getExternalTemplateId()).isEqualTo("externalTemplateId");
    assertThat(notificationAudit.getDistributionId()).isEqualTo("1000");
    assertThat(notificationAudit.getRecipient()).isEqualTo(recipient);
  }


  @Test
  void testAuditOnFailure() {
    var auditEventArgumentCaptor = ArgumentCaptor.forClass(AuditEvent.class);

    var recipient = createRecipient();
    var diiaNotificationMsg = createMessage(recipient);

    auditFacade.sendAuditOnFailure(diiaNotificationMsg, Step.AFTER, "fail reason");

    verify(auditService, times(1)).sendAudit(auditEventArgumentCaptor.capture());
    var auditEvent = auditEventArgumentCaptor.getValue();
    var deliveryAuditDto = (DeliveryAuditDto) auditEvent.getContext().get("delivery");
    assertThat(deliveryAuditDto.getFailureReason()).isEqualTo("fail reason");
  }

  private DiiaNotificationMessageDto createMessage(DiiaRecipientDto recipient) {
    return DiiaNotificationMessageDto.builder()
        .notification(DiiaNotificationDto.builder()
            .externalTemplateId("externalTemplateId")
            .templateName("templateName")
            .build())
        .recipient(recipient)
        .build();
  }

  private DiiaRecipientDto createRecipient() {
    return DiiaRecipientDto.builder().id("kaycloak-username")
        .rnokpp("rnokpp")
        .parameters(List.of(new KeyValue("key", "value")))
        .build();
  }
}