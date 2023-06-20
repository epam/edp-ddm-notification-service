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
package com.epam.digital.data.platform.notification.core.template;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"data-platform.kafka.enabled=false"})
@ComponentScan(basePackages = "com.epam.digital.data.platform.notification.core")
@EntityScan("com.epam.digital.data.platform.notification.entity")
class FreemarkerTemplateResolverIT {

  @Autowired
  private FreemarkerTemplateResolver templateResolver;

  @Test
  void shouldResoleTemplate() {
    var template = "<html>"
        + "[#if recipientRoles?seq_contains(\"citizen\")]"
        + "Confirmation code: ${verificationCode} for citizen user"
        + "[#elseif recipientRoles?seq_contains(\"officer\")]"
        + "Confirmation code: ${verificationCode} for officer user"
        + "[/#if]"
        + "</html>";
    var templateModel  = new HashMap<String, Object>();
    var roles = List.of("officer");
    templateModel.put("verificationCode", "123");
    templateModel.put("recipientRoles", roles);

    var result = templateResolver.resolve("name", template, templateModel);

    assertThat(result).isEqualTo("<html>Confirmation code: 123 for officer user</html>");
  }

}
