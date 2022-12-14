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
package com.epam.digital.data.platform.notification.core;

import com.epam.digital.data.platform.notification.core.repository.CoreNotificationTemplateRepository;
import com.epam.digital.data.platform.notification.entity.NotificationTemplate;
import java.time.LocalDateTime;
import java.util.UUID;
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
public class BaseIT {

  @Autowired
  protected CoreNotificationTemplateRepository repository;

  public NotificationTemplate createTemplateInDb(String name, String content, String title) {
    var template = NotificationTemplate.builder()
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .channel("email")
        .checksum("1234")
        .content(content)
        .name(name)
        .title(title)
        .id(UUID.randomUUID())
        .build();
    return repository.save(template);
  }

}
