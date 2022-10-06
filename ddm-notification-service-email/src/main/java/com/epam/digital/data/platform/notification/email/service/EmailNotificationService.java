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

package com.epam.digital.data.platform.notification.email.service;

import com.epam.digital.data.platform.notification.dto.email.EmailNotificationMessageDto;
import com.epam.digital.data.platform.notification.email.template.FreemarkerTemplateResolver;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class EmailNotificationService {

  private final EmailNotificationTemplateService templateService;
  private final FreemarkerTemplateResolver templateResolver;
  private final MailService mailService;

  public void notify(EmailNotificationMessageDto message) {
    log.info("Sending notification via email");
    mailService.send(
        message.getNotification().getSubject(),
        message.getNotification().getMessage(),
        message.getRecipient().getEmail());
    log.info("Email notification was sent");
  }

  public String prepareEmailBody(String templateName, Map<String, Object> data) {
    var template = templateService.getByNameAndChannel(templateName, Channel.EMAIL);
    return templateResolver.resolve(templateName, template, data);
  }
}
