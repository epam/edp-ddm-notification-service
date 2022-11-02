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

import com.epam.digital.data.platform.notification.dto.inbox.InboxNotificationResponseDto;
import com.epam.digital.data.platform.notification.dto.inbox.InboxOffsetBasedPageRequest;
import com.epam.digital.data.platform.notification.inbox.service.InboxNotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications/inbox")
@ConditionalOnProperty(prefix = "notifications", name = "enabled", havingValue = "true")
public class InboxNotificationController {

  private static final String ACCESS_TOKEN = "X-Access-Token";
  private final InboxNotificationService inboxNotificationService;

  @GetMapping
  public ResponseEntity<List<InboxNotificationResponseDto>> getInboxNotifications(
      @RequestHeader(ACCESS_TOKEN) String accessToken,
      InboxOffsetBasedPageRequest request) {
    log.info("processing get all notifications request");
    return ResponseEntity.ok()
        .body(inboxNotificationService.getInboxNotifications(accessToken, request));
  }

  @PostMapping("/{id}/ack")
  public ResponseEntity<Void> acknowledgeNotification(
      @PathVariable("id") UUID id,
      @RequestHeader(ACCESS_TOKEN) String accessToken) {
    log.info("processing acknowledgeNotification request");
    inboxNotificationService.acknowledgeNotification(id, accessToken);
    return ResponseEntity.ok().build();
  }
}