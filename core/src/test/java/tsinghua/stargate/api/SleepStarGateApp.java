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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Checks results after finishing running on accelerator.
 */
@SuppressWarnings("unchecked")
public class SleepStarGateApp implements StarGateApp {

  private static final long serialVersionUID = 9529L;

  @Override
  public AcceleratorResponse accelerate(AcceleratorRequest request) throws IOException {

    try {
      Thread.sleep(10 * 1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    return new AcceleratorResponse() {
      @Override
      public StarGateAppState getHandlerState() {
        return StarGateAppState.SUCCESS;
      }

      @Override
      public Collection<ByteBuffer> getOutputResult() {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put("SleepStarGateApp".getBytes());
        buffer.flip();
        List<ByteBuffer> bufferList = new ArrayList();
        bufferList.add(buffer);
        return bufferList;
      }
    };
  }
}
