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

import com.epam.digital.data.platform.notification.audit.UserNotificationAuditFacade;
import com.epam.digital.data.platform.notification.dto.ChannelObject;
import com.epam.digital.data.platform.notification.dto.Recipient;
import com.epam.digital.data.platform.notification.dto.UserNotificationDto;
import com.epam.digital.data.platform.notification.dto.UserNotificationMessageDto;
import com.epam.digital.data.platform.notification.exception.NotificationException;
import com.epam.digital.data.platform.notification.exception.NotificationTemplateNotFoundException;
import com.epam.digital.data.platform.notification.email.producer.EmailNotificationProducer;
import com.epam.digital.data.platform.notification.email.mapper.EmailChannelMapper;
import com.epam.digital.data.platform.notification.inbox.mapper.InboxChannelMapper;
import com.epam.digital.data.platform.notification.inbox.producer.InboxNotificationProducer;
import com.epam.digital.data.platform.notification.service.UserSettingsService;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.epam.digital.data.platform.settings.model.dto.ChannelReadDto;
import com.epam.digital.data.platform.settings.model.dto.SettingsReadDto;
import com.epam.digital.data.platform.starter.audit.model.Step;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserNotificationFacadeTest {

  @Mock
  private UserSettingsService userSettingsService;
  @Mock
  private EmailNotificationProducer emailNotificationProducer;
  @Mock
  private InboxNotificationProducer inboxNotificationProducer;
  @Mock
  private UserNotificationAuditFacade notificationAuditFacade;
  private UserNotificationFacade notificationFacade;

  @BeforeEach
  void setup() {
    notificationFacade =
        new UserNotificationFacade(
            userSettingsService,
            notificationAuditFacade,
            Map.of(Channel.EMAIL, emailNotificationProducer,
                Channel.INBOX, inboxNotificationProducer),
            Map.of(Channel.EMAIL, new EmailChannelMapper(),
                Channel.INBOX, new InboxChannelMapper()));
    notificationFacade.setRecipientsMaxThreadPoolSize(10);
    notificationFacade.setChannelsMaxThreadPoolSize(5);
  }

  @Test
  void shouldNotNotifyInactiveChannel() {
    var recipient = "recipient";
    var message = UserNotificationMessageDto.builder()
        .notification(UserNotificationDto.builder().build())
        .recipients(List.of(Recipient.builder().id(recipient).build()))
        .build();
    var settingsReadDto = new SettingsReadDto();
    var emailChannel = new ChannelReadDto();
    emailChannel.setChannel(Channel.EMAIL);
    emailChannel.setActivated(false);
    emailChannel.setAddress("address");
    settingsReadDto.setChannels(Collections.singletonList(emailChannel));
    when(userSettingsService.getByUsername(recipient)).thenReturn(settingsReadDto);

    notificationFacade.sendNotification(message);

    verifyNoInteractions(emailNotificationProducer);
  }

  @Test
  void shouldNotifyActiveValidChannel() {
    var recipient = "recipient";
    var recipientDto = Recipient.builder().id(recipient).build();
    var message = UserNotificationMessageDto.builder()
        .notification(UserNotificationDto.builder().build())
        .recipients(List.of(recipientDto))
        .build();
    var settingsReadDto = new SettingsReadDto();
    var emailChannel = new ChannelReadDto();
    emailChannel.setChannel(Channel.EMAIL);
    emailChannel.setActivated(true);
    emailChannel.setAddress("address");
    settingsReadDto.setChannels(Collections.singletonList(emailChannel));
    when(userSettingsService.getByUsername(recipient)).thenReturn(settingsReadDto);

    notificationFacade.sendNotification(message);

    verify(emailNotificationProducer).send(recipientDto, message);
  }

  @Test
  void shouldNotNotifyUnimplementedChannel() {
    var recipient = "recipient";
    var message = UserNotificationMessageDto.builder()
        .notification(UserNotificationDto.builder().build())
        .recipients(List.of(Recipient.builder().id(recipient).build()))
        .build();
    var settingsReadDto = new SettingsReadDto();
    var emailChannel = new ChannelReadDto();
    emailChannel.setChannel(Channel.DIIA);
    emailChannel.setActivated(true);
    settingsReadDto.setChannels(new ArrayList<>(Collections.singletonList(emailChannel)));
    when(userSettingsService.getByUsername(recipient)).thenReturn(settingsReadDto);

    notificationFacade.sendNotification(message);

    verifyNoInteractions(emailNotificationProducer);
  }

  @Test
  void shouldAuditOnFailureWhenNotificationIsNull() {
    var message = UserNotificationMessageDto.builder()
        .build();

    var exception = assertThrows(NotificationException.class,
        () -> notificationFacade.sendNotification(message));

    verify(notificationAuditFacade, times(1)).sendAuditOnFailure(null, null, Step.BEFORE,
        exception.getMessage());
  }

  @Test
  void shouldAuditOnFailureWhenUserSettingsNotFound() {
    var recipient = "recipient";
    var message = UserNotificationMessageDto.builder()
        .notification(UserNotificationDto.builder().build())
        .recipients(List.of(Recipient.builder().id(recipient).build()))
        .build();
    when(userSettingsService.getByUsername(recipient)).thenThrow(IllegalArgumentException.class);

    notificationFacade.sendNotification(message);

    verify(notificationAuditFacade, times(1)).sendAuditOnFailure(any(), eq(message),
        eq(Step.BEFORE), any());
  }

  @Test
  void shouldAuditOnNotificationFailure() {
    var recipient = "recipient";
    var recipientDto = Recipient.builder().id(recipient)
        .channels(List.of(new ChannelObject("email", "address", null)))
        .build();
    var message = UserNotificationMessageDto.builder()
        .notification(UserNotificationDto.builder().build())
        .recipients(List.of(Recipient.builder().id(recipient).build()))
        .build();
    var settingsReadDto = new SettingsReadDto();
    var emailChannel = new ChannelReadDto();
    emailChannel.setChannel(Channel.EMAIL);
    emailChannel.setActivated(true);
    emailChannel.setAddress("address");
    settingsReadDto.setChannels(Collections.singletonList(emailChannel));
    when(userSettingsService.getByUsername(recipient)).thenReturn(settingsReadDto);
    doThrow(new NotificationTemplateNotFoundException("template-id"))
        .when(emailNotificationProducer).send(recipientDto, message);

    notificationFacade.sendNotification(message);

    verify(notificationAuditFacade, times(1))
        .sendAuditOnFailure(Channel.EMAIL, message, Step.AFTER,
            "Notification template template-id not found");
  }
}
