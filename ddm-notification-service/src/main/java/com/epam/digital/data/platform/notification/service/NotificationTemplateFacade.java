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

package com.epam.digital.data.platform.notification.service;

import com.epam.digital.data.platform.notification.dto.SaveNotificationTemplateInputDto;
import com.epam.digital.data.platform.notification.dto.SaveNotificationTemplateOutputDto;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class NotificationTemplateFacade {

  private final Map<Channel, SaveNotificationTemplateService> notificationTemplatesMap;

  public SaveNotificationTemplateOutputDto saveTemplate(
      String channel, String name, SaveNotificationTemplateInputDto inputDto) {
    var channelEnum = Channel.valueOf(channel.toUpperCase());
    var handler = notificationTemplatesMap.get(channelEnum);
    return handler.save(channel, name, inputDto);
  }
}
