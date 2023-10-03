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

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.notification.DdmNotificationServiceApplication;
import com.epam.digital.data.platform.notification.core.repository.CoreNotificationTemplateRepository;
import com.epam.digital.data.platform.notification.dto.NotificationTemplateAttributeDto;
import com.epam.digital.data.platform.notification.dto.SaveNotificationTemplateInputDto;
import com.epam.digital.data.platform.notification.entity.NotificationTemplate;
import com.epam.digital.data.platform.notification.entity.NotificationTemplateAttribute;
import com.epam.digital.data.platform.notification.repository.NotificationTemplateAttributeRepository;
import java.util.Collections;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@Transactional
@SpringBootTest(properties = {"notifications.enabled=false"}, classes = DdmNotificationServiceApplication.class)
class SaveDefaultNotificationTemplateServiceTest {

  private static final String CHANNEL = "email";
  private static final String NAME = "template";
  private static final String TITLE = "title";

  @Autowired
  private SaveDefaultNotificationTemplateService service;

  @Autowired
  private CoreNotificationTemplateRepository notificationTemplateRepository;

  @Autowired
  private NotificationTemplateAttributeRepository notificationTemplateAttributeRepository;

  @Test
  void expectNewTemplateIsCreatedIfNotExists() {
    var inputDto =
        SaveNotificationTemplateInputDto.builder()
            .title(TITLE)
            .content("<html><h1>Hello</h1></html>")
            .build();

    service.save(CHANNEL, NAME, inputDto);

    var templateTableContent = notificationTemplateRepository
            .findByNameAndChannel(NAME, CHANNEL).get();
    assertThat(templateTableContent).isNotNull();
    assertThat(templateTableContent.getId()).isNotNull();
    assertThat(templateTableContent.getChannel()).isEqualTo(CHANNEL);
    assertThat(templateTableContent.getName()).isEqualTo(NAME);
    assertThat(templateTableContent.getTitle()).isEqualTo(TITLE);
    assertThat(templateTableContent.getContent()).isEqualTo("<html><h1>Hello</h1></html>");
    assertThat(templateTableContent.getChecksum())
        .isEqualTo("40c6d08f4bbd1fe4ac7f66239a7ca777dc7b0c449d4b459c523ad9eefab54abc");
    assertThat(templateTableContent.getCreatedAt()).isNotNull();
    assertThat(templateTableContent.getUpdatedAt()).isNotNull();
    assertThat(templateTableContent.getExtTemplateId()).isNull();
    assertThat(templateTableContent.getExtPublishedAt()).isNull();

    var attributesTableContent =
        notificationTemplateAttributeRepository.findByTemplateId(templateTableContent.getId());
    assertThat(attributesTableContent).isEmpty();
  }

  @Test
  void expectTemplateIsUpdatedIfNewContent() {
    var prepopulatedEntity =
        notificationTemplateRepository.save(createTemplate("<html><h1>Hello</h1></html>"));
    var inputDto =
            SaveNotificationTemplateInputDto.builder()
                    .title(TITLE)
                    .content("<html><h1>Updated content</h1></html>")
                    .build();

    service.save("email", "template", inputDto);

    var templateTableContent = notificationTemplateRepository
            .findByNameAndChannel("template", "email").get();
    assertThat(templateTableContent).isNotNull();
    assertThat(templateTableContent.getId()).isEqualTo(prepopulatedEntity.getId());
    assertThat(templateTableContent.getChannel()).isEqualTo(CHANNEL);
    assertThat(templateTableContent.getName()).isEqualTo(NAME);
    assertThat(templateTableContent.getTitle()).isEqualTo(TITLE);
    assertThat(templateTableContent.getContent()).isEqualTo("<html><h1>Updated content</h1></html>");
    assertThat(templateTableContent.getChecksum())
            .isEqualTo("c7028d4e3a2f4cf10322fc39c84af41f0be397b63cc3a4f1b76867a29bf9ab8f");
    assertThat(templateTableContent.getCreatedAt()).isNotNull();
    assertThat(templateTableContent.getUpdatedAt()).isNotNull();
    assertThat(templateTableContent.getExtTemplateId()).isNull();
    assertThat(templateTableContent.getExtPublishedAt()).isNull();

    var attributesTableContent =
            notificationTemplateAttributeRepository.findByTemplateId(templateTableContent.getId());
    assertThat(attributesTableContent).isEmpty();
  }

  @Test
  void expectTemplateAttributesCreated() {
    var prepopulatedTemplate =
            notificationTemplateRepository.save(createTemplate("<html><h1>Hello</h1></html>"));
    var inputDto =
            SaveNotificationTemplateInputDto.builder()
                    .title(TITLE)
                    .content("<html><h1>Hello</h1></html>")
                    .attributes(Collections.singletonList(new NotificationTemplateAttributeDto("name", "value")))
                    .build();

    service.save(CHANNEL, NAME, inputDto);

    var templateTableContent = notificationTemplateRepository
            .findByNameAndChannel(NAME, CHANNEL).get();
    assertThat(templateTableContent).isNotNull();
    assertThat(templateTableContent.getId()).isEqualTo(prepopulatedTemplate.getId());
    assertThat(templateTableContent.getChannel()).isEqualTo(CHANNEL);
    assertThat(templateTableContent.getName()).isEqualTo(NAME);
    assertThat(templateTableContent.getTitle()).isEqualTo(TITLE);
    assertThat(templateTableContent.getContent()).isEqualTo("<html><h1>Hello</h1></html>");
    assertThat(templateTableContent.getChecksum())
            .isEqualTo("40c6d08f4bbd1fe4ac7f66239a7ca777dc7b0c449d4b459c523ad9eefab54abc");
    assertThat(templateTableContent.getCreatedAt()).isNotNull();
    assertThat(templateTableContent.getUpdatedAt()).isNotNull();
    assertThat(templateTableContent.getExtTemplateId()).isNull();
    assertThat(templateTableContent.getExtPublishedAt()).isNull();

    var attributesTableContent =
            notificationTemplateAttributeRepository.findByTemplateId(templateTableContent.getId());
    assertThat(attributesTableContent).hasSize(1);
    assertThat(attributesTableContent.get(0).getId()).isNotNull();
    assertThat(attributesTableContent.get(0).getTemplateId()).isEqualTo(prepopulatedTemplate.getId());
    assertThat(attributesTableContent.get(0).getName()).isEqualTo("name");
    assertThat(attributesTableContent.get(0).getValue()).isEqualTo("value");
  }

  @Test
  void expectTemplateAttributesUpdated() {
    var prepopulatedTemplate =
        notificationTemplateRepository.save(createTemplate("<html><h1>Hello</h1></html>"));
    notificationTemplateAttributeRepository.saveAll(
        List.of(
            NotificationTemplateAttribute.builder()
                .templateId(prepopulatedTemplate.getId())
                .name("name1")
                .value("value1")
                .build(),
            NotificationTemplateAttribute.builder()
                .templateId(prepopulatedTemplate.getId())
                .name("name2")
                .value("value2")
                .build()));
    var inputDto =
        SaveNotificationTemplateInputDto.builder()
            .title(TITLE)
            .content("<html><h1>Hello</h1></html>")
            .attributes(
                List.of(
                    new NotificationTemplateAttributeDto("name1", "newValue"),
                    new NotificationTemplateAttributeDto("name3", "value3")))
            .build();

    service.save(CHANNEL, NAME, inputDto);

    var templateTableContent =
        notificationTemplateRepository.findByNameAndChannel(NAME, CHANNEL).get();
    assertThat(templateTableContent).isNotNull();
    assertThat(templateTableContent.getId()).isEqualTo(prepopulatedTemplate.getId());
    assertThat(templateTableContent.getChannel()).isEqualTo(CHANNEL);
    assertThat(templateTableContent.getName()).isEqualTo(NAME);
    assertThat(templateTableContent.getTitle()).isEqualTo(TITLE);
    assertThat(templateTableContent.getContent()).isEqualTo("<html><h1>Hello</h1></html>");
    assertThat(templateTableContent.getChecksum())
        .isEqualTo("40c6d08f4bbd1fe4ac7f66239a7ca777dc7b0c449d4b459c523ad9eefab54abc");
    assertThat(templateTableContent.getCreatedAt()).isNotNull();
    assertThat(templateTableContent.getUpdatedAt()).isNotNull();
    assertThat(templateTableContent.getExtTemplateId()).isNull();
    assertThat(templateTableContent.getExtPublishedAt()).isNull();

    var attributesTableContent =
        notificationTemplateAttributeRepository.findByTemplateId(templateTableContent.getId());
    assertThat(attributesTableContent).hasSize(2);
    assertThat(attributesTableContent.get(0).getId()).isNotNull();
    assertThat(attributesTableContent.get(0).getTemplateId())
        .isEqualTo(prepopulatedTemplate.getId());
    assertThat(attributesTableContent.get(0).getName()).isEqualTo("name1");
    assertThat(attributesTableContent.get(0).getValue()).isEqualTo("newValue");
    assertThat(attributesTableContent.get(1).getId()).isNotNull();
    assertThat(attributesTableContent.get(1).getTemplateId())
        .isEqualTo(prepopulatedTemplate.getId());
    assertThat(attributesTableContent.get(1).getName()).isEqualTo("name3");
    assertThat(attributesTableContent.get(1).getValue()).isEqualTo("value3");
  }

  private NotificationTemplate createTemplate(String content) {
    return NotificationTemplate.builder()
            .name(NAME)
            .channel(CHANNEL)
            .title(TITLE)
            .content(content)
            .checksum(DigestUtils.sha256Hex(content))
            .build();
  }
}
