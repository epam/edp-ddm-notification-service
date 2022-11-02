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

package com.epam.digital.data.platform.notification.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.digital.data.platform.notification.dto.NotificationTemplateAttributeDto;
import com.epam.digital.data.platform.notification.dto.SaveNotificationTemplateInputDto;
import com.epam.digital.data.platform.notification.email.repository.NotificationTemplateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "notifications.enabled=false"
})
class NotificationTemplateControllerTest {

  private static final String BASE_URL = "/api/notifications/templates";
  private static String TOKEN;

  @Autowired
  MockMvc mockMvc;
  @Autowired
  ObjectMapper objectMapper;
  @Autowired
  NotificationTemplateRepository notificationTemplateRepository;

  @BeforeAll
  static void init() throws IOException {
    TOKEN = FileUtils.readFileToString(ResourceUtils.getFile("classpath:token.txt"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"email", "inbox"})
  void shouldSaveTemplate(String channel) throws Exception {
    var title = "Title";
    var content = "<html><h1>Hello</h1></html>";
    var inputDto = new SaveNotificationTemplateInputDto();
    inputDto.setTitle(title);
    inputDto.setContent(content);
    inputDto.setAttributes(
        Collections.singletonList(new NotificationTemplateAttributeDto("name", "value")));
    mockMvc
        .perform(
            put(BASE_URL + String.format("/%s:template", channel))
                .header("X-Access-Token", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            jsonPath("$.name", is("template")),
            jsonPath("$.channel", is(channel)),
            jsonPath("$.title", is(title)),
            jsonPath("$.content", is(content)),
            jsonPath("$.checksum", is(DigestUtils.sha256Hex(content))),
            jsonPath("$.attributes[0].name", is("name")),
            jsonPath("$.attributes[0].value", is("value")),
            jsonPath("$.createdAt", notNullValue()),
            jsonPath("$.updatedAt", notNullValue()),
            jsonPath("$.externalTemplateId", nullValue()),
            jsonPath("$.externallyPublishedAt", nullValue()));

    var dbContent = notificationTemplateRepository.findByNameAndChannel("template", channel);
    assertThat(dbContent).isNotEmpty();
  }

  @Test
  void shouldFailIfNoTemplateHandler() throws Exception {
    var title = "Title";
    var content = "<html><h1>Hello</h1></html>";
    var inputDto = new SaveNotificationTemplateInputDto();
    inputDto.setTitle(title);
    inputDto.setContent(content);
    mockMvc
        .perform(
            put(BASE_URL + "/test:template")
                .header("X-Access-Token", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpectAll(
            status().is(HttpStatus.INTERNAL_SERVER_ERROR_500),
            content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void unauthorizedIfAccessTokenAbsent() throws Exception {
    mockMvc.perform(get(BASE_URL)).andExpect(status().isUnauthorized());
  }
}
