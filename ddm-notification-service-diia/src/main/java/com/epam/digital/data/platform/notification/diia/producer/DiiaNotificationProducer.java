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

import com.epam.digital.data.platform.notification.core.producer.AbstractNotificationProducer;
import com.epam.digital.data.platform.notification.dto.Recipient;
import com.epam.digital.data.platform.notification.dto.UserNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaRecipientDto.KeyValue;
import com.epam.digital.data.platform.notification.dto.diia.DiiaNotificationDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaRecipientDto;
import com.epam.digital.data.platform.notification.template.NotificationTemplateService;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
public class DiiaNotificationProducer extends
    AbstractNotificationProducer<DiiaNotificationMessageDto> {

  public DiiaNotificationProducer(
      NotificationTemplateService<String> notificationTemplateService,
      KafkaTemplate<String, Object> kafkaTemplate,
      String topic) {
    super(notificationTemplateService, kafkaTemplate, topic);
  }

  @Override
  public Channel getChannel() {
    return Channel.DIIA;
  }

  public DiiaNotificationMessageDto createMessageDto(Recipient recipient,
      UserNotificationMessageDto message) {
    var diiaChannel = getChannelObject(recipient.getChannels());
    var templateName = message.getNotification().getTemplateName();
    return DiiaNotificationMessageDto.builder()
        .context(message.getContext())
        .diiaNotificationDto(DiiaNotificationDto.builder()
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
    return notificationTemplateService.getTemplate(templateName, Channel.DIIA)
        .getExtTemplateId();
  }

  private List<KeyValue> mapToListKeyValue(Map<String, Object> parameters) {
    return parameters.entrySet().stream()
        .map(entry -> new KeyValue(entry.getKey(), (String) entry.getValue()))
        .collect(Collectors.toList());
  }
}
