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

package com.epam.digital.data.platform.notification.diia.service;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

import com.epam.digital.data.platform.notification.dto.diia.DiiaNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaPublishTemplateRequestDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaSendNotificationRequestDto;
import com.epam.digital.data.platform.notification.dto.diia.ExternalTemplateId;
import com.epam.digital.data.platform.notification.service.NotificationService;
import com.epam.digital.data.platform.starter.security.exception.JwtParsingException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import java.text.ParseException;
import java.time.Clock;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Slf4j
@RequiredArgsConstructor
public class DiiaService implements NotificationService<DiiaNotificationMessageDto> {

  private static final String BEARER_HEADER_PATTERN = "Bearer %s";

  private final DiiaRestClient diiaRestClient;
  private final String partnerToken;
  private final Clock clock;

  private volatile String accessToken;

  @Override
  public void notify(DiiaNotificationMessageDto message) {
    log.info("Sending notification via diia. RecipientId: '{}'", message.getRecipient().getId());
    var result = diiaRestClient.sendNotification(toRequest(message), createHeaders());
    message.setDistributionId(result.getDistributionId());
    log.info("Diia notification was sent. DistributionId: '{}'", result.getDistributionId());
  }

  public ExternalTemplateId publishTemplate(DiiaPublishTemplateRequestDto template) {
    return diiaRestClient.publishTemplate(template, createHeaders());
  }

  private DiiaSendNotificationRequestDto toRequest(DiiaNotificationMessageDto message) {
    return DiiaSendNotificationRequestDto.builder()
        .templateId(message.getDiiaNotificationDto().getExternalTemplateId())
        .recipients(List.of(message.getRecipient()))
        .build();
  }

  private HttpHeaders createHeaders() {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add(AUTHORIZATION, String.format(BEARER_HEADER_PATTERN, getAccessToken()));
    return headers;
  }

  private String getAccessToken() {
    if (accessToken == null || isTokenExpired(accessToken)) {
      synchronized (this) {
        if (accessToken == null || isTokenExpired(accessToken)) {
          accessToken = diiaRestClient.getToken(partnerToken).getToken();
        }
      }
    }
    return accessToken;
  }

  private boolean isTokenExpired(String accessToken) {
    JWTClaimsSet jwtClaimsSet = getClaimsFromToken(accessToken);
    Date now = new Date(clock.millis());
    return Optional.of(jwtClaimsSet.getExpirationTime())
        .map(now::after)
        .orElse(true);
  }

  private JWTClaimsSet getClaimsFromToken(String accessToken) {
    try {
      return JWTParser.parse(accessToken).getJWTClaimsSet();
    } catch (ParseException e) {
      throw new JwtParsingException(e.getMessage());
    }
  }
}
