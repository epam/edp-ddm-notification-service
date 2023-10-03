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

package com.epam.digital.data.platform.notification.controller;

import com.epam.digital.data.platform.notification.dto.NotificationTemplateShortInfoResponseDto;
import com.epam.digital.data.platform.notification.dto.SaveNotificationTemplateInputDto;
import com.epam.digital.data.platform.notification.dto.SaveNotificationTemplateOutputDto;
import com.epam.digital.data.platform.notification.model.DetailedErrorResponse;
import com.epam.digital.data.platform.notification.service.NotificationTemplateFacade;
import com.epam.digital.data.platform.notification.service.NotificationTemplateService;
import java.util.List;
import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(description = "User notification template management Rest API", name = "notification-template-api")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications/templates")
public class NotificationTemplateController {

  private final NotificationTemplateFacade notificationTemplateFacade;

  private final NotificationTemplateService notificationTemplateService;

  @Operation(
      summary = "Model notification templates separately for each of the communication channels",
      description = "### Endpoint purpose: \n This endpoint provides an opportunity to model notification templates separately for each of the communication channels. \n ### Authorization:\n This endpoint requires valid user authentication. To access this endpoint, the request must include a valid access token in the _X-Access-Token_ header, otherwise, the API will return a _401 Unauthorized_ status code",
      parameters = {
          @Parameter(
              name = "X-Access-Token",
              description = "User access token",
              in = ParameterIn.HEADER,
              schema = @Schema(type = "string")
          ),
          @Parameter(
              name = "channel",
              description = "Communication channel for using the message template. Unique in combination with name\n" +
                  "\n" +
                  "inbox - Citizen portal\n" +
                  "\n" +
                  "email - email\n" +
                  "\n" +
                  "diia - Diia application (Ukrainian citizen-facing solution, UA-specific)",
              in = ParameterIn.PATH,
              required = true,
              schema = @Schema(type = "string")
          ),
          @Parameter(
              name = "name",
              description = "Template message internal name. Unique in combination with channel",
              in = ParameterIn.PATH,
              required = true,
              schema = @Schema(type = "string")
          )
      },
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = SaveNotificationTemplateInputDto.class),
              examples = @ExampleObject(
                  value = "{\n" +
                      "  \"title\": \"New notification\",\n" +
                      "  \"content\": \"Hello world\",\n" +
                      "  \"attributes\": [\n" +
                      "    {\n" +
                      "      \"name\": \"attribute1\",\n" +
                      "      \"value\": \"value1\"\n" +
                      "    },\n" +
                      "    {\n" +
                      "      \"name\": \"attribute2\",\n" +
                      "      \"value\": \"value2\"\n" +
                      "    }\n" +
                      "  ]\n" +
                      "}"
              )
          )
      ),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "OK. Notification templates successfully saved.",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = SaveNotificationTemplateOutputDto.class),
                  examples = @ExampleObject(
                      value = "{\n" +
                          "  \"name\": \"Notification Template 1\",\n" +
                          "  \"channel\": \"email\",\n" +
                          "  \"title\": \"New notification\",\n" +
                          "  \"content\": \"Hello world\",\n" +
                          "  \"checksum\": \"1234567890\",\n" +
                          "  \"attributes\": [\n" +
                          "    {\n" +
                          "      \"name\": \"attribute1\",\n" +
                          "      \"value\": \"value1\"\n" +
                          "    },\n" +
                          "    {\n" +
                          "      \"name\": \"attribute2\",\n" +
                          "      \"value\": \"value2\"\n" +
                          "    }\n" +
                          "  ],\n" +
                          "  \"createdAt\": \"2022-01-01T12:00:00.000Z\",\n" +
                          "  \"updatedAt\": \"2022-01-02T12:00:00.000Z\",\n" +
                          "  \"externalTemplateId\": \"abcd1234\",\n" +
                          "  \"externallyPublishedAt\": \"2022-01-03T12:00:00.000Z\"\n" +
                          "}"
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
  @PutMapping("/{channel}:{name}")
  public SaveNotificationTemplateOutputDto saveTemplate(
      @PathVariable("channel") String channel,
      @PathVariable("name") String name,
      @RequestBody SaveNotificationTemplateInputDto input) {
    return notificationTemplateFacade.saveTemplate(channel, name, input);
  }

  @GetMapping("/")
  public List<NotificationTemplateShortInfoResponseDto> getAllTemplates() {
    return notificationTemplateService.getAll();
  }

  @DeleteMapping("/{id}")
  public void deleteTemplate(@PathVariable("id") UUID id) {
    notificationTemplateService.delete(id);
  }
}
