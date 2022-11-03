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

package com.epam.digital.data.platform.notification.diia;

import com.epam.digital.data.platform.notification.diia.repository.DiiaNotificationTemplateRepository;
import com.epam.digital.data.platform.notification.entity.NotificationTemplate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"data-platform.kafka.enabled=false"})
@ComponentScan(basePackages = "com.epam.digital.data.platform.notification.diia")
@EntityScan("com.epam.digital.data.platform.notification.entity")
public abstract class BaseIT {

  @Autowired
  protected DiiaNotificationTemplateRepository repository;

  public NotificationTemplate createDiiaTemplateInDb(String name, String templateId) {
    var template = NotificationTemplate.builder()
        .id(UUID.randomUUID())
        .name(name)
        .channel("diia")
        .checksum("1234")
        .extTemplateId(templateId)
        .extPublishedAt(LocalDateTime.of(2022, 2, 12, 10, 25, 33))
        .build();
    return repository.save(template);
  }
}
