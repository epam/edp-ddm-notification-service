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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.notification.dto.ChannelObject;
import com.epam.digital.data.platform.notification.dto.Recipient;
import com.epam.digital.data.platform.notification.dto.UserNotificationDto;
import com.epam.digital.data.platform.notification.dto.UserNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.inbox.InboxNotificationDto;
import com.epam.digital.data.platform.notification.dto.inbox.InboxNotificationMessageDto;
import com.epam.digital.data.platform.notification.inbox.service.InboxNotificationService;
import com.epam.digital.data.platform.notification.inbox.service.InboxNotificationTemplateService;
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
class InboxNotificationProducerTest {

  private final String topic = "user-notifications";
  @Mock
  private KafkaTemplate<String, Object> kafkaTemplate;
  @Mock
  private InboxNotificationService inboxNotificationService;
  @Mock
  private InboxNotificationTemplateService inboxNotificationTemplateService;

  private InboxNotificationProducer producer;

  @BeforeEach
  void init() {
    producer = new InboxNotificationProducer(kafkaTemplate, inboxNotificationService,
        inboxNotificationTemplateService);
    producer.setTopic(topic);
  }

  @Test
  void shouldSendNotification() {
    var title = "title";
    var inboxBody = "content";
    var recipientId = "testuser";
    var templateName = "test-template";
    Map<String, Object> parameters = Map.of("name", "John");
    var recipient = Recipient.builder()
        .id(recipientId)
        .parameters(parameters)
        .channels(List.of(ChannelObject.builder()
            .channel(Channel.INBOX.getValue())
            .build()))
        .build();
    var userNotification = UserNotificationDto.builder()
        .templateName(templateName)
        .build();
    var userNotificationMsg = UserNotificationMessageDto.builder()
        .notification(userNotification)
        .build();
    when(inboxNotificationService.prepareInboxBody(templateName, parameters)).thenReturn(inboxBody);
    when(inboxNotificationTemplateService.getTitleByTemplateName(templateName)).thenReturn(title);

    producer.send(recipient, userNotificationMsg);

    var expectedMessage = InboxNotificationMessageDto.builder()
        .recipientName(recipientId)
        .notification(InboxNotificationDto.builder()
            .message(inboxBody)
            .subject(title)
            .build())
        .build();
    verify(kafkaTemplate, times(1)).send(topic, expectedMessage);
  }
}