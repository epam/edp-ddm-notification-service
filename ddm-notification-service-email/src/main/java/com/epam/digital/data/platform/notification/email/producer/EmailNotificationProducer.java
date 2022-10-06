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

package com.epam.digital.data.platform.notification.email.producer;

import com.epam.digital.data.platform.notification.dto.ChannelObject;
import com.epam.digital.data.platform.notification.dto.Recipient;
import com.epam.digital.data.platform.notification.dto.UserNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.email.EmailNotificationDto;
import com.epam.digital.data.platform.notification.dto.email.EmailNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.email.EmailRecipientDto;
import com.epam.digital.data.platform.notification.producer.NotificationProducer;
import com.epam.digital.data.platform.notification.email.service.EmailNotificationTemplateService;
import com.epam.digital.data.platform.notification.email.service.EmailNotificationService;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@Setter
@RequiredArgsConstructor
public class EmailNotificationProducer implements NotificationProducer {

  @Value("\u0023{kafkaProperties.topics['email-notifications']}")
  private String topic;

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final EmailNotificationService emailNotificationService;
  private final EmailNotificationTemplateService emailNotificationTemplateService;

  @Override
  public void send(Recipient recipient, UserNotificationMessageDto message) {
    log.info("Sending email notification Kafka message");
    var emailNotificationMessageDto = createMessageDto(recipient, message);
    kafkaTemplate.send(topic, emailNotificationMessageDto);
    log.info("Email notification Kafka message is sent, context: {}", message.getContext());
  }

  @Override
  public Channel getChannel() {
    return Channel.EMAIL;
  }

  private EmailNotificationMessageDto createMessageDto(Recipient recipient,
      UserNotificationMessageDto message) {
    var emailChannel = getEmailChannel(recipient.getChannels());
    var messageBody = createEmailMessageBody(recipient, message);
    var title = getTitle(message);
    return EmailNotificationMessageDto.builder()
        .context(message.getContext())
        .notification(EmailNotificationDto.builder()
            .subject(title)
            .message(messageBody)
            .build())
        .recipient(EmailRecipientDto.builder()
            .id(recipient.getId())
            .email(emailChannel.getEmail())
            .build())
        .build();
  }

  private String createEmailMessageBody(Recipient recipient, UserNotificationMessageDto message) {
    var templateName = message.getNotification().getTemplateName();
    return emailNotificationService.prepareEmailBody(templateName, recipient.getParameters());
  }

  private ChannelObject getEmailChannel(List<ChannelObject> channelObjectList) {
    return channelObjectList.stream()
        .filter(channelObject -> Channel.EMAIL.getValue().equals(channelObject.getChannel()))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Email channel not found"));
  }

  private String getTitle(UserNotificationMessageDto message) {
    var title = message.getNotification().getTitle();
    var templateName = message.getNotification().getTemplateName();
    return Objects.isNull(title) ?
        emailNotificationTemplateService.getTitleByTemplateName(templateName) : title;
  }
}
