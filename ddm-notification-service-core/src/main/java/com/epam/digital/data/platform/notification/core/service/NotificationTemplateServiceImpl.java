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
package com.epam.digital.data.platform.notification.core.service;

import com.epam.digital.data.platform.notification.entity.NotificationTemplate;
import com.epam.digital.data.platform.notification.exception.NotificationTemplateNotFoundException;
import com.epam.digital.data.platform.notification.repository.NotificationTemplateRepository;
import com.epam.digital.data.platform.notification.template.NotificationTemplateService;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class NotificationTemplateServiceImpl implements NotificationTemplateService<String> {

  private final NotificationTemplateRepository repository;

  @Override
  public String getContentByNameAndChannel(String templateName, Channel channel) {
    log.info("Getting {} template", templateName);
    var template = getTemplate(templateName, channel);
    log.info("Template {} found", templateName);
    return template.getContent();
  }

  @Override
  public String getTitleByNameAndChannel(String templateName, Channel channel) {
    log.info("Getting template title from {}", templateName);
    var template = getTemplate(templateName, channel);
    log.info("Template title is found from {}", templateName);
    return template.getTitle();
  }

  @Override
  public NotificationTemplate getTemplate(String templateName, Channel channel) {
    return repository.findByNameAndChannel(templateName, channel.getValue())
        .orElseThrow(() -> new NotificationTemplateNotFoundException(templateName));
  }
}