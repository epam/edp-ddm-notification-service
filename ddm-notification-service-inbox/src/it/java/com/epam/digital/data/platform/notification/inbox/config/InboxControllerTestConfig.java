/*
 *  Copyright 2022 EPAM Systems.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.epam.digital.data.platform.notification.inbox.config;

import com.epam.digital.data.platform.integration.idm.service.IdmService;
import com.epam.digital.data.platform.notification.inbox.controller.InboxNotificationController;
import com.epam.digital.data.platform.notification.inbox.repository.InboxNotificationRepository;
import com.epam.digital.data.platform.notification.inbox.service.InboxNotificationService;
import com.epam.digital.data.platform.notification.inbox.service.InboxNotificationTemplateService;
import com.epam.digital.data.platform.notification.inbox.service.TokenParserService;
import com.epam.digital.data.platform.notification.inbox.template.InboxFreemarkerTemplateResolver;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class InboxControllerTestConfig {

  @Bean
  public InboxNotificationService inboxNotificationService(
      InboxNotificationTemplateService inboxNotificationTemplateService,
      InboxFreemarkerTemplateResolver inboxFreemarkerTemplateResolver,
      InboxNotificationRepository inboxNotificationRepository,
      TokenParserService tokenParserService,
      IdmService systemIdmService) {
    return new InboxNotificationService(
        inboxNotificationTemplateService,
        inboxFreemarkerTemplateResolver,
        inboxNotificationRepository,
        tokenParserService,
        systemIdmService);
  }

  @Bean
  public InboxNotificationController inboxNotificationController(
      InboxNotificationService inboxNotificationService) {
    return new InboxNotificationController(inboxNotificationService);
  }
}
