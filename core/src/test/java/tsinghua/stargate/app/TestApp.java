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

package tsinghua.stargate.app;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import org.junit.Test;

import tsinghua.stargate.StarGateContext;
import tsinghua.stargate.api.StarGateApp;
import tsinghua.stargate.conf.StarGateConf;
import tsinghua.stargate.exception.StarGateException;
import tsinghua.stargate.rpc.message.entity.BlockStoreType;
import tsinghua.stargate.rpc.message.entity.ServiceData;
import tsinghua.stargate.rpc.message.entity.Worker;
import tsinghua.stargate.util.AppUtils;
import tsinghua.stargate.util.PathUtils;
import tsinghua.stargate.util.ReflectionUtils;

public class TestApp {

  private static final int CONCURRENT_NUM = 1;

  @Test
  public void testSubmitApp() throws InterruptedException {
    ExecutorService requestDispatcher =
        newDaemonCachedThreadPool("Loopback launch executor", 20, 60);

    CountDownLatch latch = new CountDownLatch(CONCURRENT_NUM);

    for (int id = 0; id < CONCURRENT_NUM; id++) {
      requestDispatcher.execute(new AppThread(latch));
    }
    latch.await();
    requestDispatcher.shutdown();
  }

  /**
   * Create a cached thread pool whose max number of threads is
   * `maxThreadNumber`. Thread names are formatted as prefix-ID, where ID is a
   * unique, sequentially assigned integer.
   *
   * @param prefix the prefix of a thread name
   * @param maxThreadNumber the max number of threads to create before queuing
   *          the tasks
   * @param keepAliveSeconds the keep alive time of threads
   * @return a configured {@link ThreadPoolExecutor} instance
   */
  private static ThreadPoolExecutor newDaemonCachedThreadPool(String prefix,
      int maxThreadNumber, int keepAliveSeconds) {
    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
        maxThreadNumber, maxThreadNumber, (long) keepAliveSeconds,
        TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());
    threadPoolExecutor.allowCoreThreadTimeOut(true);
    return threadPoolExecutor;
  }

  static StarGateContext createSGC() {
    StarGateConf sgConf =
        new StarGateConf().setAppName("LoopbackTest").setWorker(Worker.FPGA)
            .setWorkload("loopback").setProcessor(getProcessors())
            .setOutSD(getOutServiceData()).setResource(getResources());

    StarGateContext sgc = new StarGateContext(sgConf);
    return sgc;
  }

  private static String getBaseDir() {
    String basePath =
        System.getenv("STARGATE_HOME").replace("\\", PathUtils.SEPARATOR);
    return Paths.get(basePath).toUri().toString();
  }

  private static ServiceData getOutServiceData() {
    String resultPath = getBaseDir() + "tmp";
    ServiceData serviceData = ReflectionUtils.get().getMsg(ServiceData.class);
    serviceData.setStoreType(BlockStoreType.DISK);
    serviceData.setStorePath(resultPath);
    return serviceData;
  }

  private static Map<String, String> getResources() {
    Map<String, String> resources = new HashMap<>();
    Map<String, String> jars = new HashMap<>();
    String userJarPath = getBaseDir() + "share" + PathUtils.SEPARATOR
        + "stargate-examples-0.1.0.jar";
    try {
      AppUtils.addJar(jars, userJarPath);
    } catch (StarGateException e) {
      e.printStackTrace();
    }
    resources.putAll(jars);
    return resources;
  }

  private static Map<String, String> getProcessors() {
    Map<String, String> processors = new HashMap<>();
    processors.put(StarGateApp.class.getSimpleName(),
        "tsinghua.stargate.examples.loopback.SleepStarGateApp");
    return processors;
  }
}

class AppThread implements Runnable {

  private CountDownLatch latch;

  AppThread(CountDownLatch latch) {
    this.latch = latch;
  }

  @Override
  public void run() {
    StarGateContext app = TestApp.createSGC();

    try {
      app.waitForCompletion();
    } catch (StarGateException e) {
      e.printStackTrace();
    }

    latch.countDown();
  }
}
