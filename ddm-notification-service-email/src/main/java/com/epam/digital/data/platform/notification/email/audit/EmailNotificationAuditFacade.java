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

import com.epam.digital.data.platform.notification.core.audit.AbstractNotificationAuditFacade;
import com.epam.digital.data.platform.notification.dto.audit.NotificationAuditDto;
import com.epam.digital.data.platform.notification.dto.audit.RecipientAuditDto;
import com.epam.digital.data.platform.notification.dto.email.EmailNotificationAuditDto;
import com.epam.digital.data.platform.notification.dto.email.EmailNotificationMessageDto;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.epam.digital.data.platform.starter.audit.service.AuditService;
import java.time.Clock;
import java.util.Objects;

public class EmailNotificationAuditFacade extends
    AbstractNotificationAuditFacade<EmailNotificationMessageDto> {

  public EmailNotificationAuditFacade(AuditService auditService, String appName, Clock clock) {
    super(auditService, appName, clock);
  }

  @Override
  public NotificationAuditDto notificationAuditDto(EmailNotificationMessageDto notificationDto,
      Channel channel) {
    return EmailNotificationAuditDto.builder()
        .recipient(Objects.nonNull(notificationDto) ?
            RecipientAuditDto.builder()
                .id(notificationDto.getRecipient().getId())
                .email(notificationDto.getRecipient().getEmail())
                .build() : null)
        .subject(Objects.nonNull(notificationDto) ?
            notificationDto.getNotification().getSubject() : null)
        .message(Objects.nonNull(notificationDto) ?
            notificationDto.getNotification().getMessage() : null)
        .channel(channel.getValue())
        .build();
  }
}
