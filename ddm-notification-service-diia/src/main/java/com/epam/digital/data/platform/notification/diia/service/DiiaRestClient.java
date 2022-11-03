/*
 * Copyright 2022 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.notification.diia.service;

import com.epam.digital.data.platform.notification.diia.config.FeignConfig;
import com.epam.digital.data.platform.notification.dto.diia.DiiaPublishTemplateRequestDto;
import com.epam.digital.data.platform.notification.dto.diia.ExternalTemplateId;
import com.epam.digital.data.platform.notification.dto.diia.DiiaSendNotificationRequestDto;
import com.epam.digital.data.platform.notification.dto.diia.DistributionId;
import com.epam.digital.data.platform.notification.dto.diia.DiiaToken;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "diia-client", url = "${notifications.diia.url}", configuration = FeignConfig.class)
public interface DiiaRestClient {

  @GetMapping("/api/v1/auth/partner/{partnerToken}")
  DiiaToken getToken(@PathVariable("partnerToken") String partnerToken);

  @PostMapping(path = "/api/v1/notification/template")
  ExternalTemplateId publishTemplate(
      @RequestBody DiiaPublishTemplateRequestDto diiaPublishTemplateRequestDto,
      @RequestHeader HttpHeaders headers
  );

  @PostMapping(path = "/api/v1/notification/distribution/push")
  DistributionId sendNotification(
      @RequestBody DiiaSendNotificationRequestDto sendNotificationRequestDto,
      @RequestHeader HttpHeaders headers
  );
}
