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

package com.epam.digital.data.platform.notification.template;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.epam.digital.data.platform.notification.BaseIT;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class FreemarkerTemplateResolverIT extends BaseIT {

  @Autowired
  private FreemarkerTemplateResolver templateResolver;

  @Test
  void shouldResoleTemplate() {
    var template = "<html>Hello ${name}!</html>";
    Map<String, Object> templateModel  = Map.of("name", "John");

    var result = templateResolver.resolve("name", template, templateModel);

    assertThat(result).isEqualTo("<html>Hello John!</html>");
  }

}
