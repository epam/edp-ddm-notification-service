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

package com.epam.digital.data.platform.notification.producer;

import com.epam.digital.data.platform.notification.dto.ChannelObject;
import com.epam.digital.data.platform.notification.dto.Recipient;
import com.epam.digital.data.platform.notification.dto.UserNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.audit.NotificationMessageDto;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import java.util.List;

/**
 * Notification producer that sends message to specific channel topic.
 */
public interface NotificationProducer<T extends NotificationMessageDto> {

  void send(Recipient recipient, UserNotificationMessageDto message);

  Channel getChannel();

  T createMessageDto(Recipient recipient, UserNotificationMessageDto message);

  String getTitle(UserNotificationMessageDto message);

  ChannelObject getChannelObject(List<ChannelObject> channelObjectList);
}
