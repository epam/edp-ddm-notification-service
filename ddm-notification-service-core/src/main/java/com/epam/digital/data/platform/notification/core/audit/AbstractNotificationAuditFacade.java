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
package com.epam.digital.data.platform.notification.core.audit;

import com.epam.digital.data.platform.notification.audit.NotificationAuditFacade;
import com.epam.digital.data.platform.notification.dto.NotificationContextDto;
import com.epam.digital.data.platform.notification.dto.audit.AuditResultDto;
import com.epam.digital.data.platform.notification.dto.audit.DeliveryAuditDto;
import com.epam.digital.data.platform.notification.dto.audit.NotificationMessageDto;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.epam.digital.data.platform.starter.audit.model.AuditSourceInfo;
import com.epam.digital.data.platform.starter.audit.model.EventType;
import com.epam.digital.data.platform.starter.audit.model.Operation;
import com.epam.digital.data.platform.starter.audit.model.Status;
import com.epam.digital.data.platform.starter.audit.model.Step;
import com.epam.digital.data.platform.starter.audit.service.AbstractAuditFacade;
import com.epam.digital.data.platform.starter.audit.service.AuditService;
import java.time.Clock;
import java.util.Objects;
import org.slf4j.MDC;

public abstract class AbstractNotificationAuditFacade<T extends NotificationMessageDto> extends
    AbstractAuditFacade implements NotificationAuditFacade<T> {

  private static final String MDC_TRACE_ID_HEADER = "X-B3-TraceId";

  public AbstractNotificationAuditFacade(
      AuditService auditService,
      String appName,
      Clock clock) {
    super(auditService, appName, clock);
  }

  public void sendAuditOnSuccess(Channel channel, T notification) {
    this.sendNotificationAudit(EventType.SYSTEM_EVENT,
        Operation.SEND_USER_NOTIFICATION.name(), Step.AFTER.name(),
        AuditResultDto.builder().status(Status.SUCCESS.name()).build(),
        channel, notification);
  }

  public void sendAuditOnFailure(Channel channel, T notification,
      Step step,
      String failureReason) {
    this.sendNotificationAudit(EventType.SYSTEM_EVENT,
        Operation.SEND_USER_NOTIFICATION.name(), step.name(),
        AuditResultDto.builder().status(Status.FAILURE.name()).failureReason(failureReason).build(),
        channel, notification);
  }

  private void sendNotificationAudit(EventType eventType, String action, String step,
      AuditResultDto result, Channel channel, T notificationDto) {
    var event = createBaseAuditEvent(
        eventType, action, MDC.get(MDC_TRACE_ID_HEADER));

    var notification = notificationAuditDto(notificationDto, channel);

    var delivery = DeliveryAuditDto.builder()
        .failureReason(result.getFailureReason())
        .status(result.getStatus())
        .channel(channel)
        .build();

    var context = auditService.createContext(action, step, null, null, null, result.getStatus());
    context.put("notification", notification);
    context.put("delivery", delivery);
    event.setContext(context);

    event.setSourceInfo(Objects.nonNull(notificationDto) ?
        toAuditSourceDto(notificationDto.getContext()) : null);

    auditService.sendAudit(event.build());
  }


  private AuditSourceInfo toAuditSourceDto(NotificationContextDto notificationContext) {
    return AuditSourceInfo.AuditSourceInfoBuilder.anAuditSourceInfo()
        .application(notificationContext.getApplication())
        .businessActivity(notificationContext.getBusinessActivity())
        .businessActivityInstanceId(notificationContext.getBusinessActivityInstanceId())
        .businessProcess(notificationContext.getBusinessProcess())
        .businessProcessDefinitionId(notificationContext.getBusinessProcessDefinitionId())
        .businessProcessInstanceId(notificationContext.getBusinessProcessInstanceId())
        .system(notificationContext.getSystem())
        .build();
  }

}
