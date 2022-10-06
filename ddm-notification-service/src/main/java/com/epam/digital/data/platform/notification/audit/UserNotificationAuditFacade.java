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

import com.epam.digital.data.platform.notification.dto.UserNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.audit.AuditResultDto;
import com.epam.digital.data.platform.notification.dto.audit.DeliveryAuditDto;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.epam.digital.data.platform.starter.audit.model.EventType;
import com.epam.digital.data.platform.starter.audit.model.Operation;
import com.epam.digital.data.platform.starter.audit.model.Status;
import com.epam.digital.data.platform.starter.audit.model.Step;
import com.epam.digital.data.platform.starter.audit.service.AbstractAuditFacade;
import com.epam.digital.data.platform.starter.audit.service.AuditService;
import java.time.Clock;
import org.slf4j.MDC;

public class UserNotificationAuditFacade extends AbstractAuditFacade {

  private static final String MDC_TRACE_ID_HEADER = "X-B3-TraceId";

  public UserNotificationAuditFacade(AuditService auditService, String appName, Clock clock) {
    super(auditService, appName, clock);
  }

  public void sendAuditOnFailure(Channel channel, UserNotificationMessageDto notification, Step step,
      String failureReason) {
    this.sendNotificationAudit(EventType.SYSTEM_EVENT,
        Operation.SEND_USER_NOTIFICATION.name(), step.name(),
        AuditResultDto.builder().status(Status.FAILURE.name()).failureReason(failureReason).build(),
        channel, notification);
  }

  private void sendNotificationAudit(EventType eventType, String action, String step,
      AuditResultDto result, Channel channel, UserNotificationMessageDto notificationDto) {
    var event = createBaseAuditEvent(
        eventType, action, MDC.get(MDC_TRACE_ID_HEADER));
    var context = auditService.createContext(action, step, null, null, null, result.getStatus());
    context.put("notification", notificationDto);
    context.put("delivery", DeliveryAuditDto.builder()
        .failureReason(result.getFailureReason())
        .status(result.getStatus())
        .channel(channel)
        .build());
    event.setContext(context);

    auditService.sendAudit(event.build());
  }
}
