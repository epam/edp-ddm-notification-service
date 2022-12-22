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

package com.epam.digital.data.platform.notification.diia.producer;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.notification.dto.ChannelObject;
import com.epam.digital.data.platform.notification.dto.Recipient;
import com.epam.digital.data.platform.notification.dto.UserNotificationDto;
import com.epam.digital.data.platform.notification.dto.UserNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaNotificationDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaRecipientDto;
import com.epam.digital.data.platform.notification.entity.NotificationTemplate;
import com.epam.digital.data.platform.notification.repository.NotificationTemplateRepository;
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
class DiiaNotificationProducerTest {

  private final String topic = "diia-notifications";
  @Mock
  private KafkaTemplate<String, Object> kafkaTemplate;
  @Mock
  private NotificationTemplateRepository repository;
  @Mock
  private NotificationTemplateService<String> notificationTemplateService;

  private DiiaNotificationProducer producer;

  @BeforeEach
  void init() {
    producer = new DiiaNotificationProducer(notificationTemplateService, kafkaTemplate, topic);
  }

  @Test
  void shouldSendNotification() {
    var recipientId = "testuser";
    var templateName = "test-template";
    var rnokpp = "rnokpp-1";
    var templateId = "templateId-1";
    Map<String, Object> parameters = Map.of("name", "John");
    var recipient = Recipient.builder()
        .id(recipientId)
        .parameters(parameters)
        .channels(List.of(ChannelObject.builder()
            .rnokpp(rnokpp)
            .channel(Channel.DIIA.getValue())
            .build()))
        .build();
    var userNotification = UserNotificationDto.builder()
        .templateName(templateName)
        .build();
    var userNotificationMsg = UserNotificationMessageDto.builder()
        .notification(userNotification)
        .build();
    when(notificationTemplateService.getTemplate(templateName, Channel.DIIA)).thenReturn(
        NotificationTemplate.builder().extTemplateId(templateId).build());

    producer.send(recipient, userNotificationMsg);

    var expectedMessage = DiiaNotificationMessageDto.builder()
        .recipient(DiiaRecipientDto.builder()
            .id(recipientId)
            .rnokpp(rnokpp)
            .parameters(
                List.of(DiiaRecipientDto.KeyValue.builder().key("name").value("John").build()))
            .build())
        .diiaNotificationDto(DiiaNotificationDto.builder()
            .externalTemplateId(templateId)
            .templateName(templateName)
            .build())
        .build();
    verify(kafkaTemplate, times(1)).send(topic, expectedMessage);
  }
}