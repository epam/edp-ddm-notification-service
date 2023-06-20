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

package com.epam.digital.data.platform.notification.inbox.service;

import com.epam.digital.data.platform.notification.core.service.IdmServiceProvider;
import com.epam.digital.data.platform.notification.core.template.FreemarkerTemplateResolver;
import com.epam.digital.data.platform.notification.dto.inbox.InboxNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.inbox.InboxNotificationResponseDto;
import com.epam.digital.data.platform.notification.entity.InboxNotification;
import com.epam.digital.data.platform.notification.exception.ForbiddenNotificationActionException;
import com.epam.digital.data.platform.notification.inbox.repository.InboxNotificationRepository;
import com.epam.digital.data.platform.notification.model.JwtClaims;
import com.epam.digital.data.platform.notification.service.NotificationService;
import com.epam.digital.data.platform.notification.template.NotificationTemplateService;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.data.domain.Pageable;

@Slf4j
@RequiredArgsConstructor
public class InboxNotificationService implements NotificationService<InboxNotificationMessageDto> {

  private final NotificationTemplateService<String> templateService;
  private final FreemarkerTemplateResolver templateResolver;
  private final InboxNotificationRepository inboxNotificationRepository;
  private final TokenParserService tokenParserService;
  private final IdmServiceProvider idmServiceProvider;

  public void notify(InboxNotificationMessageDto message) {
    log.info("Sending inbox notification - saving in database");
    log.info("Getting user id by username");

    var idmService = idmServiceProvider.getIdmService(message.getRecipientRealm());
    var users = idmService.getUserByUserName(message.getRecipientName());
    if (users.isEmpty()) {
      throw new IllegalArgumentException("User not found by username");
    }

    inboxNotificationRepository.save(
        InboxNotification.builder()
            .subject(message.getNotification().getSubject())
            .message(message.getNotification().getMessage())
            .recipientId(users.get(0).getId())
            .build());
    log.info("Inbox notification was sent - saved in database");
  }

  public List<InboxNotificationResponseDto> getInboxNotifications(
      String accessToken,
      Pageable pageable) {

    JwtClaims jwtClaims = tokenParserService.parseClaims(accessToken);

    List<InboxNotification> byRecipientId = inboxNotificationRepository.findByRecipientId(
        jwtClaims.getSubject(), pageable);

    return byRecipientId.stream().map(inboxNotification ->
        InboxNotificationResponseDto.builder()
            .id(inboxNotification.getId())
            .subject(inboxNotification.getSubject())
            .message(inboxNotification.getMessage())
            .isAcknowledged(inboxNotification.isAcknowledged())
            .createdAt(inboxNotification.getCreatedAt())
            .build()).collect(Collectors.toList());
  }

  public void acknowledgeNotification(UUID notificationId, String accessToken) {
    Optional<InboxNotification> byId = inboxNotificationRepository.findById(notificationId);
    if (byId.isPresent()) {
      JwtClaims jwtClaims = tokenParserService.parseClaims(accessToken);

      InboxNotification inboxNotification = byId.get();
      if (!StringUtils.equals(jwtClaims.getSubject(), inboxNotification.getRecipientId())) {
        throw new ForbiddenNotificationActionException(
            "Forbidden. Notification state can't be updated.");
      }

      inboxNotification.setAcknowledged(true);
      inboxNotificationRepository.save(inboxNotification);
    }
  }

  public String prepareInboxBody(String templateName, Map<String, Object> data) {
    var template = templateService.getContentByNameAndChannel(templateName, Channel.INBOX);
    return templateResolver.resolve(templateName, template, data);
  }
}
