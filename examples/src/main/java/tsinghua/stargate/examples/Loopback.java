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

package tsinghua.stargate.examples;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;

import tsinghua.stargate.StarGateContext;
import tsinghua.stargate.api.*;
import tsinghua.stargate.api.impl.AcceleratorResponseImpl;
import tsinghua.stargate.conf.StarGateConf;
import tsinghua.stargate.exception.StarGateException;
import tsinghua.stargate.rpc.message.entity.BlockStoreType;
import tsinghua.stargate.rpc.message.entity.ServiceData;
import tsinghua.stargate.rpc.message.entity.Worker;
import tsinghua.stargate.util.ArrayUtils;
import tsinghua.stargate.util.PathUtils;
import tsinghua.stargate.util.Utils;

@SuppressWarnings("unchecked")
public class Loopback implements StarGateApp {

  private static final long serialVersionUID = 9527L;

  private static final String PROJECT_ROOT = PathUtils.root();
  private static final String RESULT_PATH = PROJECT_ROOT + "data"
      + PathUtils.SEPARATOR + "Loopback" + PathUtils.SEPARATOR + "result";

  private static final int SEND_BYTES = 64 * 1024;

  public static void main(String[] args) {
    StarGateConf sgConf =
        new StarGateConf().setAppName("Loopback").setWorker(Worker.FPGA)
            .setWorkload("loopback").setProcessor(getProcessor())
            .setOutSD(getOutSD()).setResource(Utils.getExampleJar());

    StarGateContext sgc = new StarGateContext(sgConf);
    try {
      sgc.waitForCompletion();
    } catch (StarGateException e) {
      e.printStackTrace();
    }
  }

  private static Map<String, String> getProcessor() {
    Map<String, String> processor = new HashMap<>();
    processor.put(StarGateApp.class.getSimpleName(),
        Loopback.class.getCanonicalName());
    return processor;
  }

  private static ServiceData getOutSD() {
    return ServiceData.newInstance(RESULT_PATH)
        .setStoreType(BlockStoreType.DISK).setCached(true);
  }

  @Override
  public AcceleratorResponse accelerate(AcceleratorRequest request)
      throws IOException {
    ByteBuffer sndBuf = produceSendBuffer();
    ByteBuffer rcvBuf = ByteBuffer.allocate(SEND_BYTES);
    compute(request.getClient(), sndBuf, rcvBuf);
    return verifyResult(sndBuf, rcvBuf);
  }

  // Prepare send buffer with random data
  private ByteBuffer produceSendBuffer() {
    Random random = new Random();
    ByteBuffer buf = ByteBuffer.allocate(SEND_BYTES);
    int intLen = SEND_BYTES / 4;
    IntBuffer intBuf = buf.asIntBuffer();
    for (int i = 0; i < intLen; i++) {
      intBuf.put(random.nextInt(Integer.MAX_VALUE));
    }
    return buf;
  }

  private void compute(AcceleratorHandler client, ByteBuffer sndBuf,
      ByteBuffer rcvBuf) throws IOException {
    client.send(sndBuf);
    sndBuf.flip();
    client.receive(rcvBuf);
    rcvBuf.flip();
  }

  private AcceleratorResponse verifyResult(ByteBuffer sndBuf,
      ByteBuffer rcvBuf) {
    byte sndByte, rcvByte;
    boolean isDifferent = false;

    if (sndBuf == null || rcvBuf == null) {
      return new AcceleratorResponseImpl(StarGateAppState.FAIL);
    }

    int sndCap = sndBuf.capacity();
    int rcvCap = rcvBuf.capacity();

    if (sndCap != rcvCap) {
      return new AcceleratorResponseImpl(StarGateAppState.FAIL);
    }

    for (int i = 0; i < sndCap; i++) {
      sndByte = sndBuf.get(i);
      rcvByte = rcvBuf.get(i);
      if (sndByte != rcvByte) {
        isDifferent = true;
      }
    }

    String result = isDifferent ? "Loopback sends and receives different data\n"
        : "Loopback sends and receives the same data\n";
    StringBuilder sb = new StringBuilder();
    sb.append("\n-----------------------------------------------------\n")
        .append("Successfully sent ").append(sndBuf.capacity())
        .append(" bytes\n").append("Successfully received ")
        .append(rcvBuf.capacity()).append(" bytes\n").append(result)
        .append("-----------------------------------------------------\n");

    List<ByteBuffer> results = new ArrayList<>();
    results.add(ArrayUtils.bytes2Buffer(sb.toString().getBytes()));
    return new AcceleratorResponseImpl(StarGateAppState.SUCCESS, results);
  }
}
