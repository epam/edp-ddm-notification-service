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

package com.epam.digital.data.platform.notification.inbox.config;

import com.epam.digital.data.platform.notification.core.service.IdmServiceProvider;
import com.epam.digital.data.platform.notification.core.service.NotificationTemplateServiceImpl;
import com.epam.digital.data.platform.notification.core.template.FreemarkerTemplateResolver;
import com.epam.digital.data.platform.notification.inbox.audit.InboxNotificationAuditFacade;
import com.epam.digital.data.platform.notification.inbox.listener.InboxNotificationListener;
import com.epam.digital.data.platform.notification.inbox.producer.InboxNotificationProducer;
import com.epam.digital.data.platform.notification.inbox.repository.InboxNotificationRepository;
import com.epam.digital.data.platform.notification.inbox.repository.InboxNotificationTemplateRepository;
import com.epam.digital.data.platform.notification.inbox.service.InboxNotificationService;
import com.epam.digital.data.platform.notification.inbox.service.TokenParserService;
import com.epam.digital.data.platform.notification.template.NotificationTemplateService;
import com.epam.digital.data.platform.starter.audit.service.AuditService;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@ConditionalOnProperty(prefix = "notifications", name = "enabled", havingValue = "true")
public class InboxNotificationConfig {

  @Bean
  public InboxNotificationService inboxNotificationService(
      @Qualifier("inboxNotificationTemplateService") NotificationTemplateService<String> inboxNotificationTemplateService,
      FreemarkerTemplateResolver inboxFreemarkerTemplateResolver,
      InboxNotificationRepository inboxNotificationRepository,
      TokenParserService tokenParserService,
      IdmServiceProvider idmServiceProvider) {
    return new InboxNotificationService(
        inboxNotificationTemplateService,
        inboxFreemarkerTemplateResolver,
        inboxNotificationRepository,
        tokenParserService,
        idmServiceProvider);
  }

  @Bean
  @ConditionalOnProperty(prefix = "data-platform.kafka", name = "enabled", havingValue = "true")
  public InboxNotificationProducer inboxNotificationProducer(
      KafkaTemplate<String, Object> kafkaTemplate,
      InboxNotificationService inboxNotificationService,
      @Qualifier("inboxNotificationTemplateService") NotificationTemplateService<String> inboxNotificationTemplateService,
      @Value("\u0023{kafkaProperties.topics['inbox-notifications']}") String topic) {
    return new InboxNotificationProducer(
        inboxNotificationTemplateService,
        kafkaTemplate,
        topic,
        inboxNotificationService);
  }

  @Bean
  @ConditionalOnProperty(prefix = "data-platform.kafka", name = "enabled", havingValue = "true")
  public InboxNotificationListener inboxNotificationListener(
      InboxNotificationService inboxNotificationService,
      InboxNotificationAuditFacade notificationAuditFacade) {
    return new InboxNotificationListener(inboxNotificationService, notificationAuditFacade);
  }

  @Bean
  public InboxNotificationAuditFacade inboxNotificationAuditFacade(AuditService auditService,
      @Value("${spring.application.name}") String appName, Clock clock) {
    return new InboxNotificationAuditFacade(auditService, appName, clock);
  }

  @Bean
  @Qualifier("inboxNotificationTemplateService")
  public NotificationTemplateService<String> inboxNotificationTemplateService(
      InboxNotificationTemplateRepository inboxNotificationTemplateRepository) {
    return new NotificationTemplateServiceImpl(inboxNotificationTemplateRepository);
  }
}
