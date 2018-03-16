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

package tsinghua.stargate.task;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import tsinghua.stargate.DaemonContext;
import tsinghua.stargate.DaemonContextImpl;
import tsinghua.stargate.api.RecordReader;
import tsinghua.stargate.api.StarGateApp;
import tsinghua.stargate.conf.StarGateConf;
import tsinghua.stargate.exception.StarGateException;
import tsinghua.stargate.rpc.message.entity.ApplicationId;
import tsinghua.stargate.rpc.message.entity.BlockStoreType;
import tsinghua.stargate.rpc.message.entity.ServiceData;
import tsinghua.stargate.rpc.message.entity.Worker;
import tsinghua.stargate.storage.BlockManagerService;
import tsinghua.stargate.storage.BlockStoreLevel;
import tsinghua.stargate.util.AppUtils;
import tsinghua.stargate.util.PathUtils;
import tsinghua.stargate.util.ReflectionUtils;

public class TestTask {

  private TaskImpl task;
  private StarGateApp sga;

  private Map<String, ByteBuffer> weights;

  @Before
  public void setUp() throws Exception {
    DaemonContext context = mock(DaemonContextImpl.class);

    BlockManagerService blockManagerService = new BlockManagerService(null);
    blockManagerService.serviceInit(new StarGateConf());
    when(context.getBlockManagerService()).thenReturn(blockManagerService);

    ApplicationId appId = ApplicationId.newInstance(5451, 3);
    task = new TaskImpl(context, "default", appId, Worker.FPGA, "Kmeans", 0);
  }

  @Test
  public void testCreateBlockData() {
    ServiceData sd1 = ReflectionUtils.get().getMsg(ServiceData.class);
    sd1.setStoreType(BlockStoreType.DISK);
    sd1.setStorePath(Paths.get("D:\\tmp\\utmp").toUri().toString());

    ServiceData sd2 = ReflectionUtils.get().getMsg(ServiceData.class);
    sd2.setStoreType(BlockStoreType.DISK);
    sd2.setStorePath(Paths.get("D:\\tmp\\utmp2").toUri().toString());
    try {
      task.createBlockData(sd1, sd2);
    } catch (StarGateException e) {
      e.printStackTrace();
    }
    assertNotNull(task.getBlockData().getDataBlockInfo());
    assertNotNull(task.getBlockData().getResultBlockInfo());
  }

  private String getBaseDir() {
    String basePath =
        System.getenv("STARGATE_HOME").replace("\\", PathUtils.SEPARATOR);
    return Paths.get(basePath).toUri().toString();
  }

  private Map<String, String> addProcessors() {
    Map<String, String> processors = new HashMap<>();
    processors.put(StarGateApp.class.getSimpleName(),
        "tsinghua.stargate.thinker.ImageCaptionHandler");
    processors.put(RecordReader.class.getSimpleName(),
        "tsinghua.stargate.thinker.ImageCaptionRecordReader");
    return processors;
  }

  private void testDependencies() throws StarGateException {
    Map<String, String> resources = new HashMap<>();
    Map<String, String> jars = new HashMap<>();
    Map<String, String> files = new HashMap<>();
    String userJarPath = getBaseDir() + "share" + PathUtils.SEPARATOR
        + "stargate-examples-0.1.0.jar";
    String weightPath = getBaseDir() + "data" + PathUtils.SEPARATOR + "thinker"
        + PathUtils.SEPARATOR + "weight";

    try {
      AppUtils.addJar(jars, userJarPath);
      resources.putAll(jars);
      DirectoryStream<Path> paths = Files
          .newDirectoryStream(Paths.get(PathUtils.getPathFromURI(weightPath)));
      StringBuilder pathBuilder = new StringBuilder();
      for (Path path : paths) {
        pathBuilder.append(path.toString()).append(",");
      }
      pathBuilder.deleteCharAt(pathBuilder.length() - 1);

      AppUtils.addFile(files, pathBuilder.toString());
      resources.putAll(files);
    } catch (StarGateException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    task.createDependencies(resources, null, addProcessors());
    sga = task.getDependencies().getSga();
    assertTrue(sga.getClass().getCanonicalName()
        .equals("tsinghua.stargate.thinker.ImageCaptionHandler"));

    task.getDependencies().setStoreLevel(BlockStoreLevel.DISK);
    weights = task.getDependencies().getAttachedFiles();
    assertNotNull(weights);
  }

  @Test
  public void testProcessor() throws StarGateException {
    testDependencies();
    task.createProcessor(Worker.FPGA, 1, 2, sga, null);
    task.getProcessor().addExtras(weights);
    assertNotNull(task.getProcessor().iterator().hasNext());
  }
}
