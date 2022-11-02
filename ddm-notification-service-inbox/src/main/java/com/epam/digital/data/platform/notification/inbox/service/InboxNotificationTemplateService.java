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

package com.epam.digital.data.platform.notification.inbox.service;

import com.epam.digital.data.platform.notification.entity.NotificationTemplate;
import com.epam.digital.data.platform.notification.exception.NotificationTemplateNotFoundException;
import com.epam.digital.data.platform.notification.inbox.repository.InboxNotificationTemplateRepository;
import com.epam.digital.data.platform.notification.template.NotificationTemplateService;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InboxNotificationTemplateService implements NotificationTemplateService<String> {

  private final InboxNotificationTemplateRepository repository;

  @Override
  public String getByName(String templateName) {
    log.info("Getting {} template", templateName);
    var template = getTemplateByName(templateName);
    log.info("Template {} found", templateName);
    return template.getContent();
  }

  @Override
  public String getTitleByTemplateName(String templateName) {
    log.info("Getting template title from {}", templateName);
    var template = getTemplateByName(templateName);
    log.info("Template title is found from {}", templateName);
    return template.getTitle();
  }

  @Override
  public String getByNameAndChannel(String name, Channel channel) {
    log.info("Getting {} template", name);
    var template = repository.findByNameAndChannel(name, channel.getValue())
        .orElseThrow(() -> new NotificationTemplateNotFoundException(name));
    log.info("Template {} found", name);
    return template.getContent();
  }

  private NotificationTemplate getTemplateByName(String templateName) {
    return repository.findByNameAndChannel(templateName, Channel.INBOX.getValue())
        .orElseThrow(() -> new NotificationTemplateNotFoundException(templateName));
  }
}
