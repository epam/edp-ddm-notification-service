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

package com.epam.digital.data.platform.notification.diia.config;

import com.epam.digital.data.platform.notification.core.service.NotificationTemplateServiceImpl;
import com.epam.digital.data.platform.notification.diia.audit.DiiaNotificationAuditFacade;
import com.epam.digital.data.platform.notification.diia.listener.DiiaNotificationListener;
import com.epam.digital.data.platform.notification.diia.producer.DiiaNotificationProducer;
import com.epam.digital.data.platform.notification.diia.repository.DiiaNotificationTemplateRepository;
import com.epam.digital.data.platform.notification.diia.service.DiiaRestClient;
import com.epam.digital.data.platform.notification.diia.service.DiiaService;
import com.epam.digital.data.platform.notification.diia.service.TokenCacheService;
import com.epam.digital.data.platform.notification.template.NotificationTemplateService;
import com.epam.digital.data.platform.starter.audit.service.AuditService;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class DiiaNotificationConfig {

  @Bean
  public DiiaService diiaService(
      DiiaRestClient diiaRestClient,
      @Value("${external-systems.diia.auth.secret.token}") String partnerToken,
      TokenCacheService tokenCacheService) {
    return new DiiaService(diiaRestClient, partnerToken, tokenCacheService);
  }

  @Bean
  @ConditionalOnProperty(prefix = "data-platform.kafka", name = "enabled", havingValue = "true")
  public DiiaNotificationProducer diiaNotificationProducer(
      @Qualifier("diiaNotificationTemplateService") NotificationTemplateService<String> diiaNotificationTemplateService,
      KafkaTemplate<String, Object> kafkaTemplate,
      @Value("\u0023{kafkaProperties.topics['diia-notifications']}") String topic) {
    return new DiiaNotificationProducer(diiaNotificationTemplateService, kafkaTemplate, topic);
  }

  @Bean
  @ConditionalOnProperty(prefix = "data-platform.kafka", name = "enabled", havingValue = "true")
  public DiiaNotificationListener diiaNotificationListener(
      DiiaService diiaService, DiiaNotificationAuditFacade diiaNotificationAuditFacade) {
    return new DiiaNotificationListener(diiaService, diiaNotificationAuditFacade);
  }

  @Bean
  public DiiaNotificationAuditFacade diiaNotificationAuditFacade(AuditService auditService,
      @Value("${spring.application.name}") String appName, Clock clock) {
    return new DiiaNotificationAuditFacade(auditService, appName, clock);
  }

  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }

  @Bean
  public TokenCacheService tokenCacheService(CacheManager cacheManager) {
    return new TokenCacheService(cacheManager);
  }

  @Bean
  @Qualifier("diiaNotificationTemplateService")
  public NotificationTemplateService<String> diiaNotificationTemplateService(
      DiiaNotificationTemplateRepository diiaNotificationTemplateRepository) {
    return new NotificationTemplateServiceImpl(diiaNotificationTemplateRepository);
  }
}
