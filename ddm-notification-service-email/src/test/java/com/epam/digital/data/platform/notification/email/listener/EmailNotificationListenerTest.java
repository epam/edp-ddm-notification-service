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

package com.epam.digital.data.platform.notification.email.listener;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.digital.data.platform.notification.dto.email.EmailNotificationMessageDto;
import com.epam.digital.data.platform.notification.email.audit.EmailNotificationAuditFacade;
import com.epam.digital.data.platform.notification.email.service.EmailNotificationService;
import com.epam.digital.data.platform.notification.exception.NotificationException;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.epam.digital.data.platform.starter.audit.model.Step;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailNotificationListenerTest {

  @Mock
  private EmailNotificationService emailNotificationService;
  @Mock
  private EmailNotificationAuditFacade emailNotificationAuditFacade;
  @InjectMocks
  private EmailNotificationListener listener;

  @Test
  void testNotify() {
    var message = EmailNotificationMessageDto.builder().build();

    listener.notify(message);

    verify(emailNotificationService, times(1)).notify(message);
    verify(emailNotificationAuditFacade, times(1)).sendAuditOnSuccess(Channel.EMAIL, message);
  }

  @Test
  void shouldAuditOnFailure() {
    var errorMsg = "error";
    var message = EmailNotificationMessageDto.builder().build();
    doThrow(new NotificationException(errorMsg))
        .when(emailNotificationService).notify(message);

    assertThrows(NotificationException.class, () -> listener.notify(message));

    verify(emailNotificationAuditFacade, times(1)).sendAuditOnFailure(Channel.EMAIL, message,
        Step.AFTER, errorMsg);
  }
}