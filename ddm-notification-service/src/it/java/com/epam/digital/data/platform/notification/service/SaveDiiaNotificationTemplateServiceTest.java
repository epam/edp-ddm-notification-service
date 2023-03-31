/*
 * Copyright 2023 EPAM Systems.
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
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.notification.config.WireMockContextInitializer;
import com.epam.digital.data.platform.notification.diia.repository.DiiaNotificationTemplateRepository;
import com.epam.digital.data.platform.notification.diia.service.DiiaService;
import com.epam.digital.data.platform.notification.dto.NotificationTemplateAttributeDto;
import com.epam.digital.data.platform.notification.dto.SaveNotificationTemplateInputDto;
import com.epam.digital.data.platform.notification.entity.NotificationTemplate;
import com.epam.digital.data.platform.notification.entity.NotificationTemplateAttribute;
import com.epam.digital.data.platform.notification.repository.NotificationTemplateAttributeRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@Transactional
@SpringBootTest(properties = {"notifications.enabled=false"})
@ContextConfiguration(initializers = {
    WireMockContextInitializer.class})
class SaveDiiaNotificationTemplateServiceTest {

  private static final String CHANNEL = "diia";
  private static final String NAME_1 = "diia-template";
  private static final String NAME_2 = "diia-template-2";
  private static final String NAME_3 = "diia-template-3";
  private static final String TITLE = "diia-title";
  private static final String CONTENT = "some long text";

  @Autowired
  SaveDiiaNotificationTemplateService service;
  @Autowired
  DiiaNotificationTemplateRepository notificationTemplateRepository;
  @Autowired
  NotificationTemplateAttributeRepository notificationTemplateAttributeRepository;
  @Autowired
  WireMockServer diiaWireMock;

  @BeforeEach
  void beforeEach() {
    diiaWireMock.resetAll();
    diiaWireMock.
        stubFor(get(urlEqualTo("/api/v1/auth/partner/partnerToken"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("{\"token\":\"diia_access_token\"}")
            ));
  }

  @Test
  void expectNewTemplateAndAttributesAreCreatedIfNotExists() {
    var inputDto =
        SaveNotificationTemplateInputDto.builder()
            .title(TITLE)
            .content("some long text")
            .attributes(
                List.of(
                    new NotificationTemplateAttributeDto(ACTION_TYPE, "DiiaActionType"),
                    new NotificationTemplateAttributeDto(TEMPLATE_TYPE, "DiiaTemplateType"),
                    new NotificationTemplateAttributeDto(SHORT_TEXT, "DiiaShortText")
                )).build();

    diiaWireMock.
        stubFor(post(urlEqualTo("/api/v1/notification/template"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("{\"templateId\":\"12345\"}")
            ));

    service.save(CHANNEL, NAME_1, inputDto);

    var templateTableContent = notificationTemplateRepository
        .findByNameAndChannel(NAME_1, CHANNEL).get();
    assertThat(templateTableContent).isNotNull();
    assertThat(templateTableContent.getId()).isNotNull();
    assertThat(templateTableContent.getChannel()).isEqualTo(CHANNEL);
    assertThat(templateTableContent.getName()).isEqualTo(NAME_1);
    assertThat(templateTableContent.getTitle()).isEqualTo(TITLE);
    assertThat(templateTableContent.getContent()).isEqualTo(CONTENT);
    assertThat(templateTableContent.getChecksum())
        .isEqualTo("b873ee130819ed542ec8ce28ea74a5830ad17756c55557e5f21e30efbcf57785");
    assertThat(templateTableContent.getCreatedAt()).isNotNull();
    assertThat(templateTableContent.getUpdatedAt()).isNotNull();
    assertThat(templateTableContent.getExtTemplateId()).isEqualTo("12345");
    assertThat(templateTableContent.getExtPublishedAt()).isNotNull();

    var attributesTableContent =
        notificationTemplateAttributeRepository.findByTemplateId(templateTableContent.getId());
    assertThat(attributesTableContent.stream()
        .collect(
            toMap(NotificationTemplateAttribute::getName, NotificationTemplateAttribute::getValue)))
        .containsExactly(
            Map.entry("templateType", "DiiaTemplateType"),
            Map.entry("actionType", "DiiaActionType"),
            Map.entry("shortText", "DiiaShortText"));
  }

  @Test
  void expectTemplateIsUpdatedIfNewContent() {
    var prepopulatedEntity =
        notificationTemplateRepository.save(createTemplate(NAME_2, CONTENT,
            "b873ee130819ed542ec8ce28ea74a5830ad17756c55557e5f21e30efbcf57785"));
    var inputDto =
        SaveNotificationTemplateInputDto.builder()
            .title(TITLE)
            .content("Updated content")
            .build();

    diiaWireMock.
        stubFor(post(urlEqualTo("/api/v1/notification/template"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("{\"templateId\":\"5555\"}")
            ));

    service.save(CHANNEL, NAME_2, inputDto);

    var templateTableContent = notificationTemplateRepository
        .findByNameAndChannel(NAME_2, CHANNEL).get();
    assertThat(templateTableContent).isNotNull();
    assertThat(templateTableContent.getId()).isEqualTo(prepopulatedEntity.getId());
    assertThat(templateTableContent.getChannel()).isEqualTo(CHANNEL);
    assertThat(templateTableContent.getName()).isEqualTo(NAME_2);
    assertThat(templateTableContent.getTitle()).isEqualTo(TITLE);
    assertThat(templateTableContent.getContent()).isEqualTo("Updated content");
    assertThat(templateTableContent.getChecksum())
        .isEqualTo("0f590c85b54774764456994cd1db7f87c58671cd2039f5735cd40d823bc8c68f");
    assertThat(templateTableContent.getCreatedAt()).isNotNull();
    assertThat(templateTableContent.getUpdatedAt()).isNotNull();
    assertThat(templateTableContent.getExtTemplateId()).isEqualTo("5555");
    assertThat(templateTableContent.getExtPublishedAt()).isNotNull();

    var attributesTableContent =
        notificationTemplateAttributeRepository.findByTemplateId(templateTableContent.getId());
    assertThat(attributesTableContent).isEmpty();
  }

  @Test
  void expectTemplateAndAttributesAreUpdatedIfNewAttributeValue() {
    var prepopulatedEntity =
        notificationTemplateRepository.save(createTemplate(NAME_3, CONTENT,
            "b873ee130819ed542ec8ce28ea74a5830ad17756c55557e5f21e30efbcf57785"));

    notificationTemplateAttributeRepository.saveAll(
        List.of(
            NotificationTemplateAttribute.builder()
                .templateId(prepopulatedEntity.getId())
                .name("templateType")
                .value("DiiaTemplateType")
                .build(),
            NotificationTemplateAttribute.builder()
                .templateId(prepopulatedEntity.getId())
                .name("actionType")
                .value("DiiaActionType")
                .build(),
            NotificationTemplateAttribute.builder()
                .templateId(prepopulatedEntity.getId())
                .name("shortText")
                .value("DiiaShortText")
                .build()
        ));

    var inputDto =
        SaveNotificationTemplateInputDto.builder()
            .title(TITLE)
            .content(CONTENT)
            .attributes(
                List.of(
                    new NotificationTemplateAttributeDto("templateType", "DiiaTemplateType"),
                    new NotificationTemplateAttributeDto("actionType", "UpdatedActionType")
                )
            ).build();

    diiaWireMock.
        stubFor(post(urlEqualTo("/api/v1/notification/template"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("{\"templateId\":\"31337\"}")
            ));

    service.save(CHANNEL, NAME_3, inputDto);

    var templateTableContent = notificationTemplateRepository
        .findByNameAndChannel(NAME_3, CHANNEL).get();
    assertThat(templateTableContent).isNotNull();
    assertThat(templateTableContent.getId()).isEqualTo(prepopulatedEntity.getId());
    assertThat(templateTableContent.getChannel()).isEqualTo(CHANNEL);
    assertThat(templateTableContent.getName()).isEqualTo(NAME_3);
    assertThat(templateTableContent.getTitle()).isEqualTo(TITLE);
    assertThat(templateTableContent.getContent()).isEqualTo(CONTENT);
    assertThat(templateTableContent.getChecksum())
        .isEqualTo("692fa32344032c2e23a5d72433edca1be4c9e18c4064f3cc794d1f802e4daa39");
    assertThat(templateTableContent.getCreatedAt()).isNotNull();
    assertThat(templateTableContent.getUpdatedAt()).isNotNull();
    assertThat(templateTableContent.getExtTemplateId()).isEqualTo("31337");
    assertThat(templateTableContent.getExtPublishedAt()).isNotNull();

    var attributesTableContent =
        notificationTemplateAttributeRepository.findByTemplateId(templateTableContent.getId());
    assertThat(attributesTableContent.stream()
        .collect(
            toMap(NotificationTemplateAttribute::getName, NotificationTemplateAttribute::getValue)))
        .containsExactly(
            Map.entry("templateType", "DiiaTemplateType"),
            Map.entry("actionType", "UpdatedActionType"));
  }

  private NotificationTemplate createTemplate(String name, String content, String checksum) {
    return NotificationTemplate.builder()
        .name(name)
        .channel(CHANNEL)
        .title(TITLE)
        .content(content)
        .checksum(checksum)
        .extTemplateId("12345")
        .build();
  }
}
