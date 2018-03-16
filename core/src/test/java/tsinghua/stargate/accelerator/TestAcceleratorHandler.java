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

package tsinghua.stargate.accelerator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.ByteChannel;
import java.util.Random;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tsinghua.stargate.api.factory.provider.AcceleratorClientFactoryProvider;
import tsinghua.stargate.api.AcceleratorHandler;
import tsinghua.stargate.io.FpgaChannel;
import tsinghua.stargate.rpc.message.entity.Worker;
import tsinghua.stargate.util.FpgaUtils;

public class TestAcceleratorHandler {

  private static final Logger LOG =
      LoggerFactory.getLogger(TestAcceleratorHandler.class);

  @Test
  public void testAcceleratorClient() throws IOException {
    LOG.info("Start single loopback app");

    // Parse input arguments
    int cardId = 0;
    int coreId = 9;
    int numbers = 128;

    ByteBuffer byteBufRecv = ByteBuffer.allocate(numbers);
    ByteBuffer byteBufSend = ByteBuffer.allocate(numbers);
    IntBuffer intBufSend = byteBufSend.asIntBuffer();

    Random random = new Random();

    long startTime = System.currentTimeMillis();
    for (int i = 0; i < numbers / 4; i++) {
      intBufSend.put(i, random.nextInt());
    }
    long endTime = System.currentTimeMillis();

    LOG.info("Time to produce data:" + (endTime - startTime) / 1000 + "s");

    LOG.info("available FPGA info", FpgaUtils.getInfo());

    AcceleratorHandler client = AcceleratorClientFactoryProvider
        .getClientFactory(Worker.FPGA).getClient(cardId, coreId);

    startTime = endTime;
    int numSentBytes = client.send(byteBufSend);
    endTime = System.currentTimeMillis();
    LOG.info("Time to write data:" + (endTime - startTime) / 1000 + "s");

    startTime = endTime;
    int numReceivedBytes = client.receive(byteBufRecv);
    endTime = System.currentTimeMillis();
    LOG.info("Time to read data:" + (endTime - startTime) / 1000 + "s");

    byte tmp0, tmp1;
    boolean isDifferent = false;
    for (int i = 0; i < numbers; i++) {
      tmp0 = byteBufSend.get(i);
      tmp1 = byteBufRecv.get(i);
      if (tmp0 != tmp1) {
        System.out.printf("%d times send %02x receive %02x: data different\n",
            i + 1, tmp0, tmp1);
        isDifferent = true;
      }
    }

    LOG.info("Core:{} send {} bytes", coreId, numSentBytes);
    LOG.info("Core:{} receive {} bytes", coreId, numReceivedBytes);

    if (!isDifferent) {
      LOG.info("Core:{} send and receive the same data", coreId);
    }

    client.close();
  }

  public static void test() {
    LOG.info("Start single loopback app");

    // Parse input arguments
    int cardId = 0;
    int coreId = 9;
    int numbers = 16384;

    ByteBuffer byteBufRecv = ByteBuffer.allocateDirect(numbers);
    ByteBuffer byteBufSend = ByteBuffer.allocateDirect(numbers);
    IntBuffer intBufSend = byteBufSend.asIntBuffer();

    Random random = new Random();

    long startTime = System.currentTimeMillis();
    for (int i = 0; i < numbers / 4; i++) {
      intBufSend.put(i, random.nextInt());
    }
    long endTime = System.currentTimeMillis();

    LOG.info("Time to produce data:" + (endTime - startTime) / 1000 + "s");

    try (ByteChannel channel = FpgaUtils.newByteChannel(cardId, coreId)) {
      LOG.info("Listing available FPGAs...");
      FpgaChannel.getInfo();

      LOG.info("Select the FPGA of id:{}, core:{}", cardId, coreId);
      LOG.info("Reset the selected FPGA");
      FpgaChannel.reset(cardId);

      startTime = endTime;
      int numSentBytes = channel.write(byteBufSend);
      endTime = System.currentTimeMillis();
      LOG.info("Time to write data:" + (endTime - startTime) / 1000 + "s");

      startTime = endTime;
      int numReceivedBytes = channel.read(byteBufRecv);
      endTime = System.currentTimeMillis();
      LOG.info("Time to read data:" + (endTime - startTime) / 1000 + "s");

      byte tmp0, tmp1;
      boolean isDifferent = false;
      for (int i = 0; i < numbers; i++) {
        tmp0 = byteBufSend.get(i);
        tmp1 = byteBufRecv.get(i);
        if (tmp0 != tmp1) {
          System.out.printf("%d times send %02x receive %02x: data different\n",
              i + 1, tmp0, tmp1);
          isDifferent = true;
        }
      }

      LOG.info("Core:{} send {} bytes", coreId, numSentBytes);
      LOG.info("Core:{} receive {} bytes", coreId, numReceivedBytes);

      if (!isDifferent) {
        LOG.info("Core:{} send and receive the same data", coreId);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}