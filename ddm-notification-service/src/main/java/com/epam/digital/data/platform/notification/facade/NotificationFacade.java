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
import com.epam.digital.data.platform.notification.exception.NotificationException;
import com.epam.digital.data.platform.notification.service.NotificationService;
import com.epam.digital.data.platform.notification.service.UserSettingsService;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.epam.digital.data.platform.settings.model.dto.ChannelReadDto;
import com.epam.digital.data.platform.settings.model.dto.SettingsReadDto;
import com.epam.digital.data.platform.starter.audit.model.Step;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The notification facade that responsible for sending notification to user
 */
@Slf4j
@RequiredArgsConstructor
public class NotificationFacade {

  private static final EnumSet<Channel> IMPLEMENTED_CHANNELS = EnumSet.of(Channel.EMAIL);

  private final UserSettingsService userSettingsService;
  private final Map<Channel, NotificationService> notificationServiceMap;
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

  private void notify(List<Channel> channelsToNotify, NotificationDto notification,
      SettingsReadDto userSettings) {
    var userChannels = userSettings.getChannels()
                    .stream().filter(userChannel -> channelsToNotify.contains(userChannel.getChannel()))
                    .collect(Collectors.toList());
    userChannels.forEach(channel -> send(notification, channel));
  }

  private List<Channel> getChannels(SettingsReadDto userSettings) {
    var result =
        userSettings.getChannels().stream()
            .filter(
                channelDto ->
                    IMPLEMENTED_CHANNELS.contains(channelDto.getChannel())
                        && channelDto.isActivated())
            .map(ChannelReadDto::getChannel)
            .collect(Collectors.toList());
    log.info("Allowed communication channels {}", result);
    return result;
  }

  private void send(NotificationDto notification, ChannelReadDto userChannel) {
    try {
      var notificationService = notificationServiceMap.get(userChannel.getChannel());
      notificationService.notify(notification, userChannel);
      auditFacade.sendAuditOnSuccess(userChannel.getChannel(), notification);
    } catch (RuntimeException exception) {
      auditFacade.sendAuditOnFailure(userChannel.getChannel(), notification, Step.AFTER, exception.getMessage());
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
