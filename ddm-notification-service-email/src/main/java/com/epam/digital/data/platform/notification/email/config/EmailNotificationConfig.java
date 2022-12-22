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

package com.epam.digital.data.platform.notification.email.config;

import com.epam.digital.data.platform.notification.core.service.NotificationTemplateServiceImpl;
import com.epam.digital.data.platform.notification.core.template.FreemarkerTemplateResolver;
import com.epam.digital.data.platform.notification.email.audit.EmailNotificationAuditFacade;
import com.epam.digital.data.platform.notification.email.listener.EmailNotificationListener;
import com.epam.digital.data.platform.notification.email.producer.EmailNotificationProducer;
import com.epam.digital.data.platform.notification.email.repository.EmailNotificationTemplateRepository;
import com.epam.digital.data.platform.notification.email.service.EmailNotificationService;
import com.epam.digital.data.platform.notification.email.service.MailService;
import com.epam.digital.data.platform.notification.template.NotificationTemplateService;
import com.epam.digital.data.platform.starter.audit.service.AuditService;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
@ConditionalOnProperty(prefix = "notifications", name = "enabled", havingValue = "true")
public class EmailNotificationConfig {

  @Bean
  @ConditionalOnProperty(prefix = "spring.mail", value = "host")
  public MailService mailService(JavaMailSender javaMailSender,
      @Value("${spring.mail.username}") String from) {
    return new MailService(javaMailSender, from);
  }

  @Bean
  public EmailNotificationService emailNotificationService(
      @Qualifier("emailNotificationTemplateService") NotificationTemplateService<String> emailNotificationTemplateService,
      FreemarkerTemplateResolver freemarkerTemplateResolver, MailService mailService) {
    return new EmailNotificationService(emailNotificationTemplateService,
        freemarkerTemplateResolver, mailService);
  }

  @Bean
  @ConditionalOnProperty(prefix = "data-platform.kafka", name = "enabled", havingValue = "true")
  public EmailNotificationProducer emailNotificationProducer(
      KafkaTemplate<String, Object> kafkaTemplate,
      EmailNotificationService emailNotificationService,
      @Qualifier("emailNotificationTemplateService") NotificationTemplateService<String> emailNotificationTemplateService,
      @Value("\u0023{kafkaProperties.topics['email-notifications']}") String topic) {
    return new EmailNotificationProducer(
        emailNotificationTemplateService,
        kafkaTemplate,
        topic,
        emailNotificationService);
  }

  @Bean
  @ConditionalOnProperty(prefix = "data-platform.kafka", name = "enabled", havingValue = "true")
  public EmailNotificationListener emailNotificationListener(
      EmailNotificationService emailNotificationService,
      EmailNotificationAuditFacade emailNotificationAuditFacade) {
    return new EmailNotificationListener(emailNotificationService, emailNotificationAuditFacade);
  }

  @Bean
  public EmailNotificationAuditFacade emailNotificationAuditFacade(AuditService auditService,
      @Value("${spring.application.name}") String appName, Clock clock) {
    return new EmailNotificationAuditFacade(auditService, appName, clock);
  }

  @Bean
  @Qualifier("emailNotificationTemplateService")
  public NotificationTemplateService<String> emailNotificationTemplateService(
      EmailNotificationTemplateRepository emailNotificationTemplateRepository) {
    return new NotificationTemplateServiceImpl(emailNotificationTemplateRepository);
  }
}
