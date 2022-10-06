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

package com.epam.digital.data.platform.notification.controller;

import com.epam.digital.data.platform.notification.dto.SaveNotificationTemplateInputDto;
import com.epam.digital.data.platform.notification.dto.SaveNotificationTemplateOutputDto;
import com.epam.digital.data.platform.notification.service.NotificationTemplateFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications/templates")
public class NotificationTemplateController {

  private final NotificationTemplateFacade notificationTemplateFacade;

  @PutMapping("/{channel}:{name}")
  public SaveNotificationTemplateOutputDto saveTemplate(
      @PathVariable("channel") String channel,
      @PathVariable("name") String name,
      @RequestBody SaveNotificationTemplateInputDto input) {
    return notificationTemplateFacade.saveTemplate(channel, name, input);
  }
}
