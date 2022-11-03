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

package com.epam.digital.data.platform.notification.diia.listener;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.notification.diia.audit.DiiaNotificationAuditFacade;
import com.epam.digital.data.platform.notification.diia.service.DiiaService;
import com.epam.digital.data.platform.notification.dto.diia.DiiaNotificationDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaRecipientDto;
import com.epam.digital.data.platform.notification.exception.NotificationException;
import com.epam.digital.data.platform.starter.audit.model.Step;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiiaNotificationListenerTest {

  @Mock
  private DiiaService diiaService;
  @Mock
  private DiiaNotificationAuditFacade diiaNotificationAuditFacade;
  @InjectMocks
  private DiiaNotificationListener listener;

  @Test
  void testNotify() {
    var message = DiiaNotificationMessageDto.builder()
        .recipient(DiiaRecipientDto.builder().id("100").build())
        .notification(new DiiaNotificationDto()).build();
    when(diiaService.notify(message)).thenReturn("1000");

    listener.notify(message);

    verify(diiaService, times(1)).notify(message);
    verify(diiaNotificationAuditFacade, times(1)).sendAuditOnSuccess(message, "1000");
  }

  @Test
  void shouldAuditOnFailure() {
    var errorMsg = "error";
    var message = DiiaNotificationMessageDto.builder()
        .recipient(DiiaRecipientDto.builder().id("100").build())
        .notification(new DiiaNotificationDto()).build();
    doThrow(new NotificationException(errorMsg))
        .when(diiaService).notify(message);

    assertThrows(NotificationException.class, () -> listener.notify(message));

    verify(diiaNotificationAuditFacade, times(1))
        .sendAuditOnFailure(message, Step.AFTER, errorMsg);
  }
}