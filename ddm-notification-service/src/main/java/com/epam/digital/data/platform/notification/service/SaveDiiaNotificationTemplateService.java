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

import static com.epam.digital.data.platform.notification.utils.TemplateAttributes.ACTION_TYPE;
import static com.epam.digital.data.platform.notification.utils.TemplateAttributes.SHORT_TEXT;
import static com.epam.digital.data.platform.notification.utils.TemplateAttributes.TEMPLATE_TYPE;

import com.epam.digital.data.platform.notification.core.repository.CoreNotificationTemplateRepository;
import com.epam.digital.data.platform.notification.diia.service.DiiaService;
import com.epam.digital.data.platform.notification.dto.NotificationTemplateAttributeDto;
import com.epam.digital.data.platform.notification.dto.SaveNotificationTemplateInputDto;
import com.epam.digital.data.platform.notification.dto.SaveNotificationTemplateOutputDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaPublishTemplateRequestDto;
import com.epam.digital.data.platform.notification.entity.NotificationTemplate;
import com.epam.digital.data.platform.notification.entity.NotificationTemplateAttribute;
import com.epam.digital.data.platform.notification.repository.NotificationTemplateAttributeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class SaveDiiaNotificationTemplateService
    extends AbstractSaveNotificationTemplateService {

  private final DiiaService diiaService;
  private final ObjectMapper objectMapper;

  public SaveDiiaNotificationTemplateService(
      CoreNotificationTemplateRepository notificationTemplateRepository,
      NotificationTemplateAttributeRepository notificationTemplateAttributeRepository,
        DiiaService diiaService, ObjectMapper objectMapper) {
    super(notificationTemplateRepository, notificationTemplateAttributeRepository);
    this.diiaService = diiaService;
    this.objectMapper = objectMapper;
  }

  @SneakyThrows
  @Override
  @Transactional
  public SaveNotificationTemplateOutputDto save(
      String channel, String name, SaveNotificationTemplateInputDto inputDto) {
    log.info("Processing template {} for channel {}", name, channel);

    var diiaPublishTemplateRequestDto = createRequestDto(inputDto);
    var checksum = DigestUtils.sha256Hex(
        objectMapper.writeValueAsString(diiaPublishTemplateRequestDto));

    NotificationTemplate template;
    List<NotificationTemplateAttribute> attributes;

    var templateOpt = notificationTemplateRepository.findByNameAndChannel(name, channel);
    if (templateOpt.isEmpty() || !Objects.equals(templateOpt.get().getChecksum(), checksum)) {
      var externalTemplateId =
          diiaService.publishTemplate(diiaPublishTemplateRequestDto).getTemplateId();
      template = saveTemplate(channel, name, inputDto, externalTemplateId, templateOpt, checksum);
      attributes = saveAttributes(template.getId(), inputDto);
    } else {
      template = templateOpt.get();
      attributes = notificationTemplateAttributeRepository.findByTemplateId(template.getId());
    }
    return buildOutputDtoFromDb(template, attributes);
  }

  private DiiaPublishTemplateRequestDto createRequestDto(
      SaveNotificationTemplateInputDto inputDto) {
    Map<String, String> attributesMap = Map.of();
    if(inputDto.getAttributes() != null) {
      attributesMap = inputDto.getAttributes().stream()
          .collect(
              Collectors.toMap(
                  NotificationTemplateAttributeDto::getName,
                  NotificationTemplateAttributeDto::getValue));
    }
    return DiiaPublishTemplateRequestDto.builder()
        .actionType(attributesMap.get(ACTION_TYPE))
        .templateType(attributesMap.get(TEMPLATE_TYPE))
        .title(inputDto.getTitle())
        .shortText(attributesMap.get(SHORT_TEXT))
        .fullText(inputDto.getContent())
        .build();
  }

  private NotificationTemplate saveTemplate(
      String channel, String name, SaveNotificationTemplateInputDto inputDto,
      String extTemplateId, Optional<NotificationTemplate> templateOpt, String checksum) {

    NotificationTemplate template;
    if (templateOpt.isEmpty()) {
      log.info("Template does not exist, creating new");
      template = new NotificationTemplate();
    } else {
      log.info("Updating existing template");
      template = templateOpt.get();
    }
    template.setName(name);
    template.setChannel(channel);
    template.setTitle(inputDto.getTitle());
    template.setContent(inputDto.getContent());
    template.setExtTemplateId(extTemplateId);
    template.setExtPublishedAt(LocalDateTime.now());
    template.setChecksum(checksum);
    return notificationTemplateRepository.save(template);
  }
}
