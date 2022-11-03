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
import com.epam.digital.data.platform.notification.email.repository.NotificationTemplateRepository;
import com.epam.digital.data.platform.notification.entity.NotificationTemplate;
import com.epam.digital.data.platform.notification.repository.NotificationTemplateAttributeRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class SaveDefaultNotificationTemplateService
    extends AbstractSaveNotificationTemplateService {

  public SaveDefaultNotificationTemplateService(
      NotificationTemplateRepository notificationTemplateRepository,
      NotificationTemplateAttributeRepository notificationTemplateAttributeRepository) {
    super(notificationTemplateRepository, notificationTemplateAttributeRepository);
  }

  @Override
  @Transactional
  public SaveNotificationTemplateOutputDto save(
      String channel, String name, SaveNotificationTemplateInputDto inputDto) {
    log.info("Processing template {} for channel {}", name, channel);
    var template = saveTemplate(channel, name, inputDto);
    var attributes = saveAttributes(template.getId(), inputDto);
    return buildOutputDtoFromDb(template, attributes);
  }

  private NotificationTemplate buildTemplateFromInput(
      String channel, String name, SaveNotificationTemplateInputDto inputDto) {
    return NotificationTemplate.builder()
        .name(name)
        .channel(channel)
        .title(inputDto.getTitle())
        .content(inputDto.getContent())
        .checksum(DigestUtils.sha256Hex(inputDto.getContent()))
        .build();
  }

  private NotificationTemplate saveTemplate(
      String channel, String name, SaveNotificationTemplateInputDto inputDto) {
    var templateOpt = notificationTemplateRepository.findByNameAndChannel(name, channel);
    if (templateOpt.isEmpty()) {
      log.info("Template does not exist, creating new");
      var newTemplate = buildTemplateFromInput(channel, name, inputDto);
      return notificationTemplateRepository.save(newTemplate);
    } else {
      var template = templateOpt.get();
      var inputContentChecksum = DigestUtils.sha256Hex(inputDto.getContent());
      if (StringUtils.equals(inputContentChecksum, template.getChecksum())
          && StringUtils.equals(inputDto.getTitle(), template.getTitle())) {
        log.info("No update for template");
        return template;
      } else {
        log.info("Updating existing template");
        template.setContent(inputDto.getContent());
        template.setChecksum(inputContentChecksum);
        template.setTitle(inputDto.getTitle());
        return notificationTemplateRepository.save(template);
      }
    }
  }
}
