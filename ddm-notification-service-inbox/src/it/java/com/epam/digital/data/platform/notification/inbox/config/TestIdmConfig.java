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

import com.epam.digital.data.platform.integration.idm.config.IdmClientServiceConfig;
import com.epam.digital.data.platform.integration.idm.factory.IdmServiceFactory;
import com.epam.digital.data.platform.integration.idm.model.KeycloakClientProperties;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import com.epam.digital.data.platform.notification.core.service.IdmServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@RequiredArgsConstructor
@Import(IdmClientServiceConfig.class)
public class TestIdmConfig {

  public final IdmServiceFactory idmServiceFactory;

  @Bean
  @ConditionalOnProperty(prefix = "keycloak.citizen-user", value = "realm")
  @ConfigurationProperties(prefix = "keycloak.citizen-user")
  public KeycloakClientProperties citizenRealmProperties() {
    return new KeycloakClientProperties();
  }

  @Bean
  @ConditionalOnBean(name = "citizenRealmProperties")
  public IdmService citizenIdmService(KeycloakClientProperties citizenRealmProperties) {
    return idmServiceFactory.createIdmService(citizenRealmProperties.getRealm(),
        citizenRealmProperties.getClientId(),
        citizenRealmProperties.getClientSecret());
  }

  @Bean
  @ConditionalOnProperty(prefix = "keycloak.officer-user", value = "realm")
  @ConfigurationProperties(prefix = "keycloak.officer-user")
  public KeycloakClientProperties officerRealmProperties() {
    return new KeycloakClientProperties();
  }

  @Bean
  @ConditionalOnBean(name = "officerRealmProperties")
  public IdmService officerIdmService(KeycloakClientProperties officerRealmProperties) {
    return idmServiceFactory.createIdmService(officerRealmProperties.getRealm(),
        officerRealmProperties.getClientId(),
        officerRealmProperties.getClientSecret());
  }

  @Bean
  @ConditionalOnBean(name = {"officerIdmService", "citizenIdmService"})
  public IdmServiceProvider idmServiceProvider(IdmService officerIdmService,
      IdmService citizenIdmService) {
    return new IdmServiceProvider(officerIdmService, citizenIdmService);
  }
}