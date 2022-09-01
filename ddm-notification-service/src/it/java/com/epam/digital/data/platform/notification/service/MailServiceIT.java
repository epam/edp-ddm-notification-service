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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.epam.digital.data.platform.notification.BaseIT;
import com.icegreen.greenmail.util.GreenMailUtil;
import javax.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MailServiceIT extends BaseIT {

  @Autowired
  private MailService mailService;

  @Test
  void shouldSendEmail() throws MessagingException {
    var email = "test@test.com";
    var subject = "Subject";
    var payload = "<html>Hello John!</html>";

    mailService.send(subject, payload, email);

    var receivedMessages = greenMail.getReceivedMessages();
    assertThat(receivedMessages).isNotEmpty();
    var receivedMessage = receivedMessages[0];
    assertThat(GreenMailUtil.getBody(receivedMessage)).isEqualTo(payload);
    assertThat(receivedMessage.getSubject()).isEqualTo(subject);
    assertThat(receivedMessage.getAllRecipients()[0].toString()).isEqualTo(email);
  }
}
