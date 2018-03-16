/*
 * Copyright 2017 The Tsinghua University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tsinghua.stargate.api;

import java.io.IOException;
import java.io.Serializable;

/** Base callback interface for handling user's data. */
public interface StarGateApp extends Serializable {

  /**
   * This method must be implemented to handle user's data.
   *
   * <p>
   * StarGate client is responsible for serializing and delivering this method
   * to StarGate server. Once server receives the binaries, it takes charge of
   * deserializing and posting this method for execution.
   *
   * @param request the request either as the {@link AcceleratorRequest}
   * @return handler process result {@link AcceleratorResponse}
   */
  AcceleratorResponse accelerate(AcceleratorRequest request) throws IOException;
}
