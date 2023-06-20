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

package com.epam.digital.data.platform.notification.config;

import com.epam.digital.data.platform.datafactory.settings.client.UserSettingsFeignClient;
import com.epam.digital.data.platform.notification.audit.UserNotificationAuditFacade;
import com.epam.digital.data.platform.notification.core.service.IdmServiceProvider;
import com.epam.digital.data.platform.notification.facade.UserNotificationFacade;
import com.epam.digital.data.platform.notification.mapper.ChannelMapper;
import com.epam.digital.data.platform.notification.producer.NotificationProducer;
import com.epam.digital.data.platform.notification.service.UserService;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.epam.digital.data.platform.starter.audit.service.AuditService;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The configuration that contains main notification beans
 */
@Configuration
@EnableFeignClients(clients = UserSettingsFeignClient.class)
@ConditionalOnProperty(prefix = "notifications", name = "enabled", havingValue = "true")
public class NotificationsConfig {

  @Bean
  public Map<Channel, NotificationProducer> channelProducerMap(List<NotificationProducer> producerList) {
    return producerList.stream()
        .collect(Collectors.toMap(NotificationProducer::getChannel, Function.identity()));
  }

  @Bean
  public Map<Channel, ChannelMapper> channelMapperMap(List<ChannelMapper> channelMapperList) {
    return channelMapperList.stream()
        .collect(Collectors.toMap(ChannelMapper::getChannel, Function.identity()));
  }

  @Bean
  public UserService userService(IdmServiceProvider idmServiceProvider,
      UserSettingsFeignClient userSettingsFeignClient) {
    return new UserService(idmServiceProvider, userSettingsFeignClient);
  }

  @Bean
  public UserNotificationFacade userNotificationFacade(UserService userService,
      UserNotificationAuditFacade notificationAuditFacade,
      Map<Channel, NotificationProducer> channelProducerMap, Map<Channel, ChannelMapper> channelMapperMap) {
    return new UserNotificationFacade(userService, notificationAuditFacade,
        channelProducerMap, channelMapperMap);
  }

  @Bean
  public UserNotificationAuditFacade userNotificationAuditFacade(AuditService auditService,
      @Value("${spring.application.name}") String appName, Clock clock) {
    return new UserNotificationAuditFacade(auditService, appName, clock);
  }
}
