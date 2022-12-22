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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.notification.dto.ChannelObject;
import com.epam.digital.data.platform.notification.dto.Recipient;
import com.epam.digital.data.platform.notification.dto.UserNotificationDto;
import com.epam.digital.data.platform.notification.dto.UserNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.audit.NotificationDto;
import com.epam.digital.data.platform.notification.dto.email.EmailNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.email.EmailRecipientDto;
import com.epam.digital.data.platform.notification.email.service.EmailNotificationService;
import com.epam.digital.data.platform.notification.template.NotificationTemplateService;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class EmailNotificationProducerTest {

  private final String topic = "user-notifications";
  @Mock
  private KafkaTemplate<String, Object> kafkaTemplate;
  @Mock
  private EmailNotificationService emailNotificationService;
  @Mock
  private NotificationTemplateService<String> emailNotificationTemplateService;

  private EmailNotificationProducer producer;

  @BeforeEach
  void init() {
    producer = new EmailNotificationProducer(emailNotificationTemplateService, kafkaTemplate, topic, emailNotificationService);
  }

  @Test
  void shouldSendNotification() {
    var title = "title";
    var emailBody = "content";
    var recipientId = "testuser";
    var email = "email@dot.com";
    var templateName = "test-template";
    Map<String, Object> parameters = Map.of("name", "John");
    var recipient = Recipient.builder()
        .id(recipientId)
        .parameters(parameters)
        .channels(List.of(ChannelObject.builder()
            .email(email)
            .channel(Channel.EMAIL.getValue())
            .build()))
        .build();
    var userNotification = UserNotificationDto.builder()
        .templateName(templateName)
        .build();
    var userNotificationMsg = UserNotificationMessageDto.builder()
        .notification(userNotification)
        .build();
    when(emailNotificationService.prepareEmailBody(templateName, parameters)).thenReturn(emailBody);
    when(emailNotificationTemplateService.getTitleByNameAndChannel(templateName, Channel.EMAIL)).thenReturn(title);

    producer.send(recipient, userNotificationMsg);

    var expectedMessage = EmailNotificationMessageDto.builder()
        .recipient(EmailRecipientDto.builder()
            .id(recipientId)
            .email(email)
            .build())
        .notification(NotificationDto.builder()
            .message(emailBody)
            .subject(title)
            .build())
        .build();
    verify(kafkaTemplate, times(1)).send(topic, expectedMessage);
  }
}