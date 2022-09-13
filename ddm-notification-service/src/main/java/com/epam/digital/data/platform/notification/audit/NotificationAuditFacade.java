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

package com.epam.digital.data.platform.notification.audit;

import com.epam.digital.data.platform.notification.audit.dto.AuditResultDto;
import com.epam.digital.data.platform.notification.audit.dto.DeliveryAuditDto;
import com.epam.digital.data.platform.notification.audit.dto.NotificationAuditDto;
import com.epam.digital.data.platform.notification.dto.NotificationDto;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.epam.digital.data.platform.starter.audit.model.EventType;
import com.epam.digital.data.platform.starter.audit.model.Operation;
import com.epam.digital.data.platform.starter.audit.model.Status;
import com.epam.digital.data.platform.starter.audit.model.Step;
import com.epam.digital.data.platform.starter.audit.service.AbstractAuditFacade;
import com.epam.digital.data.platform.starter.audit.service.AuditService;
import java.time.Clock;
import java.util.Objects;
import org.slf4j.MDC;

public class NotificationAuditFacade extends AbstractAuditFacade {

  private static final String MDC_TRACE_ID_HEADER = "X-B3-TraceId";

  public NotificationAuditFacade(AuditService auditService, String appName, Clock clock) {
    super(auditService, appName, clock);
  }

  public void sendAuditOnSuccess(Channel channel, NotificationDto notification) {
    this.sendNotificationAudit(EventType.SYSTEM_EVENT,
        Operation.SEND_USER_NOTIFICATION.name(), Step.AFTER.name(),
        AuditResultDto.builder().status(Status.SUCCESS.name()).build(),
        channel, notification);
  }

  public void sendAuditOnFailure(Channel channel, NotificationDto notification, Step step,
      String failureReason) {
    this.sendNotificationAudit(EventType.SYSTEM_EVENT,
        Operation.SEND_USER_NOTIFICATION.name(), step.name(),
        AuditResultDto.builder().status(Status.FAILURE.name()).failureReason(failureReason).build(),
        channel, notification);
  }

  private void sendNotificationAudit(EventType eventType, String action, String step,
      AuditResultDto result, Channel channel, NotificationDto notificationDto) {
    var event = createBaseAuditEvent(
        eventType, action, MDC.get(MDC_TRACE_ID_HEADER));

    var notification = NotificationAuditDto.builder()
        .recipient(Objects.nonNull(notificationDto) ? notificationDto.getRecipient() : null)
        .subject(Objects.nonNull(notificationDto) ? notificationDto.getSubject() : null)
        .build();
    var delivery = DeliveryAuditDto.builder()
        .failureReason(result.getFailureReason())
        .status(result.getStatus())
        .channel(channel)
        .build();

    var context = auditService.createContext(action, step, null, null, null, result.getStatus());
    context.put("notification", notification);
    context.put("delivery", delivery);
    event.setContext(context);

    auditService.sendAudit(event.build());
  }
}
