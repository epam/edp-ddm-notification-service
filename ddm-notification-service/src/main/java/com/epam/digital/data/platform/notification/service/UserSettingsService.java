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

package com.epam.digital.data.platform.notification.service;

import com.epam.digital.data.platform.bpms.api.dto.enums.PlatformHttpHeader;
import com.epam.digital.data.platform.datafactory.settings.client.UserSettingsFeignClient;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import com.epam.digital.data.platform.settings.model.dto.SettingsReadDto;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * The service for managing user settings. It uses system realm for getting access to keycloak
 */
@Slf4j
@RequiredArgsConstructor
public class UserSettingsService {

  private final IdmService systemIdmService;
  private final UserSettingsFeignClient userSettingsFeignClient;

  /**
   * Get user settings by username
   *
   * @param username specified username
   * @return {@link SettingsReadDto} with user settings data
   */
  public SettingsReadDto getByUsername(String username) {
    log.info("Getting user settings by username");
    var users = systemIdmService.getUserByUserName(username);
    if (users.isEmpty()) {
      throw new IllegalArgumentException("User not found by username");
    }
    var user = users.get(0);
    var accessToken = systemIdmService.getClientAccessToken();
    var result = userSettingsFeignClient.performGetByUserId(UUID.fromString(user.getId()),
        createHeaders(accessToken));
    if (Objects.nonNull(result)) {
      log.info("Found user settings");
    }
    return result;
  }

  private HttpHeaders createHeaders(String accessToken) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add(PlatformHttpHeader.X_ACCESS_TOKEN.getName(), accessToken);
    return headers;
  }
}
