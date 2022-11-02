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

package com.epam.digital.data.platform.notification.inbox.listener;

import com.epam.digital.data.platform.notification.dto.inbox.InboxNotificationMessageDto;
import com.epam.digital.data.platform.notification.exception.NotificationException;
import com.epam.digital.data.platform.notification.inbox.audit.InboxNotificationAuditFacade;
import com.epam.digital.data.platform.notification.inbox.service.InboxNotificationService;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.epam.digital.data.platform.starter.audit.model.Step;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;

@Slf4j
@RequiredArgsConstructor
public class InboxNotificationListener {

  private final InboxNotificationService inboxNotificationService;
  private final InboxNotificationAuditFacade inboxNotificationAuditFacade;

  @KafkaListener(
      topics = "\u0023{kafkaProperties.topics['inbox-notifications']}",
      groupId = "\u0023{kafkaProperties.consumer.groupId}",
      containerFactory = "concurrentKafkaListenerContainerFactory")
  public void notify(InboxNotificationMessageDto message) {
    log.info("Kafka event received");
    sendNotification(message);
    log.info("Kafka event processed");
  }

  private void sendNotification(InboxNotificationMessageDto message) {
    try {
      inboxNotificationService.notify(message);
      inboxNotificationAuditFacade.sendAuditOnSuccess(Channel.INBOX, message);
    } catch (RuntimeException exception) {
      inboxNotificationAuditFacade.sendAuditOnFailure(Channel.INBOX, message, Step.AFTER,
          exception.getMessage());
      throw new NotificationException(exception.getMessage(), exception);
    }
  }
}
