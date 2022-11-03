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

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

public class WireMockContextInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext> {

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    var userSettingsWireMock = new WireMockServer(wireMockConfig().dynamicPort());
    userSettingsWireMock.start();
    var keycloakWireMock = new WireMockServer(wireMockConfig().dynamicPort());
    keycloakWireMock.start();
    var diiaWireMock = new WireMockServer(wireMockConfig().dynamicPort());
    diiaWireMock.start();

    applicationContext.getBeanFactory()
        .registerSingleton("userSettingsWireMock", userSettingsWireMock);
    applicationContext.getBeanFactory()
        .registerSingleton("keycloakWireMock", keycloakWireMock);
    applicationContext.getBeanFactory()
        .registerSingleton("diiaWireMock", diiaWireMock);

    applicationContext.addApplicationListener(event -> {
      if (event instanceof ContextClosedEvent) {
        userSettingsWireMock.stop();
        keycloakWireMock.stop();
        diiaWireMock.stop();
      }
    });

    TestPropertyValues.of(
        String.format("user-settings-service.url=http://localhost:%s", userSettingsWireMock.port()), 
        String.format("keycloak.url=http://localhost:%s", keycloakWireMock.port()),
        String.format("notifications.diia.url=http://localhost:%s", diiaWireMock.port()))
        .applyTo(applicationContext);
  }
}
