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

package com.epam.digital.data.platform.notification.email.listener;

import com.epam.digital.data.platform.notification.audit.NotificationAuditFacade;
import com.epam.digital.data.platform.notification.core.listener.AbstractNotificationListener;
import com.epam.digital.data.platform.notification.dto.email.EmailNotificationMessageDto;
import com.epam.digital.data.platform.notification.service.NotificationService;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;

@Slf4j
public class EmailNotificationListener extends
    AbstractNotificationListener<EmailNotificationMessageDto> {

  public EmailNotificationListener(
      NotificationService<EmailNotificationMessageDto> notificationService,
      NotificationAuditFacade<EmailNotificationMessageDto> notificationAuditFacade) {
    super(notificationService, notificationAuditFacade);
  }

  @KafkaListener(
      topics = "\u0023{kafkaProperties.topics['email-notifications']}",
      groupId = "\u0023{kafkaProperties.consumer.groupId}",
      containerFactory = "concurrentKafkaListenerContainerFactory")
  public void notify(EmailNotificationMessageDto message) {
    log.info("Kafka event received");
    sendNotification(message, Channel.EMAIL);
    log.info("Kafka event processed");
  }
}
