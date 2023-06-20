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

package com.epam.digital.data.platform.notification.core.service;

import com.epam.digital.data.platform.integration.idm.service.IdmService;
import com.epam.digital.data.platform.notification.dto.Recipient.RecipientRealm;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IdmServiceProvider {

  private final IdmService officerIdmService;
  private final IdmService citizenIdmService;

  public IdmService getIdmService(RecipientRealm realm) {
    switch (realm) {
      case OFFICER:
        return officerIdmService;
      case CITIZEN:
        return citizenIdmService;
      default:
        throw new IllegalArgumentException("Realm must be one of ['officer', 'citizen']");
    }
  }
}
