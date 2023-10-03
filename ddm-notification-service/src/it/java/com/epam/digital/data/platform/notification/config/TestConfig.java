/*
 * Copyright 2023 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.notification.config;

import com.epam.digital.data.platform.notification.DdmNotificationServiceApplication;
import com.epam.digital.data.platform.notification.inbox.service.InboxNotificationService;
import com.epam.digital.data.platform.notification.service.NotificationTemplateFacade;
import com.epam.digital.data.platform.notification.service.NotificationTemplateService;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
@ConditionalOnMissingBean(DdmNotificationServiceApplication.class)
public class TestConfig {

  @Bean
  public InboxNotificationService testInboxNotificationService() {
    return Mockito.mock(InboxNotificationService.class);
  }
  @Bean
  public NotificationTemplateFacade testNotificationTemplateFacade() {
    return Mockito.mock(NotificationTemplateFacade.class);
  }
  @Bean
  public NotificationTemplateService testNotificationTemplateService() {
    return Mockito.mock(NotificationTemplateService.class);
  }

}
