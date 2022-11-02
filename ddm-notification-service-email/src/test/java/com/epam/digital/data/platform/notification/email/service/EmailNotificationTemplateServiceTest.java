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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.notification.exception.NotificationTemplateNotFoundException;
import com.epam.digital.data.platform.notification.email.repository.NotificationTemplateRepository;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailNotificationTemplateServiceTest {

  @Mock
  private NotificationTemplateRepository repository;

  private EmailNotificationTemplateService service;

  @BeforeEach
  void setUp() {
    service = new EmailNotificationTemplateService(repository);
  }

  @Test
  void shouldThrowNotFoundException() {
    var name = "invalid-name";

    when(repository.findByNameAndChannel(name, Channel.EMAIL.getValue()))
        .thenReturn(Optional.empty());

    var exception = assertThrows(NotificationTemplateNotFoundException.class,
        () -> service.getByName(name));

    assertThat(exception.getMessage())
        .isEqualTo(String.format(NotificationTemplateNotFoundException.MESSAGE_FORMAT, name));
  }
}
