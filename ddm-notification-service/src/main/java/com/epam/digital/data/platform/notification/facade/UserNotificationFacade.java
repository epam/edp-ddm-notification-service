/*
 * Copyright 2023 EPAM Systems.
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

import com.epam.digital.data.platform.notification.audit.UserNotificationAuditFacade;
import com.epam.digital.data.platform.notification.dto.ChannelObject;
import com.epam.digital.data.platform.notification.dto.Recipient;
import com.epam.digital.data.platform.notification.dto.UserNotificationMessageDto;
import com.epam.digital.data.platform.notification.exception.NotificationException;
import com.epam.digital.data.platform.notification.mapper.ChannelMapper;
import com.epam.digital.data.platform.notification.producer.NotificationProducer;
import com.epam.digital.data.platform.notification.service.UserService;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.epam.digital.data.platform.settings.model.dto.SettingsReadDto;
import com.epam.digital.data.platform.starter.audit.model.Step;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * The notification facade that responsible for sending notification to user
 */
@Slf4j
@Setter
@RequiredArgsConstructor
public class UserNotificationFacade {

  private static final String RECIPIENT_ROLES_ATTRIBUTE = "recipientRoles";

  private final UserService userService;
  private final UserNotificationAuditFacade auditFacade;
  private final Map<Channel, NotificationProducer> channelProducerMap;
  private final Map<Channel, ChannelMapper> channelMapperMap;

  /**
   * Send notification to user
   *
   * @param notificationRecord contains notification and context
   */
  public void sendNotification(UserNotificationMessageDto notificationRecord) {
    boolean ignoreChannelPreferences = isIgnoreChannelPreferences(notificationRecord);
    notifyEachRecipient(ignoreChannelPreferences, notificationRecord);
  }

  private void notifyEachRecipient(boolean ignorePref, UserNotificationMessageDto message) {
    verifyNotification(message);
    message.getRecipients().forEach(recipient -> {
      var channels = getChannels(ignorePref, recipient, recipient.getChannels(), message);
      log.info("Allowed communication channels {}", channels);
      var roles = userService.getUserRoles(recipient);
      recipient.getParameters().put(RECIPIENT_ROLES_ATTRIBUTE, roles);
      channels.forEach(
          channel -> sendNotification(channel, recipient, message));
    });
  }

  private void sendNotification(Channel channel, Recipient recipient,
      UserNotificationMessageDto message) {
    try {
      channelProducerMap.get(channel).send(recipient, message);
    } catch (RuntimeException exception) {
      auditFacade.sendAuditOnFailure(channel, message, Step.AFTER, exception.getMessage());
      log.error("Failed to send notification to channel ${}", channel, exception);
    }
  }

  private SettingsReadDto getUserSettings(Recipient recipient,
      UserNotificationMessageDto userNotificationMessageDto) {
    try {
      return userService.getUserSettings(recipient);
    } catch (RuntimeException ex) {
      auditFacade.sendAuditOnFailure(null, userNotificationMessageDto, Step.BEFORE,
          ex.getMessage());
      throw new NotificationException(ex.getMessage(), ex);
    }
  }

  private List<Channel> getChannelsFromSettings(Recipient recipient,
      UserNotificationMessageDto notificationRecord) {
    var userSettings = getUserSettings(recipient, notificationRecord);
    var channelObjects =
        userSettings.getChannels().stream()
            .filter(channelDto ->
                channelProducerMap.containsKey(channelDto.getChannel())
                    && channelDto.isActivated())
            .map(c -> channelMapperMap.get(c.getChannel()).map(c))
            .collect(Collectors.toList());
    recipient.setChannels(channelObjects);

    return channelObjects.stream()
        .map(c -> Channel.valueOf(c.getChannel().toUpperCase()))
        .collect(Collectors.toList());
  }

  private List<Channel> getChannels(boolean ignoreChannelPreferences, Recipient recipient,
      List<ChannelObject> channels, UserNotificationMessageDto notificationRecord) {
    if (ignoreChannelPreferences) {
      return channels.stream()
          .map(channelObject -> Channel.valueOf(channelObject.getChannel().toUpperCase()))
          .collect(Collectors.toList());
    }

    List<Channel> channelsToNotify = getDefaultChannels();
    channelsToNotify.addAll(this.getChannelsFromSettings(recipient, notificationRecord));
    return channelsToNotify;
  }

  private List<Channel> getDefaultChannels() {
    return new ArrayList<>(List.of(Channel.INBOX));
  }

  private boolean isIgnoreChannelPreferences(UserNotificationMessageDto notificationRecord) {
    if (notificationRecord.getNotification() == null) {
      var msg = "IgnoreChannelPreferences flag is not presented in message";
      auditFacade.sendAuditOnFailure(null, null, Step.BEFORE, msg);
      throw new NotificationException(msg);
    }
    return notificationRecord.getNotification().isIgnoreChannelPreferences();
  }

  private void verifyNotification(UserNotificationMessageDto userNotificationMessageDto) {
    if (Objects.isNull(userNotificationMessageDto.getNotification())) {
      var msg = "Notification is not presented in message";
      auditFacade.sendAuditOnFailure(null, null, Step.BEFORE, msg);
      throw new NotificationException(msg);
    }
  }
}
