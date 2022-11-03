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

import com.epam.digital.data.platform.notification.diia.repository.DiiaNotificationTemplateRepository;
import com.epam.digital.data.platform.notification.dto.ChannelObject;
import com.epam.digital.data.platform.notification.dto.Recipient;
import com.epam.digital.data.platform.notification.dto.UserNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaRecipientDto.KeyValue;
import com.epam.digital.data.platform.notification.dto.diia.DiiaNotificationDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaRecipientDto;
import com.epam.digital.data.platform.notification.exception.NotificationTemplateNotFoundException;
import com.epam.digital.data.platform.notification.producer.NotificationProducer;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@Setter
@RequiredArgsConstructor
public class DiiaNotificationProducer implements NotificationProducer {

  @Value("\u0023{kafkaProperties.topics['diia-notifications']}")
  private String topic;

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final DiiaNotificationTemplateRepository repository;

  @Override
  public void send(Recipient recipient, UserNotificationMessageDto message) {
    log.info("Sending diia notification Kafka message");
    var diiaNotificationMessageDto = createMessageDto(recipient, message);
    kafkaTemplate.send(topic, diiaNotificationMessageDto);
    log.info("Diia notification Kafka message is sent, context: {}", message.getContext());
  }

  @Override
  public Channel getChannel() {
    return Channel.DIIA;
  }

  private DiiaNotificationMessageDto createMessageDto(Recipient recipient,
      UserNotificationMessageDto message) {
    var diiaChannel = getDiiaChannel(recipient.getChannels());
    var templateName = message.getNotification().getTemplateName();
    return DiiaNotificationMessageDto.builder()
        .context(message.getContext())
        .notification(DiiaNotificationDto.builder()
            .templateName(templateName)
            .externalTemplateId(getTemplateId(templateName))
            .build())
        .recipient(DiiaRecipientDto.builder()
            .rnokpp(diiaChannel.getRnokpp())
            .id(recipient.getId())
            .parameters(mapToListKeyValue(recipient.getParameters()))
            .build())
        .build();
  }

  private String getTemplateId(String templateName) {
    return repository.findByNameAndChannel(templateName, Channel.DIIA.getValue())
        .orElseThrow(() -> new NotificationTemplateNotFoundException(templateName))
        .getExtTemplateId();
  }

  private ChannelObject getDiiaChannel(List<ChannelObject> channelObjectList) {
    return channelObjectList.stream()
        .filter(channelObject -> Channel.DIIA.getValue().equals(channelObject.getChannel()))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Diia channel not found"));
  }

  private List<KeyValue> mapToListKeyValue(Map<String, Object> parameters) {
    return parameters.entrySet().stream()
        .map(entry -> new KeyValue(entry.getKey(), (String) entry.getValue()))
        .collect(Collectors.toList());
  }
}
