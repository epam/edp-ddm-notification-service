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

import com.epam.digital.data.platform.notification.facade.UserNotificationFacade;
import com.epam.digital.data.platform.notification.listener.UserNotificationListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "data-platform.kafka", name = "enabled", havingValue = "true")
public class KafkaConfig {

  @Bean
  public UserNotificationListener notificationListener(UserNotificationFacade userNotificationFacade) {
    return new UserNotificationListener(userNotificationFacade);
  }
}
