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

package tsinghua.stargate.serializer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import tsinghua.stargate.api.AcceleratorRequest;
import tsinghua.stargate.api.AcceleratorResponse;
import tsinghua.stargate.api.StarGateAppState;
import tsinghua.stargate.api.StarGateApp;
import tsinghua.stargate.util.ArrayUtils;

class TestStarGateApp implements StarGateApp {

  @Override
  public AcceleratorResponse accelerate(AcceleratorRequest request) {
    return new AcceleratorResponse() {
      @Override
      public StarGateAppState getHandlerState() {
        return StarGateAppState.SUCCESS;
      }

      @Override
      public Collection<ByteBuffer> getOutputResult() {
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.put("testHandler".getBytes());
        buffer.flip();
        List<ByteBuffer> bufferList = new ArrayList<>();
        bufferList.add(buffer);
        return bufferList;
      }
    };
  }
}

public class TestSerializer {

  @Test
  public void testSerializer() throws Exception {

    StarGateApp sga = new TestStarGateApp();

    JavaSerializer serializer = new JavaSerializer();
    ByteBuffer buffer = serializer.serialize(sga);
    StarGateApp sga1 = (StarGateApp) serializer.deserialize(buffer);

    AcceleratorResponse response = sga1.accelerate(null);
    Assert.assertEquals(response.getHandlerState(), StarGateAppState.SUCCESS);

    Assert.assertEquals(
        new String((ArrayUtils
            .buffer2Bytes(response.getOutputResult().iterator().next()))),
        "testHandler");
  }
}
