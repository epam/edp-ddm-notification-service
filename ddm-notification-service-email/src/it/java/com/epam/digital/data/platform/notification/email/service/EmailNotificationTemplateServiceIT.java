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

package com.epam.digital.data.platform.notification.email.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.epam.digital.data.platform.notification.email.BaseIT;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class EmailNotificationTemplateServiceIT extends BaseIT {

  @Autowired
  private EmailNotificationTemplateService emailTemplateService;

  @Test
  void shouldGetTemplateByName() {
    var name = "test-name";
    var content = "<html>Hello ${name}!</html>";
    createEmailTemplateInDb(name, content, null);

    var result = emailTemplateService.getByName(name);

    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(content);
  }

  @Test
  void shouldGetTemplateTitle() {
    var name = "test-name2";
    var content = "<html>Hello ${name}!</html>";
    var title = "sign notification";
    createEmailTemplateInDb(name, content, title);

    var result = emailTemplateService.getTitleByTemplateName(name);

    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(title);
  }

  @Test
  void shouldGetTemplateByNameAndChannel() {
    var name = "test-name3";
    var content = "<html>Hello ${name}!</html>";
    createEmailTemplateInDb(name, content, null);

    var result = emailTemplateService.getByNameAndChannel(name, Channel.EMAIL);

    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(content);
  }
}
