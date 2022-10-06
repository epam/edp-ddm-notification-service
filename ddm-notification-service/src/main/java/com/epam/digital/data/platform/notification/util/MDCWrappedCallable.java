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

package com.epam.digital.data.platform.notification.util;

import java.util.Map;
import java.util.concurrent.Callable;
import org.slf4j.MDC;

public class MDCWrappedCallable<T> implements Callable<T> {

  private final Callable<T> callable;
  private final Map<String, String> context;

  public MDCWrappedCallable(Callable<T> callable) {
    this.callable = callable;
    context = MDC.getCopyOfContextMap();
  }

  @Override
  public T call() throws Exception {
    try {
      MDC.setContextMap(this.context);
      return callable.call();
    } finally {
      MDC.clear();
    }
  }
}
