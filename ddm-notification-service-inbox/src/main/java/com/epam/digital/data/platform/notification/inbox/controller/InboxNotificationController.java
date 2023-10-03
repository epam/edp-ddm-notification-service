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
package com.epam.digital.data.platform.notification.inbox.controller;

import com.epam.digital.data.platform.notification.dto.SaveNotificationTemplateOutputDto;
import com.epam.digital.data.platform.notification.dto.inbox.InboxNotificationResponseDto;
import com.epam.digital.data.platform.notification.dto.inbox.InboxOffsetBasedPageRequest;
import com.epam.digital.data.platform.notification.inbox.service.InboxNotificationService;
import com.epam.digital.data.platform.notification.model.DetailedErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@Tag(description = "User inbox notification management Rest API", name = "notification-inbox-api")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications/inbox")
@ConditionalOnProperty(prefix = "notifications", name = "enabled", havingValue = "true")
public class InboxNotificationController {

  private static final String ACCESS_TOKEN = "X-Access-Token";
  private final InboxNotificationService inboxNotificationService;

  @Operation(
      summary = "Viewing the list of in-app messages",
      description = "### Endpoint purpose: \n This endpoint is used for viewing notifications about the status or result of the business process, receiving official messages.\n ### Authorization:\n This endpoint requires valid user authentication. To access this endpoint, the request must include a valid access token in the _X-Access-Token_ header, otherwise, the API will return a _401 Unauthorized_ status code",
      parameters = {
          @Parameter(
              name = "X-Access-Token",
              description = "User access token",
              in = ParameterIn.HEADER,
              schema = @Schema(type = "string")
          ),
          @Parameter(
              name = "offset",
              description = "Record offset",
              in = ParameterIn.QUERY,
              required = true,
              schema = @Schema(type = "integer", defaultValue = "0")
          ),
          @Parameter(
              name = "limit",
              description = "Maximum number of records to return",
              in = ParameterIn.QUERY,
              required = true,
              schema = @Schema(type = "integer", defaultValue = "10")
          ),
          @Parameter(
              name = "sort",
              description = "Field and order for sorting the records. Example: asc(<field>) / desc(<field>)",
              in = ParameterIn.QUERY,
              required = true,
              schema = @Schema(type = "string", defaultValue = "desc(endTime)")
          )
      },
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "OK. List of inbox notifications successfully retrieved.",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = SaveNotificationTemplateOutputDto.class),
                  examples = @ExampleObject(
                      value = "[\n" +
                          "  {\n" +
                          "    \"id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\n" +
                          "    \"subject\": \"Some subject\",\n" +
                          "    \"message\": \"Some message\",\n" +
                          "    \"isAcknowledged\": true,\n" +
                          "    \"createdAt\": \"2021-08-10T10:30:00.000Z\"\n" +
                          "  }\n" +
                          "]"
                  )
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Bad Request.",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized. Missing or invalid access token or digital signature.",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal Server Error. Server error while processing the request.",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = DetailedErrorResponse.class))
          ),
      }
  )
  @GetMapping
  public ResponseEntity<List<InboxNotificationResponseDto>> getInboxNotifications(
      @RequestHeader(ACCESS_TOKEN) String accessToken,
      InboxOffsetBasedPageRequest request) {
    log.info("processing get all notifications request");
    return ResponseEntity.ok()
        .body(inboxNotificationService.getInboxNotifications(accessToken, request));
  }

  @Operation(
      summary = "Confirmation of in-app message",
      description = "### Endpoint purpose: \n This endpoint is used for confirming notification about the status or result of the business process, receiving official messages.\n ### Authorization:\n This endpoint requires valid user authentication. To access this endpoint, the request must include a valid access token in the _X-Access-Token_ header, otherwise, the API will return a _401 Unauthorized_ status code. If the user's ID provided in the JWT token does not match the recipient ID of the message, a 403 Forbidden error will be returned. Only the recipient of the notification can update its state",
      parameters = {
          @Parameter(
              name = "X-Access-Token",
              description = "User access token",
              in = ParameterIn.HEADER,
              schema = @Schema(type = "string")
          ),
          @Parameter(
              name = "id",
              description = "Notification id",
              in = ParameterIn.PATH,
              required = true,
              schema = @Schema(implementation = UUID.class)
          )
      },
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "OK. Inbox notification successfully acknowledged."
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Bad Request.",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized. Missing or invalid access token or digital signature.",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "403",
              description = "Forbidden. Insufficient permissions to perform the operation.",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal Server Error. Server error while processing the request.",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = DetailedErrorResponse.class))
          ),
      }
  )
  @PostMapping("/{id}/ack")
  public ResponseEntity<Void> acknowledgeNotification(
      @PathVariable("id") UUID id,
      @RequestHeader(ACCESS_TOKEN) String accessToken) {
    log.info("processing acknowledgeNotification request");
    inboxNotificationService.acknowledgeNotification(id, accessToken);
    return ResponseEntity.ok().build();
  }
}