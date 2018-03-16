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
import java.util.Map;

/**
 * Defines an object to provide client request information to a handler. The
 * client creates a <code>AcceleratorRequest</code> object and passes it as an
 * argument to the handler's <code>service</code> method.
 *
 * <p>
 * <code>ServletRequest</code> object provides info including target file name
 * and byte values, last the received result data from accelerator.
 **/
public interface AcceleratorRequest {

  /** Returns all the DataSet buffer which is setted <em>service data</em> */
  Map<String, ByteBuffer> getInputData();

  AcceleratorHandler getClient();

  /**
   * get the allocated accelerator card id
   * 
   * @return the allocated accelerator card id
   */
  int getCardId();

  /**
   * get the allocated accelerator card core id
   * 
   * @return the allocated accelerator card core id
   */
  int getCoreId();

  RecordReader getRecordReader();

}
