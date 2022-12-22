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

package com.epam.digital.data.platform.notification.core.listener;

import com.epam.digital.data.platform.notification.audit.NotificationAuditFacade;
import com.epam.digital.data.platform.notification.dto.audit.NotificationMessageDto;
import com.epam.digital.data.platform.notification.exception.NotificationException;
import com.epam.digital.data.platform.notification.listener.NotificationListener;
import com.epam.digital.data.platform.notification.service.NotificationService;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.epam.digital.data.platform.starter.audit.model.Step;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractNotificationListener<T extends NotificationMessageDto> implements
    NotificationListener<T> {

  private final NotificationService<T> notificationService;
  private final NotificationAuditFacade<T> notificationAuditFacade;

  public void sendNotification(T message, Channel channel) {
    try {
      notificationService.notify(message);
      notificationAuditFacade.sendAuditOnSuccess(channel, message);
    } catch (RuntimeException exception) {
      notificationAuditFacade.sendAuditOnFailure(channel, message, Step.AFTER,
          exception.getMessage());
      throw new NotificationException(exception.getMessage(), exception);
    }
  }
}
