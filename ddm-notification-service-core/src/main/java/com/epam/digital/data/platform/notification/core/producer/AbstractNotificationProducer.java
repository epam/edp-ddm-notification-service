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
package com.epam.digital.data.platform.notification.core.producer;

import com.epam.digital.data.platform.notification.dto.ChannelObject;
import com.epam.digital.data.platform.notification.dto.Recipient;
import com.epam.digital.data.platform.notification.dto.UserNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.audit.NotificationMessageDto;
import com.epam.digital.data.platform.notification.producer.NotificationProducer;
import com.epam.digital.data.platform.notification.template.NotificationTemplateService;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractNotificationProducer<T extends NotificationMessageDto> implements
    NotificationProducer<T> {

  protected final NotificationTemplateService<String> notificationTemplateService;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final String topic;

  @Override
  public void send(Recipient recipient, UserNotificationMessageDto message) {
    log.info("Sending {} notification Kafka message", getChannel().getValue());
    var notificationMessageDto = createMessageDto(recipient, message);

    if (Objects.nonNull(notificationMessageDto)) {
      kafkaTemplate.send(topic, notificationMessageDto);
      log.info("{} notification Kafka message is sent, context: {}",
          getChannel().getValue(),
          message.getContext());
    }
  }

  public String getTitle(UserNotificationMessageDto message) {
    var title = message.getNotification().getTitle();
    var templateName = message.getNotification().getTemplateName();
    return Objects.isNull(title) ?
        notificationTemplateService.getTitleByNameAndChannel(templateName, getChannel()) : title;
  }


  @Override
  public ChannelObject getChannelObject(List<ChannelObject> channelObjectList) {
    return channelObjectList.stream()
        .filter(channelObject -> getChannel().getValue().equals(channelObject.getChannel()))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
            String.format("%s channel not found", getChannel().getValue())));
  }

}
