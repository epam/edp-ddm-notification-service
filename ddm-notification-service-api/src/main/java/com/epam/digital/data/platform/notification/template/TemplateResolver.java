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

package com.epam.digital.data.platform.notification.template;

import com.epam.digital.data.platform.notification.exception.TemplateProcessingException;
import java.util.Map;

/**
 * The interface that represents template resolving contract
 *
 * @param <R> resolved template result type
 * @param <T> input template type
 */
public interface TemplateResolver<R, T> {

  /**
   * Populate template using template model data
   *
   * @param templateName  specified template name
   * @param template      to fill
   * @param templateModel contains data that populates the template
   * @return populated template
   * @throws {@link TemplateProcessingException} if during the process an error occurred
   */
  R resolve(String templateName, T template, Map<String, Object> templateModel)
      throws TemplateProcessingException;
}
