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

package com.epam.digital.data.platform.notification.service;

import static java.util.stream.Collectors.toList;

import com.epam.digital.data.platform.bpms.api.dto.enums.PlatformHttpHeader;
import com.epam.digital.data.platform.datafactory.settings.client.UserSettingsFeignClient;
import com.epam.digital.data.platform.notification.core.service.IdmServiceProvider;
import com.epam.digital.data.platform.notification.dto.Recipient;
import com.epam.digital.data.platform.settings.model.dto.SettingsReadDto;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * The service for managing user settings. It uses citizen/officer realm for getting access to
 * keycloak.
 */
@Slf4j
@RequiredArgsConstructor
public class UserService {

  private final IdmServiceProvider idmServiceProvider;
  private final UserSettingsFeignClient userSettingsFeignClient;

  /**
   * Retrieves the user settings for a given recipient.
   *
   * @param recipient the user for which to retrieve the settings.
   * @return {@link SettingsReadDto} with user settings data
   */
  public SettingsReadDto getUserSettings(Recipient recipient) {
    log.info("Getting user settings by username");
    var idmService = idmServiceProvider.getIdmService(recipient.getRealm());
    var users = idmService.getUserByUserName(recipient.getId());
    if (users.isEmpty()) {
      throw new IllegalArgumentException("User not found by username");
    }
    var user = users.get(0);
    var accessToken = idmService.getClientAccessToken();
    var result = userSettingsFeignClient.performGetByUserId(UUID.fromString(user.getId()),
        createHeaders(accessToken));
    if (Objects.nonNull(result)) {
      log.info("Found user settings");
    }
    return result;
  }

  /**
   * Retrieves the roles associated with a recipient
   *
   * @param recipient the user for which to retrieve the roles
   * @return a list of roles assigned to the recipient
   */
  public List<String> getUserRoles(Recipient recipient) {
    log.info("Getting recipient roles by username: {}", recipient.getId());
    var idmService = idmServiceProvider.getIdmService(recipient.getRealm());
    var recipientRoles = idmService.getUserRoles(recipient.getId());
    log.info("Found {} recipient roles", recipientRoles.size());
    return recipientRoles.stream()
        .map(RoleRepresentation::getName)
        .collect(toList());
  }

  private HttpHeaders createHeaders(String accessToken) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add(PlatformHttpHeader.X_ACCESS_TOKEN.getName(), accessToken);
    return headers;
  }
}
