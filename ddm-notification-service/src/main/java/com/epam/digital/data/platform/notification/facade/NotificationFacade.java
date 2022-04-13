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

package com.epam.digital.data.platform.notification.facade;

import com.epam.digital.data.platform.notification.audit.NotificationAuditFacade;
import com.epam.digital.data.platform.notification.dto.NotificationDto;
import com.epam.digital.data.platform.notification.dto.NotificationRecordDto;
import com.epam.digital.data.platform.notification.enums.NotificationChannel;
import com.epam.digital.data.platform.notification.exception.NotificationException;
import com.epam.digital.data.platform.notification.service.NotificationService;
import com.epam.digital.data.platform.notification.service.UserSettingsService;
import com.epam.digital.data.platform.settings.model.dto.SettingsReadDto;
import com.epam.digital.data.platform.starter.audit.model.Step;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The notification facade that responsible for sending notification to user
 */
@Slf4j
@RequiredArgsConstructor
public class NotificationFacade {

  private final UserSettingsService userSettingsService;
  private final Map<String, NotificationService> notificationServiceMap;
  private final NotificationAuditFacade auditFacade;

  /**
   * Send notification to user
   *
   * @param notificationRecord contains notification and context
   */
  public void sendNotification(NotificationRecordDto notificationRecord) {
    log.info("Sending notification");
    log.info("Notification context {}", notificationRecord.getContext());
    var notification = getNotification(notificationRecord);
    var userSettings = getUserSettings(notification);
    var channels = getChannels(userSettings);

    notify(channels, notification, userSettings);
    log.info("Notification process finished");
  }

  private void notify(List<String> channels, NotificationDto notification,
      SettingsReadDto userSettings) {
    channels.forEach(channel -> send(channel, notification, userSettings));
  }

  private List<String> getChannels(SettingsReadDto userSettings) {
    if (!userSettings.isCommunicationAllowed()) {
      log.info("Communication is not allowed");
      return Collections.emptyList();
    }
    var result = List.of(NotificationChannel.EMAIL.getName());
    log.info("Allowed communication channels {}", result);
    return result;
  }

  private void send(String channel, NotificationDto notification, SettingsReadDto userSettings) {
    try {
      var notificationService = notificationServiceMap.get(channel);
      notificationService.notify(notification, userSettings);
      auditFacade.sendAuditOnSuccess(channel, notification);
    } catch (RuntimeException exception) {
      auditFacade.sendAuditOnFailure(channel, notification, Step.AFTER, exception.getMessage());
      throw new NotificationException(exception.getMessage(), exception);
    }
  }

  private NotificationDto getNotification(NotificationRecordDto notificationRecordDto) {
    var notification = notificationRecordDto.getNotification();
    if (Objects.isNull(notification)) {
      var msg = "Notification is not presented in record";
      auditFacade.sendAuditOnFailure(null, null, Step.BEFORE, msg);
      throw new NotificationException(msg);
    }
    return notification;
  }

  private SettingsReadDto getUserSettings(NotificationDto notification) {
    try {
      return userSettingsService.getByUsername(notification.getRecipient());
    } catch (RuntimeException ex) {
      auditFacade.sendAuditOnFailure(null, notification, Step.BEFORE, ex.getMessage());
      throw new NotificationException(ex.getMessage(), ex);
    }
  }
}
