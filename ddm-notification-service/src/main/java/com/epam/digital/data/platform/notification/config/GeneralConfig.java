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

package com.epam.digital.data.platform.notification.config;

import com.epam.digital.data.platform.datafactory.settings.client.UserSettingsFeignClient;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import com.epam.digital.data.platform.notification.audit.NotificationAuditFacade;
import com.epam.digital.data.platform.notification.enums.NotificationChannel;
import com.epam.digital.data.platform.notification.facade.NotificationFacade;
import com.epam.digital.data.platform.notification.service.EmailNotificationService;
import com.epam.digital.data.platform.notification.service.EmailNotificationTemplateService;
import com.epam.digital.data.platform.notification.service.MailService;
import com.epam.digital.data.platform.notification.service.NotificationService;
import com.epam.digital.data.platform.notification.service.UserSettingsService;
import com.epam.digital.data.platform.notification.template.FreemarkerTemplateResolver;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.epam.digital.data.platform.starter.audit.service.AuditService;
import java.time.Clock;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * The configuration that contains main notification beans
 */
@Configuration
@EnableFeignClients(clients = UserSettingsFeignClient.class)
@ConditionalOnProperty(prefix = "notifications", name = "enabled", havingValue = "true")
public class GeneralConfig {

  @Bean
  @ConditionalOnProperty(prefix = "spring.mail", value = "host")
  public MailService mailService(JavaMailSender javaMailSender,
      @Value("${spring.mail.username}") String from) {
    return new MailService(javaMailSender, from);
  }

  @Bean
  public EmailNotificationService emailNotificationService(
      EmailNotificationTemplateService emailNotificationTemplateService,
      FreemarkerTemplateResolver freemarkerTemplateResolver, MailService mailService) {
    return new EmailNotificationService(emailNotificationTemplateService,
        freemarkerTemplateResolver, mailService);
  }

  /**
   * Map of the available channels with corresponding notification services
   */
  @Bean(name = "notification-service-map")
  public Map<Channel, NotificationService> notificationServiceMap(
      EmailNotificationService emailNotificationService) {
    return Map.of(Channel.EMAIL, emailNotificationService);
  }

  @Bean
  public UserSettingsService userSettingsService(IdmService idmService,
      UserSettingsFeignClient userSettingsFeignClient) {
    return new UserSettingsService(idmService, userSettingsFeignClient);
  }

  @Bean
  public NotificationFacade notificationFacade(UserSettingsService userSettingsService,
      @Qualifier("notification-service-map") Map<Channel, NotificationService> notificationServiceMap,
      NotificationAuditFacade notificationAuditFacade) {
    return new NotificationFacade(userSettingsService, notificationServiceMap,
        notificationAuditFacade);
  }

  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }

  @Bean
  public NotificationAuditFacade notificationAuditFacade(AuditService auditService,
      @Value("${spring.application.name}") String appName, Clock clock) {
    return new NotificationAuditFacade(auditService, appName, clock);
  }
}
