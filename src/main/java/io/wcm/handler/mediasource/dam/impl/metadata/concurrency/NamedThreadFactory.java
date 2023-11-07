/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2023 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.handler.mediasource.dam.impl.metadata.concurrency;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Creates new threads with a given formatted name, including a counter that is incremented for each new thread.
 */
public final class NamedThreadFactory implements ThreadFactory {

  private final String namePrefix;
  private final AtomicLong counter = new AtomicLong();

  /**
   * @param namePrefix Prefix for thread name, will be suffixed with "-{number}".
   */
  public NamedThreadFactory(String namePrefix) {
    this.namePrefix = namePrefix;
  }

  @Override
  public Thread newThread(Runnable r) {
    Thread thread = Executors.defaultThreadFactory().newThread(r);
    thread.setName(namePrefix + "-" + counter.getAndIncrement());
    return thread;
  }

}
