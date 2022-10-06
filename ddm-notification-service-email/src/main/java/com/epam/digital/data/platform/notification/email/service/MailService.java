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

package com.epam.digital.data.platform.notification.email.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * The service that manages and sends mail messages.
 */
@RequiredArgsConstructor
public class MailService {

  private final JavaMailSender javaMailSender;
  private final String from;

  /**
   * Send message
   *
   * @param subject    message subject
   * @param body       html message body
   * @param recipients specified recipients to whom send the message
   * @throws {@link MailSendException} in case of validation or transfer error
   */
  public void send(String subject, String body, String... recipients) {
    var message = javaMailSender.createMimeMessage();
    var helper = new MimeMessageHelper(message);
    try {
      helper.setFrom(from);
      helper.setTo(recipients);
      helper.setSubject(subject);
      helper.setText(body, true);
      javaMailSender.send(message);
    } catch (Throwable ex) {
      throw new MailSendException(ex.getMessage(), ex);
    }
  }
}

