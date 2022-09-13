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

package com.epam.digital.data.platform.notification.facade;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.integration.idm.service.IdmService;
import com.epam.digital.data.platform.notification.audit.NotificationAuditFacade;
import com.epam.digital.data.platform.notification.dto.NotificationDto;
import com.epam.digital.data.platform.notification.dto.NotificationRecordDto;
import com.epam.digital.data.platform.notification.exception.NotificationException;
import com.epam.digital.data.platform.notification.exception.NotificationTemplateNotFoundException;
import com.epam.digital.data.platform.notification.service.EmailNotificationService;
import com.epam.digital.data.platform.notification.service.UserSettingsService;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.epam.digital.data.platform.settings.model.dto.ChannelReadDto;
import com.epam.digital.data.platform.settings.model.dto.SettingsReadDto;
import com.epam.digital.data.platform.starter.audit.model.Step;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NotificationFacadeTest {

  @Mock
  private UserSettingsService userSettingsService;
  @Mock
  private IdmService idmService;
  @Mock
  private EmailNotificationService emailNotificationService;
  @Mock
  private NotificationAuditFacade notificationAuditFacade;
  private NotificationFacade notificationFacade;

  @BeforeEach
  void setup() {
    notificationFacade =
        new NotificationFacade(
            userSettingsService,
            Map.of(Channel.EMAIL, emailNotificationService),
            notificationAuditFacade);
  }

  @Test
  void shouldNotNotifyInactiveChannel() {
    var recipient = "recipient";
    var record = NotificationRecordDto.builder()
            .notification(NotificationDto.builder().recipient(recipient).build())
            .build();
    var settingsReadDto = new SettingsReadDto();
    var emailChannel = new ChannelReadDto();
    emailChannel.setChannel(Channel.EMAIL);
    emailChannel.setActivated(false);
    emailChannel.setAddress("address");
    settingsReadDto.setChannels(Collections.singletonList(emailChannel));
    when(userSettingsService.getByUsername(recipient)).thenReturn(settingsReadDto);

    notificationFacade.sendNotification(record);

    verifyNoInteractions(emailNotificationService);
  }

  @Test
  void shouldNotifyActiveValidChannel() {
    var recipient = "recipient";
    var notification = NotificationDto.builder().recipient(recipient).build();
    var record = NotificationRecordDto.builder()
            .notification(NotificationDto.builder().recipient(recipient).build())
            .build();
    var settingsReadDto = new SettingsReadDto();
    var emailChannel = new ChannelReadDto();
    emailChannel.setChannel(Channel.EMAIL);
    emailChannel.setActivated(true);
    emailChannel.setAddress("address");
    settingsReadDto.setChannels(Collections.singletonList(emailChannel));
    when(userSettingsService.getByUsername(recipient)).thenReturn(settingsReadDto);

    notificationFacade.sendNotification(record);

    verify(emailNotificationService).notify(notification, emailChannel);
  }

  @Test
  void shouldNotNotifyUnimplementedChannel() {
    var recipient = "recipient";
    var record = NotificationRecordDto.builder()
            .notification(NotificationDto.builder().recipient(recipient).build())
            .build();
    var settingsReadDto = new SettingsReadDto();
    var emailChannel = new ChannelReadDto();
    emailChannel.setChannel(Channel.DIIA);
    emailChannel.setActivated(true);
    settingsReadDto.setChannels(Collections.singletonList(emailChannel));
    when(userSettingsService.getByUsername(recipient)).thenReturn(settingsReadDto);

    notificationFacade.sendNotification(record);

    verifyNoInteractions(emailNotificationService);
  }

  @Test
  void shouldAuditOnFailureWhenNotificationIsNull() {
    var record = NotificationRecordDto.builder()
        .build();

    var exception = assertThrows(NotificationException.class,
        () -> notificationFacade.sendNotification(record));

    verify(notificationAuditFacade, times(1)).sendAuditOnFailure(null, null, Step.BEFORE,
        exception.getMessage());
  }

  @Test
  void shouldAuditOnFailureWhenUserSettingsNotFound() {
    var recipient = "recipient";
    var notification = NotificationDto.builder().recipient(recipient).build();
    var record = NotificationRecordDto.builder()
        .notification(NotificationDto.builder().recipient(recipient).build())
        .build();
    when(userSettingsService.getByUsername(recipient)).thenThrow(IllegalArgumentException.class);

    assertThrows(NotificationException.class,
        () -> notificationFacade.sendNotification(record));

    verify(notificationAuditFacade, times(1)).sendAuditOnFailure(any(), eq(notification),
        eq(Step.BEFORE), any());
  }

  @Test
  void shouldAuditOnNotificationFailure() {
    var recipient = "recipient";
    var notification = NotificationDto.builder().recipient(recipient).build();
    var record = NotificationRecordDto.builder()
        .notification(NotificationDto.builder().recipient(recipient).build())
        .build();
    var settingsReadDto = new SettingsReadDto();
    var emailChannel = new ChannelReadDto();
    emailChannel.setChannel(Channel.EMAIL);
    emailChannel.setActivated(true);
    emailChannel.setAddress("address");
    settingsReadDto.setChannels(Collections.singletonList(emailChannel));
    when(userSettingsService.getByUsername(recipient)).thenReturn(settingsReadDto);
    doThrow(new NotificationTemplateNotFoundException("template-id"))
        .when(emailNotificationService).notify(notification, emailChannel);

    var exception = assertThrows(NotificationException.class,
        () -> notificationFacade.sendNotification(record));

    verify(notificationAuditFacade, times(1))
        .sendAuditOnFailure(Channel.EMAIL, notification, Step.AFTER, exception.getMessage());
  }
}
