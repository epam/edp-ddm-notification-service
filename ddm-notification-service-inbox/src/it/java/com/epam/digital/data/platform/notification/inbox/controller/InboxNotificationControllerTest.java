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
package com.epam.digital.data.platform.notification.inbox.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.digital.data.platform.notification.entity.InboxNotification;
import com.epam.digital.data.platform.notification.inbox.config.InboxControllerTestConfig;
import com.epam.digital.data.platform.notification.inbox.repository.InboxNotificationRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@ExtendWith(SpringExtension.class)
@Import(InboxControllerTestConfig.class)
@EntityScan("com.epam.digital.data.platform.notification.entity")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "notifications.enabled=false"
})
class InboxNotificationControllerTest  {

  private static final String BASE_URL = "/api/notifications/inbox";
  private static String TOKEN;

  @Autowired
  MockMvc mockMvc;
  @Autowired
  InboxNotificationRepository inboxNotificationRepository;

  @BeforeAll
  static void init() throws IOException {
    TOKEN = FileUtils.readFileToString(ResourceUtils.getFile("classpath:token.txt"));
  }

  @Test
  @SneakyThrows
  void shouldReturnResultForGetNotifications() {
    prefillData();

    mockMvc
        .perform(
            get(BASE_URL)
                .queryParam("limit", "10")
                .queryParam("offset", "0")
                .header("X-Access-Token", TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            jsonPath("$.[0].id", notNullValue()),
            jsonPath("$.[0].subject", is("subject")),
            jsonPath("$.[0].message", is("message")),
            jsonPath("$.[0].createdAt", notNullValue()),
            jsonPath("$.[0].isAcknowledged", is(false)));
  }

  @Test
  @SneakyThrows
  void shouldValidUpdateNotificationState() {
    var inboxNotification = prefillData();

    mockMvc
        .perform(
            post(BASE_URL + String.format("/%s/ack", inboxNotification.getId()))
                .header("X-Access-Token", TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpectAll(status().isOk());

    assertTrue(inboxNotificationRepository.getById(inboxNotification.getId()).isAcknowledged());
  }

  private InboxNotification prefillData() {
    return inboxNotificationRepository.save(
        InboxNotification.builder()
            .subject("subject")
            .message("message")
            .recipientId("496fd2fd-3497-4391-9ead-41410522d06f")
            .createdAt(LocalDateTime.now())
            .isAcknowledged(false)
            .build());
  }
}