/*
 *  Copyright 2022 EPAM Systems.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.epam.digital.data.platform.notification.inbox.exception;

import com.epam.digital.data.platform.notification.inbox.exception.model.ForbiddenNotificationActionException;
import com.epam.digital.data.platform.notification.inbox.exception.model.ParsingException;
import com.epam.digital.data.platform.notification.model.DetailedErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class InboxApplicationExceptionHandler extends ResponseEntityExceptionHandler {

  private final Logger log = LoggerFactory.getLogger(InboxApplicationExceptionHandler.class);

  @ExceptionHandler(ParsingException.class)
  public ResponseEntity<DetailedErrorResponse<Void>> handleParsingException(
      ParsingException exception) {
    log.error("Parsing exception", exception);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(newDetailedResponse(HttpStatus.BAD_REQUEST.toString()));
  }

  @ExceptionHandler(ForbiddenNotificationActionException.class)
  public ResponseEntity<DetailedErrorResponse<Void>> handleForbiddenNotificationActionException(
      ForbiddenNotificationActionException exception) {
    log.error("Forbidden exception", exception);
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(newDetailedResponse(HttpStatus.FORBIDDEN.toString()));
  }

  private <T> DetailedErrorResponse<T> newDetailedResponse(String code) {
    var response = new DetailedErrorResponse<T>();
    response.setTraceId(MDC.get("X-B3-TraceId"));
    response.setCode(code);
    return response;
  }
}
