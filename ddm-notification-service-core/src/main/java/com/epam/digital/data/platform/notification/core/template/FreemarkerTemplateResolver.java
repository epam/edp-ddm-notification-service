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

package com.epam.digital.data.platform.notification.core.template;

import com.epam.digital.data.platform.notification.exception.TemplateProcessingException;
import com.epam.digital.data.platform.notification.template.TemplateResolver;
import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.StringWriter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The template resolver that uses Apache FreeMarker template engine
 */
@Component
@RequiredArgsConstructor
public class FreemarkerTemplateResolver implements TemplateResolver<String, String> {

  private final Configuration freemarkerConfig;

  @Override
  public String resolve(String templateName, String template, Map<String, Object> model)
      throws TemplateProcessingException {
    try (var out = new StringWriter()) {
      var freemarkerTemplate = new Template(templateName, template, freemarkerConfig);
      freemarkerTemplate.process(model, out);
      return out.toString();
    } catch (Exception ex) {
      throw new TemplateProcessingException(templateName, ex);
    }
  }
}
