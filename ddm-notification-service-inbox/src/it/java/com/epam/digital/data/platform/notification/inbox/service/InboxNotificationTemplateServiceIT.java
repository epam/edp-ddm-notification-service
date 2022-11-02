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

package com.epam.digital.data.platform.notification.inbox.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.epam.digital.data.platform.notification.inbox.BaseIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class InboxNotificationTemplateServiceIT extends BaseIT {

  @Autowired
  private InboxNotificationTemplateService inboxTemplateService;

  @Test
  void shouldGetTemplateByName() {
    var name = "test-name";
    var content = "Hello ${name}!";
    createInboxTemplateInDb(name, content, null);

    var result = inboxTemplateService.getByName(name);

    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(content);
  }

  @Test
  void shouldGetTemplateTitle() {
    var name = "test-name2";
    var content = "Hello ${name}!";
    var title = "sign notification";
    createInboxTemplateInDb(name, content, title);

    var result = inboxTemplateService.getTitleByTemplateName(name);

    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(title);
  }
}
