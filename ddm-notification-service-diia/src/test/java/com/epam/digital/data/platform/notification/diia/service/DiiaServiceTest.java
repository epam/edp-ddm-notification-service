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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.notification.dto.diia.DiiaNotificationDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaNotificationMessageDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaRecipientDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaSendNotificationRequestDto;
import com.epam.digital.data.platform.notification.dto.diia.DiiaToken;
import com.epam.digital.data.platform.notification.dto.diia.DistributionId;
import java.time.Clock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

@ExtendWith(MockitoExtension.class)
class DiiaServiceTest {

  private static final String PARTNER_TOKEN = "partnerToken";
  private static final String TEMPLATE_NAME = "templateName";
  private static final String TEMPLATE_ID = "templateId";

  @Mock
  private DiiaRestClient feignClient;

  private DiiaService service;

  @BeforeEach
  void beforeEach() {
    service = new DiiaService(feignClient, PARTNER_TOKEN, Clock.systemDefaultZone());
  }

  @Test
  void testNotify() {
    when(feignClient.getToken(PARTNER_TOKEN)).thenReturn(new DiiaToken("accessToken"));
    when(feignClient.sendNotification(
        any(DiiaSendNotificationRequestDto.class), any(HttpHeaders.class)))
        .thenReturn(new DistributionId("distributionId"));

    var notification = DiiaNotificationDto.builder()
        .templateName(TEMPLATE_NAME)
        .externalTemplateId(TEMPLATE_ID)
        .build();
    var recipient = DiiaRecipientDto.builder().rnokpp("rnokpp").build();
    var msgDto = DiiaNotificationMessageDto.builder()
        .notification(notification).recipient(recipient)
        .build();

    service.notify(msgDto);

    verify(feignClient, times(1)).getToken(PARTNER_TOKEN);
    verify(feignClient, times(1))
        .sendNotification(any(DiiaSendNotificationRequestDto.class), any(HttpHeaders.class));
  }
}
