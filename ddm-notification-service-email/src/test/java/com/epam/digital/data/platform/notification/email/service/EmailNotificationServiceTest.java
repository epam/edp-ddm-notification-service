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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.notification.core.template.FreemarkerTemplateResolver;
import com.epam.digital.data.platform.notification.dto.audit.NotificationDto;
import com.epam.digital.data.platform.notification.dto.email.EmailNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.email.EmailRecipientDto;
import com.epam.digital.data.platform.notification.template.NotificationTemplateService;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

  @Mock
  private NotificationTemplateService<String> templateService;
  @Mock
  private FreemarkerTemplateResolver templateResolver;
  @Mock
  private MailService mailService;
  @InjectMocks
  private EmailNotificationService service;

  @Test
  void testNotify() {
    var subject = "subject";
    var message = "message";
    var email = "email@dot.com";
    var notification = NotificationDto.builder().subject(subject)
        .message(message).build();
    var recipient = EmailRecipientDto.builder().email(email).build();
    var msgDto = EmailNotificationMessageDto.builder()
        .notification(notification).recipient(recipient)
        .build();

    service.notify(msgDto);

    verify(mailService, times(1)).send(subject, message, email);
  }

  @Test
  void prepareEmailBody() {
    var data = new HashMap<String, Object>();
    when(templateService.getContentByNameAndChannel("name", Channel.EMAIL)).thenReturn("content");

    service.prepareEmailBody("name", Map.of());

    verify(templateService, times(1)).getContentByNameAndChannel("name", Channel.EMAIL);
    verify(templateResolver, times(1)).resolve("name", "content", data);
  }
}