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

import com.epam.digital.data.platform.notification.core.producer.AbstractNotificationProducer;
import com.epam.digital.data.platform.notification.dto.Recipient;
import com.epam.digital.data.platform.notification.dto.UserNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.audit.NotificationDto;
import com.epam.digital.data.platform.notification.dto.email.EmailNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.email.EmailRecipientDto;
import com.epam.digital.data.platform.notification.email.service.EmailNotificationService;
import com.epam.digital.data.platform.notification.template.NotificationTemplateService;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
public class EmailNotificationProducer extends
    AbstractNotificationProducer<EmailNotificationMessageDto> {

  private final EmailNotificationService emailNotificationService;

  public EmailNotificationProducer(
      NotificationTemplateService<String> notificationTemplateService,
      KafkaTemplate<String, Object> kafkaTemplate,
      String topic,
      EmailNotificationService emailNotificationService) {
    super(notificationTemplateService, kafkaTemplate, topic);
    this.emailNotificationService = emailNotificationService;
  }


  @Override
  public Channel getChannel() {
    return Channel.EMAIL;
  }

  public EmailNotificationMessageDto createMessageDto(Recipient recipient,
      UserNotificationMessageDto message) {
    var emailChannel = getChannelObject(recipient.getChannels());
    var messageBody = createEmailMessageBody(recipient, message);
    var title = getTitle(message);
    return EmailNotificationMessageDto.builder()
        .context(message.getContext())
        .notification(NotificationDto.builder()
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

}
