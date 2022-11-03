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

import com.epam.digital.data.platform.notification.dto.audit.AuditResultDto;
import com.epam.digital.data.platform.notification.dto.audit.DeliveryAuditDto;
import com.epam.digital.data.platform.notification.dto.audit.DiiaNotificationAuditDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaNotificationMessageDto;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.epam.digital.data.platform.starter.audit.model.EventType;
import com.epam.digital.data.platform.starter.audit.model.Operation;
import com.epam.digital.data.platform.starter.audit.model.Status;
import com.epam.digital.data.platform.starter.audit.model.Step;
import com.epam.digital.data.platform.starter.audit.service.AbstractAuditFacade;
import com.epam.digital.data.platform.starter.audit.service.AuditService;
import java.time.Clock;
import org.slf4j.MDC;

public class DiiaNotificationAuditFacade extends AbstractAuditFacade {

  private static final String MDC_TRACE_ID_HEADER = "X-B3-TraceId";

  public DiiaNotificationAuditFacade(AuditService auditService, String appName, Clock clock) {
    super(auditService, appName, clock);
  }

  public void sendAuditOnSuccess(DiiaNotificationMessageDto notification, String distributionId) {
    this.sendNotificationAudit(
        Step.AFTER.name(),
        AuditResultDto.builder().status(Status.SUCCESS.name()).build(),
        notification,
        distributionId);
  }

  public void sendAuditOnFailure(
      DiiaNotificationMessageDto notification, Step step, String failureReason) {
    this.sendNotificationAudit(
        step.name(),
        AuditResultDto.builder().status(Status.FAILURE.name()).failureReason(failureReason).build(),
        notification,
        null);
  }

  private void sendNotificationAudit(String step,
      AuditResultDto result, DiiaNotificationMessageDto notificationDto, String distributionId) {
    var event = createBaseAuditEvent(
        EventType.SYSTEM_EVENT, Operation.SEND_USER_NOTIFICATION.name(),
        MDC.get(MDC_TRACE_ID_HEADER));

    var notification = DiiaNotificationAuditDto.builder()
        .channel(Channel.DIIA.getValue())
        .externalTemplateId(notificationDto.getNotification().getExternalTemplateId())
        .templateName(notificationDto.getNotification().getTemplateName())
        .distributionId(distributionId)
        .recipient(notificationDto.getRecipient())
        .build();
    var delivery = DeliveryAuditDto.builder()
        .failureReason(result.getFailureReason())
        .status(result.getStatus())
        .channel(Channel.DIIA)
        .build();

    var context = auditService.createContext(
        Operation.SEND_USER_NOTIFICATION.name(), step, null, null, null, result.getStatus());
    context.put("notification", notification);
    context.put("delivery", delivery);
    event.setContext(context);

    auditService.sendAudit(event.build());
  }
}
