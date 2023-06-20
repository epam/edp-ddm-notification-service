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

package com.epam.digital.data.platform.notification.inbox.producer;

import com.epam.digital.data.platform.notification.core.producer.AbstractNotificationProducer;
import com.epam.digital.data.platform.notification.dto.Recipient;
import com.epam.digital.data.platform.notification.dto.UserNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.audit.NotificationDto;
import com.epam.digital.data.platform.notification.dto.inbox.InboxNotificationMessageDto;
import com.epam.digital.data.platform.notification.exception.NotificationTemplateNotFoundException;
import com.epam.digital.data.platform.notification.inbox.service.InboxNotificationService;
import com.epam.digital.data.platform.notification.template.NotificationTemplateService;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
public class InboxNotificationProducer extends
    AbstractNotificationProducer<InboxNotificationMessageDto> {

  private final InboxNotificationService inboxNotificationService;

  public InboxNotificationProducer(
      NotificationTemplateService<String> notificationTemplateService,
      KafkaTemplate<String, Object> kafkaTemplate,
      String topic,
      InboxNotificationService inboxNotificationService) {
    super(notificationTemplateService, kafkaTemplate, topic);
    this.inboxNotificationService = inboxNotificationService;
  }

  @Override
  public Channel getChannel() {
    return Channel.INBOX;
  }

  @Override
  public InboxNotificationMessageDto createMessageDto(Recipient recipient,
      UserNotificationMessageDto message) {
    String messageBody;
    String title;
    try {
      messageBody = createMessageBody(recipient, message);
      title = getTitle(message);
    } catch (NotificationTemplateNotFoundException e) {
      log.warn(
          "Notification template not found for template: {}. '{}' Notification won't be delivered.",
          message.getNotification().getTemplateName(), getChannel().getValue());
      throw e;
    }
    return InboxNotificationMessageDto.builder()
        .recipientName(recipient.getId())
        .recipientRealm(recipient.getRealm())
        .context(message.getContext())
        .notification(NotificationDto.builder()
            .subject(title)
            .message(messageBody)
            .build())
        .build();
  }

  private String createMessageBody(Recipient recipient, UserNotificationMessageDto message) {
    var templateName = message.getNotification().getTemplateName();
    return inboxNotificationService.prepareInboxBody(templateName, recipient.getParameters());
  }

}
