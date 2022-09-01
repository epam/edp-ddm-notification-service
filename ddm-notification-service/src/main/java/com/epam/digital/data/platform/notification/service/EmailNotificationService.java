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

import com.epam.digital.data.platform.notification.dto.NotificationDto;
import com.epam.digital.data.platform.notification.template.FreemarkerTemplateResolver;
import com.epam.digital.data.platform.settings.model.dto.SettingsReadDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The notification service that uses email channel for sending notifications
 */
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationService implements NotificationService {

  private final EmailNotificationTemplateService templateService;
  private final FreemarkerTemplateResolver templateResolver;
  private final MailService mailService;

  @Override
  public void notify(NotificationDto notification, SettingsReadDto userSettings) {
    log.info("Sending notification via email");
    var template = templateService.getByName(notification.getTemplate());
    var body = templateResolver.resolve(notification.getTemplate(), template,
        notification.getTemplateModel());

    mailService.send(notification.getSubject(), body, userSettings.getEmail());
    log.info("Email notification was sent");
  }
}
