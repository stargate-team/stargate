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

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.*;

import tsinghua.stargate.StarGateContext;
import tsinghua.stargate.api.*;
import tsinghua.stargate.api.factory.provider.AcceleratorClientFactoryProvider;
import tsinghua.stargate.api.impl.AcceleratorResponseImpl;
import tsinghua.stargate.conf.StarGateConf;
import tsinghua.stargate.exception.StarGateException;
import tsinghua.stargate.io.KMeansConfig;
import tsinghua.stargate.io.ModuleConfig;
import tsinghua.stargate.rpc.message.entity.BlockStoreType;
import tsinghua.stargate.rpc.message.entity.ServiceData;
import tsinghua.stargate.rpc.message.entity.Worker;
import tsinghua.stargate.util.FpgaUtils;
import tsinghua.stargate.util.PathUtils;
import tsinghua.stargate.util.Utils;

public class KMeans implements StarGateApp {

  private static final String PROJECT_ROOT = PathUtils.root();
  private static final String APP_DATA_PATH =
      PROJECT_ROOT + "data" + PathUtils.SEPARATOR + "KMeans";
  private static final String SOURCE_PATH =
      APP_DATA_PATH + PathUtils.SEPARATOR + "source";
  private static final String RESULT_PATH =
      APP_DATA_PATH + PathUtils.SEPARATOR + "result";

  // Specify hardware bitstream parent dir.
  private static final String BITSTREAM_PATH =
      PathUtils.rootStr() + PathUtils.SEPARATOR + "data" + PathUtils.SEPARATOR
          + "KMeans" + PathUtils.SEPARATOR + "bitstream";

  // Specify hardware bitstream parent dir, if you plan to use software version,
  // uncomment this and export XCL_EMULATION_MODE=sw_emu in
  // sbin/star-stargate.sh.
  // private static final String BITSTREAM_PATH =
  // PathUtils.rootStr() + PathUtils.SEPARATOR + "core" + PathUtils.SEPARATOR
  // + "src" + PathUtils.SEPARATOR + "main" + PathUtils.SEPARATOR
  // + "native" + PathUtils.SEPARATOR + "out";

  private final KMeansConfig params = new KMeansConfig();

  private static final String TYPE = "kmeans";
  private static final int TIMEOUT = 2500;
  private static final ModuleConfig.DATATYPE FLOAT_TYPE =
      ModuleConfig.DATATYPE.FLOATTYPE;
  private static final int MIN_CLUSTERS = 5;
  private static final int MAX_CLUSTERS = 5;
  private static final float THRESHOLD = 0.01f;

  public static void main(String[] args) {
    StarGateConf sgConf = new StarGateConf().setAppName("KMeans")
        .setWorker(Worker.FPGA).setWorkload("kmeans")
        .setProcessor(getProcessors()).setInSD(getInSD()).setOutSD(getOutSD())
        .setResource(Utils.getExampleJar());

    StarGateContext sgc = new StarGateContext(sgConf);
    try {
      sgc.waitForCompletion();
    } catch (StarGateException e) {
      e.printStackTrace();
    }
  }

  private static Map<String, String> getProcessors() {
    Map<String, String> processors = new HashMap<>();
    processors.put(StarGateApp.class.getSimpleName(),
        KMeans.class.getCanonicalName());
    processors.put(RecordReader.class.getSimpleName(),
        Utils.getClassName(KMeansRR.class));
    return processors;
  }

  private static ServiceData getInSD() {
    return ServiceData.newInstance(SOURCE_PATH)
        .setStoreType(BlockStoreType.DISK).setCached(true);
  }

  private static ServiceData getOutSD() {
    return ServiceData.newInstance(RESULT_PATH)
        .setStoreType(BlockStoreType.DISK).setCached(true);
  }

  @Override
  public AcceleratorResponse accelerate(AcceleratorRequest request)
      throws IOException {
    // Print hardware information.
    FpgaUtils.getInfo();

    Map<Long, Long> fileRowIndex2byteSize = request.getRecordReader().getPos();
    int rowNum = fileRowIndex2byteSize.size();
    int byteSizeOfEachRow = fileRowIndex2byteSize.get(1L).intValue();
    // All digits are all float types.
    int colNum = byteSizeOfEachRow / 4;
    // Configure kmeans module.
    setParams(rowNum, colNum);

    // Get an accelerator handler.
    AcceleratorHandler ah =
        AcceleratorClientFactoryProvider.getClientFactory(Worker.FPGA)
            .getClient(request.getCardId(), request.getCoreId(), params);

    // Prepare the data to be sent.
    Map<String, ByteBuffer> sndBufs = request.getInputData();
    ByteBuffer sndBuf = sndBufs.entrySet().iterator().next().getValue();
    // Send prepared data to simulation environment or hardware.
    ah.send(sndBuf);

    // Receive the computing result of kmeans.
    int numbers = params.getMinNclusters() * params.getRow() * 4;
    // 700
    System.out.println("\nnumbers = " + numbers + "\n");
    ByteBuffer rcvBuf = ByteBuffer.allocate(4 + 400 + 4 + numbers);
    FloatBuffer floatBuf =
        rcvBuf.order(ByteOrder.nativeOrder()).asFloatBuffer();
    ah.receive(rcvBuf);

    // Format and print results.
    StringBuilder sb = new StringBuilder();
    sb.append("\n################ label ################\n");
    // 404.0
    System.out.println("floatBuf.get(0) = " + floatBuf.get(0));
    for (int i = 0; i < 100; i++) {
      if ((i != 0) && (i % 10 == 0)) {
        sb.append('\n');
      }
      sb.append(floatBuf.get(1 + i) + " ");
    }
    sb.append('\n');
    sb.append(
        "\n################ Centroid Coordinates ################\n");
    for (int i = 0; i < params.getMinNclusters(); i++) {
      sb.append(i + ": ");
      for (int j = 0; j < params.getRow(); j++) {
        sb.append(floatBuf.get(1 + 100 + 1 + i * params.getRow() + j) + " ");
      }
      sb.append('\n');
    }
    System.out.println(sb.toString());

    // Prepare response for output to `RESULT_PATH`
    List<ByteBuffer> bufs = new ArrayList<>();
    bufs.add(ByteBuffer.wrap(sb.toString().getBytes()));

    // Close handler
    ah.close();

    return new AcceleratorResponseImpl(StarGateAppState.SUCCESS, bufs);
  }

  private void setParams(int rowNum, int colNum) {
    params.setType(TYPE);
    params.setWriteOverTime(TIMEOUT);
    params.setReadOverTime(TIMEOUT);
    params.setInputType(FLOAT_TYPE);
    params.setOutputType(FLOAT_TYPE);
    params.setMinNclusters(MIN_CLUSTERS);
    params.setMaxNclusters(MAX_CLUSTERS);
    params.setThreshold(THRESHOLD);
    params.setLine(rowNum);
    params.setRow(colNum);
    params.setAccelerateBitPath(BITSTREAM_PATH);
  }

  public static class KMeansRR implements RecordReader {

    private Map<Long, Long> row2Bytes = new LinkedHashMap<>();
    private long size;

    @Override
    public ByteBuffer readBytes(String path) throws StarGateException {
      ByteBuffer sndBuf;
      try {
        File file = new File(path);
        BufferedReader br = new BufferedReader(new FileReader(file));
        int lineNum = 0;
        while ((br.readLine()) != null) {
          lineNum++;
        }

        br = new BufferedReader(new FileReader(file));
        String dataPoints;
        String[] dataPointVector;
        int dataPointNum = 0;
        float[][] dataPointMatrix = new float[lineNum][];
        lineNum = 0;
        while ((dataPoints = br.readLine()) != null) {
          dataPointVector = dataPoints.split(" ");
          dataPointNum = dataPointVector.length;
          dataPointMatrix[lineNum] = new float[dataPointNum];
          for (int i = 0; i < dataPointNum; i++) {
            dataPointMatrix[lineNum][i] = Float.parseFloat(dataPointVector[i]);
          }
          lineNum++;
          row2Bytes.put((long) lineNum, (long) (dataPointNum * 4));
        }

        size = lineNum * dataPointNum * 4;
        sndBuf = ByteBuffer.allocate((int) size);
        FloatBuffer floatBuf =
            sndBuf.order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (int i = 0; i < lineNum; i++) {
          for (int j = 0; j < dataPointNum; j++) {
            floatBuf.put(dataPointMatrix[i][j]);
          }
        }
      } catch (FileNotFoundException e) {
        throw new StarGateException(e);
      } catch (IOException e) {
        throw new StarGateException(e);
      }

      return sndBuf;
    }

    @Override
    public Map<Long, Long> getPos() {
      return row2Bytes;
    }

    @Override
    public long getSize() {
      return size;
    }
  }
}
