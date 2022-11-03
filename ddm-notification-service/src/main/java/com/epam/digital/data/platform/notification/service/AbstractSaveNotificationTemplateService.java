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

import com.epam.digital.data.platform.notification.dto.NotificationTemplateAttributeDto;
import com.epam.digital.data.platform.notification.dto.SaveNotificationTemplateInputDto;
import com.epam.digital.data.platform.notification.dto.SaveNotificationTemplateOutputDto;
import com.epam.digital.data.platform.notification.email.repository.NotificationTemplateRepository;
import com.epam.digital.data.platform.notification.entity.NotificationTemplate;
import com.epam.digital.data.platform.notification.entity.NotificationTemplateAttribute;
import com.epam.digital.data.platform.notification.repository.NotificationTemplateAttributeRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractSaveNotificationTemplateService
    implements SaveNotificationTemplateService {

  protected final NotificationTemplateRepository notificationTemplateRepository;
  protected final NotificationTemplateAttributeRepository notificationTemplateAttributeRepository;

  protected SaveNotificationTemplateOutputDto buildOutputDtoFromDb(
      NotificationTemplate template, List<NotificationTemplateAttribute> attributes) {
    return SaveNotificationTemplateOutputDto.builder()
        .name(template.getName())
        .channel(template.getChannel())
        .title(template.getTitle())
        .content(template.getContent())
        .checksum(template.getChecksum())
        .createdAt(template.getCreatedAt())
        .updatedAt(template.getUpdatedAt())
        .externalTemplateId(template.getExtTemplateId())
        .externallyPublishedAt(template.getExtPublishedAt())
        .attributes(
            attributes.stream()
                .map(
                    attr ->
                        NotificationTemplateAttributeDto.builder()
                            .name(attr.getName())
                            .value(attr.getValue())
                            .build())
                .collect(Collectors.toList()))
        .build();
  }

  protected List<NotificationTemplateAttribute> saveAttributes(UUID templateId,
      SaveNotificationTemplateInputDto inputDto) {
    var existingAttributes =
        notificationTemplateAttributeRepository.findByTemplateId(templateId).stream()
            .collect(
                Collectors.toMap(
                    NotificationTemplateAttribute::getName,
                    Function.identity()));
    var newAttributes =
        Optional.ofNullable(inputDto.getAttributes())
            .stream()
            .flatMap(Collection::stream)
            .collect(
                Collectors.toMap(
                    NotificationTemplateAttributeDto::getName,
                    NotificationTemplateAttributeDto::getValue,
                    (k1, k2) -> k2));
    newAttributes.forEach((name, newValue) -> {
      var existingAttrEntity = existingAttributes.get(name);
      if (existingAttrEntity == null) {
        var newAttr = new NotificationTemplateAttribute();
        newAttr.setTemplateId(templateId);
        newAttr.setName(name);
        newAttr.setValue(newValue);
        existingAttributes.put(name, notificationTemplateAttributeRepository.save(newAttr));
      } else if (!newValue.equals(existingAttrEntity.getValue())) {
        existingAttrEntity.setValue(newValue);
        existingAttributes.put(name,
            notificationTemplateAttributeRepository.save(existingAttrEntity));
      }
    });
    var outdatedAttributes = existingAttributes.values().stream()
        .filter(attr -> newAttributes.get(attr.getName()) == null)
        .collect(Collectors.toList());
    notificationTemplateAttributeRepository.deleteAll(outdatedAttributes);
    return new ArrayList<>(notificationTemplateAttributeRepository.findByTemplateId(templateId));
  }
}
