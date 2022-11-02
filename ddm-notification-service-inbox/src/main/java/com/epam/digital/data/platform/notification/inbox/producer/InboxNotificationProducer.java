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

package com.epam.digital.data.platform.notification.inbox.producer;

import com.epam.digital.data.platform.notification.dto.Recipient;
import com.epam.digital.data.platform.notification.dto.UserNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.inbox.InboxNotificationDto;
import com.epam.digital.data.platform.notification.dto.inbox.InboxNotificationMessageDto;
import com.epam.digital.data.platform.notification.exception.NotificationTemplateNotFoundException;
import com.epam.digital.data.platform.notification.inbox.service.InboxNotificationService;
import com.epam.digital.data.platform.notification.inbox.service.InboxNotificationTemplateService;
import com.epam.digital.data.platform.notification.producer.NotificationProducer;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@Setter
@RequiredArgsConstructor
public class InboxNotificationProducer implements NotificationProducer {

  @Value("\u0023{kafkaProperties.topics['inbox-notifications']}")
  private String topic;

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final InboxNotificationService inboxNotificationService;
  private final InboxNotificationTemplateService inboxNotificationTemplateService;

  @Override
  public void send(Recipient recipient, UserNotificationMessageDto message) {
    log.info("Sending inbox notification Kafka message");
    var inboxNotificationMessageDto = createMessageDto(recipient, message);

    if (Objects.nonNull(inboxNotificationMessageDto)) {
      kafkaTemplate.send(topic, inboxNotificationMessageDto);
      log.info("Inbox notification Kafka message is sent, context: {}", message.getContext());
    }
  }

  @Override
  public Channel getChannel() {
    return Channel.INBOX;
  }

  private InboxNotificationMessageDto createMessageDto(Recipient recipient,
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
      return null;
    }

    return InboxNotificationMessageDto.builder()
        .context(message.getContext())
        .notification(InboxNotificationDto.builder()
            .subject(title)
            .message(messageBody)
            .build())
        .recipientName(recipient.getId())
        .build();
  }

  private String createMessageBody(Recipient recipient, UserNotificationMessageDto message) {
    var templateName = message.getNotification().getTemplateName();
    return inboxNotificationService.prepareInboxBody(templateName, recipient.getParameters());
  }

  private String getTitle(UserNotificationMessageDto message) {
    var title = message.getNotification().getTitle();
    var templateName = message.getNotification().getTemplateName();
    return Objects.isNull(title) ?
        inboxNotificationTemplateService.getTitleByTemplateName(templateName) : title;
  }
}
