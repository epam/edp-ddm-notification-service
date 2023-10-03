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

package com.epam.digital.data.platform.notification.client;

import com.epam.digital.data.platform.notification.dto.NotificationTemplateShortInfoResponseDto;
import com.epam.digital.data.platform.notification.dto.SaveNotificationTemplateInputDto;
import com.epam.digital.data.platform.notification.dto.SaveNotificationTemplateOutputDto;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "notification-template-client",
    url = "${notification-service.url}/api/notifications/templates")
public interface NotificationTemplateRestClient {

  @PutMapping("/{channel}:{name}")
  SaveNotificationTemplateOutputDto saveTemplate(
      @PathVariable("channel") String channel,
      @PathVariable("name") String name,
      @RequestBody SaveNotificationTemplateInputDto input);

  @GetMapping("/")
  public List<NotificationTemplateShortInfoResponseDto> getAllTemplates();

  @DeleteMapping("/{id}")
  public void deleteTemplate(@PathVariable("id") UUID id);
}
