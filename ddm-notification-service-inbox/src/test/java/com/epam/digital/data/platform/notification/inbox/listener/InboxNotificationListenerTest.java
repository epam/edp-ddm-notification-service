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

package com.epam.digital.data.platform.notification.inbox.listener;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.digital.data.platform.notification.dto.inbox.InboxNotificationMessageDto;
import com.epam.digital.data.platform.notification.exception.NotificationException;
import com.epam.digital.data.platform.notification.inbox.audit.InboxNotificationAuditFacade;
import com.epam.digital.data.platform.notification.inbox.service.InboxNotificationService;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.epam.digital.data.platform.starter.audit.model.Step;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InboxNotificationListenerTest {

  @Mock
  private InboxNotificationService inboxNotificationService;
  @Mock
  private InboxNotificationAuditFacade inboxNotificationAuditFacade;
  @InjectMocks
  private InboxNotificationListener listener;

  @Test
  void testNotify() {
    var message = InboxNotificationMessageDto.builder().build();

    listener.notify(message);

    verify(inboxNotificationService, times(1)).notify(message);
    verify(inboxNotificationAuditFacade, times(1)).sendAuditOnSuccess(Channel.INBOX, message);
  }

  @Test
  void shouldAuditOnFailure() {
    var errorMsg = "error";
    var message = InboxNotificationMessageDto.builder().build();
    doThrow(new NotificationException(errorMsg))
        .when(inboxNotificationService).notify(message);

    assertThrows(NotificationException.class, () -> listener.notify(message));

    verify(inboxNotificationAuditFacade, times(1)).sendAuditOnFailure(Channel.INBOX, message,
        Step.AFTER, errorMsg);
  }
}