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

import java.nio.ByteBuffer;
import java.util.Collection;

import tsinghua.stargate.api.impl.AcceleratorResponseImpl;

/**
 * Defines an object to assist a handler in sending a response to the client.
 * The stargate server creates a <code>handlerResponse</code> object and passes
 * it as an argument to the handler service method.
 *
 * <p>
 * <code>AcceleratorResponse</code> object provides info including the state processed
 * by handler , the next data buffer and target file will be writted into
 * accelerator task,the accelerator client destination off,the next receive data
 * buffer size from accelerator client,finally if the accelerator task finish
 * then the handler will be return final result,.Interfaces that extend
 * <code>AppRequest</code> can provide additional info {@link AcceleratorResponseImpl}.
 */
public interface AcceleratorResponse {

  /** Returns handler process state. */
  StarGateAppState getHandlerState();

  /** Get the final result buffers will be setted to task output. */
  Collection<ByteBuffer> getOutputResult();
}
