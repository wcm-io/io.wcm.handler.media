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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.ThreadFactory;

import org.junit.jupiter.api.Test;

class NamedThreadFactoryTest {

  private static final Runnable NOOP = () -> {
    // do nothing
  };

  @Test
  void testNewThread() {
    ThreadFactory underTest = new NamedThreadFactory("mythread");

    Thread thread1 = underTest.newThread(NOOP);
    assertEquals("mythread-0", thread1.getName());

    Thread thread2 = underTest.newThread(NOOP);
    assertEquals("mythread-1", thread2.getName());
  }

}
