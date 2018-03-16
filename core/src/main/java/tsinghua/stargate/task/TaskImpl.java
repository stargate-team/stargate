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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.*;

import javax.annotation.Nonnull;

import com.google.common.base.Splitter;

import tsinghua.stargate.DaemonContext;
import tsinghua.stargate.Log;
import tsinghua.stargate.api.*;
import tsinghua.stargate.api.factory.provider.AcceleratorClientFactoryProvider;
import tsinghua.stargate.api.impl.AcceleratorRequestImpl;
import tsinghua.stargate.app.DaemonAppEvent;
import tsinghua.stargate.app.DaemonAppEventType;
import tsinghua.stargate.app.DaemonAppFailedTaskEvent;
import tsinghua.stargate.classloader.MutableURLClassLoader;
import tsinghua.stargate.conf.NameSpace;
import tsinghua.stargate.event.EventHandler;
import tsinghua.stargate.exception.StarGateException;
import tsinghua.stargate.exception.StarGateRuntimeException;
import tsinghua.stargate.rpc.message.entity.AcceleratorResource;
import tsinghua.stargate.rpc.message.entity.ApplicationId;
import tsinghua.stargate.rpc.message.entity.ServiceData;
import tsinghua.stargate.rpc.message.entity.Worker;
import tsinghua.stargate.scheduler.TaskRemovedSchedulerEvent;
import tsinghua.stargate.storage.*;
import tsinghua.stargate.task.launcher.TaskLauncherEvent;
import tsinghua.stargate.util.PathUtils;
import tsinghua.stargate.util.ReflectionUtils;

/**
 * A callable entity for completing a specific task.
 *
 * <p>
 * This entity posts ready operators and operands for execution on accelerators
 * via the delegate of native dynamic library.
 */
@SuppressWarnings("unchecked")
public class TaskImpl extends Log implements Task {

  private TaskId taskId;

  private DaemonContext context;

  private Worker worker;
  private String workload;
  private int allocatedCardId = -1;
  private int allocatedCoreId = -1;
  private AcceleratorResource allocatedResource;

  private BlockData blockData;

  private Dependencies dependencies;

  private Processor processor;

  private final StringBuilder diagnostics;

  private TaskState state;

  TaskImpl(DaemonContext context, String userAppId, ApplicationId daemonAppId,
      Worker worker, String workload, Integer index) {
    this.taskId = new TaskId(userAppId, daemonAppId, workload, index);
    this.context = context;
    this.worker = worker;
    this.workload = workload;
    this.diagnostics = new StringBuilder();
  }

  @Override
  public void createBlockData(ServiceData inSD, ServiceData outSD)
      throws StarGateException {
    blockData = new BlockData(inSD, outSD);
  }

  @Override
  public void createDependencies(Map<String, String> launchResources,
      Map<String, String> launchEnvironments, Map<String, String> processors) {
    dependencies =
        new Dependencies(launchResources, launchEnvironments, processors);
  }

  @Override
  public void createProcessor(Worker worker, int cardId, int coreId,
      StarGateApp sga, RecordReader recordReader) {
    processor = new Processor(worker, cardId, coreId, sga, recordReader);
  }

  @Override
  public String getDiagnostics() {
    return diagnostics.toString();
  }

  BlockData getBlockData() {
    return blockData;
  }

  Dependencies getDependencies() {
    return dependencies;
  }

  Processor getProcessor() {
    return processor;
  }

  @Override
  public TaskId getTaskId() {
    return taskId;
  }

  @Override
  public String getWorkload() {
    return workload;
  }

  @Override
  public TaskState run() {
    info(
        "Start running task {} (userAppId: {}, daemonAppId: {}) on "
            + "accelerator {} (type: {}, core: {})",
        taskId.getId(), taskId.getUserAppId(), taskId.getDaemonAppId(),
        allocatedCardId, Worker.FPGA, allocatedCoreId);

    Map<String, ByteBuffer> dataBlocks = null;
    try {
      if (null != blockData.getDataBlock()) {
        dataBlocks = blockData.getDataBlock().getBlocks();
      }
      createProcessor(worker, allocatedCardId, allocatedCoreId,
          dependencies.getSga(), dependencies.getRecordReader());
      processor.addDataBlocks(dataBlocks)
          .addExtras(dependencies.getAttachedFiles()).process();
      state = TaskState.EXITED_WITH_SUCCESS;
    } catch (StarGateException e) {
      error("StarGateApp of task {} is null", taskId.getId());
      state = TaskState.EXITED_WITH_FAILURE;
    } catch (IOException e) {
      error("Failed to process data", e);
      state = TaskState.EXITED_WITH_FAILURE;
    }

    return state;
  }

  public void setState(TaskState state) {
    this.state = state;
  }

  // -- TaskEvent handler machinery --

  public void handle(TaskEvent event) {
    String taskIdStr = taskId.getId();
    String userAppId = taskId.getUserAppId();
    ApplicationId daemonAppId = taskId.getDaemonAppId();

    switch (event.getType()) {
    case START:
      if (!(event instanceof TaskStartedEvent)) {
        throw new StarGateRuntimeException("Unexpected event: " + event);
      }
      break;

    case ADDED:
      if (!(event instanceof TaskAddedEvent)) {
        throw new StarGateRuntimeException("Unexpected event: " + event);
      }
      info("Accepting added task {} (userAppId: {}, daemonAppId: {})",
          taskIdStr, userAppId, daemonAppId);
      acceptApp(daemonAppId);
      break;

    case ALLOCATED:
      if (!(event instanceof TaskAllocatedEvent)) {
        throw new StarGateRuntimeException("Unexpected event: " + event);
      }
      info(
          "Downloading resources for allocated task {} "
              + "(userAppId: {}, anmAppId: {})",
          taskIdStr, userAppId, daemonAppId);
      state = TaskState.LOCALIZING;
      downloadResources((TaskAllocatedEvent) event);
      break;

    case LOCALIZED:
      if (!(event instanceof TaskLocalizedEvent)) {
        throw new StarGateRuntimeException("Unexpected event: " + event);
      }
      info("Launching localized task {} (userAppId: {}, daemonAppId: {})",
          taskIdStr, userAppId, daemonAppId);
      boolean success = ((TaskLocalizedEvent) (event)).isLocalState();
      Block dataBlock = ((TaskLocalizedEvent) (event)).getBlock();
      state = TaskState.LOCALIZED;
      launchTask(daemonAppId, dataBlock, success);
      break;

    case FINISH:
      if (!(event instanceof TaskFinishedEvent)) {
        throw new StarGateRuntimeException("Unexpected event: " + event);
      }
      info("Finishing task {} (userAppId: {}, daemonAppId: {})", taskIdStr,
          userAppId, daemonAppId);
      finishTask(daemonAppId);
      break;

    case FAILURE:
      info("{} run (userAppId: {}, daemonAppId: {}) failure", taskIdStr,
          userAppId, daemonAppId);
      if (!(event instanceof TaskFailedEvent)) {
        throw new StarGateRuntimeException("Unexpected event: " + event);
      }
      failedTask(daemonAppId);
      break;

    default:
      error("Unknown event arrived at TaskImpl: " + event.toString());
      break;
    }
  }

  private void addDiagnostics(String... diags) {
    for (String s : diags) {
      this.diagnostics.append(s);
    }
  }

  private void acceptApp(ApplicationId anmAppId) {
    context.getDispatcher().getEventHandler()
        .handle(new DaemonAppEvent(anmAppId, DaemonAppEventType.TASK_ACCEPTED));
  }

  private void failedTask(ApplicationId anmAppId) {
    context.getDispatcher().getEventHandler()
        .handle(new DaemonAppFailedTaskEvent(anmAppId, getDiagnostics()));
  }

  private void downloadResources(TaskAllocatedEvent allocatedEvent) {
    try {
      dependencies.resolve();
    } catch (StarGateException e) {
      String message = "Failed to resolve dependencies";
      addDiagnostics(message);
      error(message);
      DaemonAppEvent event =
          new DaemonAppEvent(allocatedEvent.getTaskId().getDaemonAppId(),
              DaemonAppEventType.TASK_FAILED);
      state = TaskState.LOCALIZATION_FAILED;
      context.getDispatcher().getEventHandler().handle(event);
    }

    this.allocatedResource = allocatedEvent.getResource();
    this.allocatedCardId = allocatedEvent.getCardId();
    this.allocatedCoreId = allocatedEvent.getCoreId();

    BlockInfo info = blockData.getDataBlockInfo();

    if (null == info) {
      // Run task directly without fetching and storing data
      ApplicationId daemonAppId = allocatedEvent.getTaskId().getDaemonAppId();
      EventHandler handler = context.getDispatcher().getEventHandler();
      handler.handle(new TaskLauncherEvent(this));
      handler.handle(
          new DaemonAppEvent(daemonAppId, DaemonAppEventType.TASK_RUNNING));
    } else {
      // Fetching data
      BlockFetchEvent event =
          new BlockFetchEvent(info, BlockFetchEventType.DATA);
      context.getDispatcher().getEventHandler().handle(event);
    }
  }

  private void launchTask(ApplicationId anmAppId, Block dataBlock,
      boolean success) {
    blockData.setDataBlock(dataBlock);
    if (success) {
      EventHandler handler = context.getDispatcher().getEventHandler();
      handler.handle(new TaskLauncherEvent(this));
      handler.handle(
          new DaemonAppEvent(anmAppId, DaemonAppEventType.TASK_RUNNING));
    } else {
      error("Failed to launch task");
    }
  }

  private void finishTask(ApplicationId anmAppId) {
    EventHandler handler = context.getDispatcher().getEventHandler();
    String userAppId = taskId.getUserAppId();
    String id = taskId.getId();
    TaskRemovedSchedulerEvent event =
        new TaskRemovedSchedulerEvent(userAppId, id);
    handler.handle(event);
    handler
        .handle(new DaemonAppEvent(anmAppId, DaemonAppEventType.TASK_FINISHED));
  }

  public class BlockData {

    /**
     * Block metadata specified by user.
     */
    private BlockInfo dataBlockInfo;
    /**
     * Block metadata specified by user.
     */
    private BlockInfo resultBlockInfo;
    private Block dataBlock;

    private BlockData(ServiceData inSD, ServiceData outSD)
        throws StarGateException {
      ApplicationId daemonAppId;
      String userAppId;
      String taskIdStr;
      String[] paths;
      BlockId blockId;
      boolean isCached;
      BlockStoreLevel storeLevel;

      if (null != inSD) {
        daemonAppId = taskId.getDaemonAppId();
        userAppId = taskId.getUserAppId();
        taskIdStr = taskId.getId();

        paths = listFiles(PathUtils.getPathFromURI(inSD.getStorePath()));
        blockId = new BlockId(daemonAppId, userAppId, taskIdStr, paths);
        isCached = inSD.isCached();

        storeLevel = BlockStoreLevel.valueOf(inSD.getStoreType().value());
        int capacity = (int) inSD.getCapacity();
        dataBlockInfo = new BlockInfo(blockId, isCached, storeLevel, capacity);
      }

      if (null != outSD) {
        daemonAppId = taskId.getDaemonAppId();
        userAppId = taskId.getId();
        taskIdStr = taskId.getId();
        paths = new String[] {
            setOutputPath(PathUtils.getPathFromURI(outSD.getStorePath())) };
        blockId = new BlockId(daemonAppId, userAppId, taskIdStr, paths);
        isCached = outSD.isCached();
        storeLevel = BlockStoreLevel.valueOf(outSD.getStoreType().value());
        resultBlockInfo = new BlockInfo(blockId, isCached, storeLevel, -1);
      }
    }

    private String[] listFiles(String path) {
      try {
        return PathUtils.listFiles(Paths.get(path)).toArray(new String[0]);
      } catch (Exception e) {
        String msg = "Failed to get the path of input service data";
        addDiagnostics(msg);
        error(msg, e);
      }
      return null;
    }

    String setOutputPath(String dir) {
      return dir + PathUtils.getOutputName(taskId.getDaemonAppId().toString());
    }

    BlockInfo getDataBlockInfo() {
      return dataBlockInfo;
    }

    BlockStoreLevel getDataBlockStoreLevel() {
      if (null != dataBlockInfo) {
        return dataBlockInfo.getStoreLevel();
      } else {
        return null;
      }
    }

    Block getDataBlock() {
      return dataBlock;
    }

    void setDataBlock(Block dataBlock) {
      this.dataBlock = dataBlock;
    }

    BlockInfo getResultBlockInfo() {
      return resultBlockInfo;
    }

    String getResultBlockPath() {
      return resultBlockInfo.getBlockId().getPaths()[0];
    }
  }

  public class Dependencies {

    private final Map<String, String> launchResources;
    private final Map<String, String> launchEnvironments;
    private final Map<String, String> processors;

    private BlockStoreLevel storeLevel;

    private RecordReader recordReader;
    private RecordWriter recordWriter;

    private StarGateApp sga;

    private ClassLoader classLoader;
    private ClassLoader contextClassLoader =
        Thread.currentThread().getContextClassLoader();

    private Map<String, ByteBuffer> attachedFiles;

    private Dependencies(Map<String, String> resources,
        Map<String, String> environments, Map<String, String> processors) {
      this.launchResources = resources;
      this.launchEnvironments = environments;
      this.processors = processors;
    }

    private void resolve() throws StarGateException {
      if (null == processors) {
        String msg = "Processor can not be null";
        addDiagnostics(msg);
        throw new StarGateException(msg);
      }
      getRecordReader();
      getRecordWriter();
      getSga();
      getAttachedFiles();
    }

    public RecordReader getRecordReader() {
      if (null != recordReader) {
        return recordReader;
      }
      String className = RecordReader.class.getSimpleName();
      String instanceName = processors.get(className);
      if (instanceName != null) {

        recordReader = (RecordReader) ReflectionUtils.get()
            .getInstance(instanceName, getClassLoader());
        getBlockManager().setRecordReader(recordReader, getStoreLevel());
      } else {
        return defaultRecordReader();
      }
      return recordReader;
    }

    private RecordReader defaultRecordReader() {
      if (storeLevel != null) {
        recordReader = getBlockManager().getRecordReader(storeLevel);
      }
      return recordReader;
    }

    private RecordWriter getRecordWriter() {
      if (null != recordWriter) {
        return recordWriter;
      }
      String className = RecordWriter.class.getSimpleName();
      String instanceName = processors.get(className);
      if (instanceName != null) {
        recordWriter = (RecordWriter) ReflectionUtils.get()
            .getInstance(instanceName, getClassLoader());
        getBlockManager().setRecordWriter(recordWriter, storeLevel);
      } else {
        return defaultRecordWriter();
      }
      return recordWriter;
    }

    private RecordWriter defaultRecordWriter() {
      if (storeLevel != null) {
        recordWriter = getBlockManager().getRecordWriter(storeLevel);
      }
      return recordWriter;
    }

    public StarGateApp getSga() throws StarGateException {
      if (null != sga) {
        return sga;
      }
      String className = StarGateApp.class.getSimpleName();
      String instanceName = processors.get(className);
      if (instanceName != null) {
        sga = (StarGateApp) ReflectionUtils.get().getInstance(instanceName,
            getClassLoader());
      } else {
        throw new StarGateException("StarGateApp can not be null");
      }
      return sga;
    }

    private ClassLoader getClassLoader() {
      if (null != classLoader) {
        return classLoader;
      }
      if (null == launchResources) {
        return defaultClassLoader();
      }
      String jars = launchResources.get(NameSpace.APP_RESOURCES_JAR);
      if (null == jars) {
        return defaultClassLoader();
      }

      List<URL> jarPaths = new ArrayList<>();
      Iterator<String> tmpIterator = Splitter.on(',').trimResults()
          .omitEmptyStrings().split(jars).iterator();
      while (tmpIterator.hasNext()) {
        try {
          jarPaths.add(new URL(tmpIterator.next()));
        } catch (MalformedURLException e) {
          return defaultClassLoader();
        }
      }

      URL[] jarUrls = new URL[jarPaths.size()];
      jarPaths.toArray(jarUrls);
      classLoader = new MutableURLClassLoader(jarUrls, contextClassLoader);
      return classLoader;
    }

    private ClassLoader defaultClassLoader() {
      classLoader = contextClassLoader;
      return classLoader;
    }

    public Map<String, ByteBuffer> getAttachedFiles() {
      if (null != attachedFiles) {
        return attachedFiles;
      }
      String attachedFilePaths =
          launchResources.get(NameSpace.APP_RESOURCES_FILE);
      if (null != attachedFilePaths) {

        attachedFiles = new HashMap<>();
        Iterator<String> tmpIterator = Splitter.on(',').trimResults()
            .omitEmptyStrings().split(attachedFilePaths).iterator();

        String file = "";
        while (tmpIterator.hasNext()) {
          try {
            file = tmpIterator.next();
            attachedFiles.put(file, getRecordReader().readBytes(file));
          } catch (StarGateException e) {
            String msg = "Failed to read attached file {}";
            addDiagnostics(msg);
            error(msg, file);
          }
        }
      }
      return attachedFiles;
    }

    public void setStoreLevel(BlockStoreLevel storeLevel) {
      this.storeLevel = storeLevel;
    }

    private BlockStoreLevel getStoreLevel() {
      if (this.storeLevel == null) {
        setStoreLevel(TaskImpl.this.blockData.getDataBlockStoreLevel());
      }
      return this.storeLevel;
    }
  }

  public class Processor implements Iterable<AcceleratorRequest> {

    private Worker workerType;
    private int cardId = -1;
    private int coreId = -1;

    private StarGateApp sga;
    private RecordReader recordReader;

    private AcceleratorHandler client;

    private Map<String, ByteBuffer> dataBlocks = new HashMap<>();
    private Map<String, ByteBuffer> extras = new HashMap<>();

    private ProcessorIterator procIter;

    private Processor(Worker workerType, int cardId, int coreId,
        StarGateApp sga, RecordReader recordReader) {
      this.workerType = workerType;
      this.coreId = coreId;
      this.cardId = cardId;
      this.sga = sga;
      this.recordReader = recordReader;
    }

    public Processor addDataBlocks(Map<String, ByteBuffer> dataBlocks) {
      if (dataBlocks != null) {
        this.dataBlocks.putAll(dataBlocks);
      }
      return this;
    }

    public Processor addExtras(Map<String, ByteBuffer> extras) {
      if (extras != null) {
        this.extras.putAll(extras);
      }
      return this;
    }

    @Override
    @Nonnull
    public Iterator iterator() {
      return new ProcessorIterator();
    }

    private void process() throws IOException, StarGateException {
      setup();
      while (procIter.hasNext()) {
        setTaskOutput(new TaskStarGateApp(sga).accelerate(procIter.next()));
      }
    }

    private void setup() throws IOException {
      procIter = (ProcessorIterator) iterator();
      if (dataBlocks.size() == 0) {
        procIter.setEmpty(true);
      } else {
        procIter.put(extras);
      }
      // Uncomment for Riffa
//      client = createAcceleratorClient(Worker.valueOf(workerType.getValue()),
//          cardId, coreId);
    }

    private AcceleratorHandler createAcceleratorClient(Worker type, int cardId,
        int coreId) throws IOException {
      return AcceleratorClientFactoryProvider.getClientFactory(type)
          .getClient(cardId, coreId);
    }

    public class ProcessorIterator implements Iterator<AcceleratorRequest> {

      private Iterator<Map.Entry<String, ByteBuffer>> blockIterator;
      private Map<String, ByteBuffer> dataContainer = new LinkedHashMap<>();
      private boolean empty = false;
      String pendingRemovedBlock = null;

      private ProcessorIterator() {
        this.blockIterator = Processor.this.dataBlocks.entrySet().iterator();
      }

      private void setEmpty(boolean empty) {
        this.empty = empty;
      }

      private void put(Map<String, ByteBuffer> value) {
        this.dataContainer.putAll(value);
      }

      @Override
      public boolean hasNext() {
        if ((cardId == -1) && (coreId == -1)) {
          return false;
        }

        // Although there is no data in blocks, here must also return true since
        // apps may store their data into attached files.
        if (empty) {
          return true;
        }

        return blockIterator.hasNext();
      }

      @Override
      public AcceleratorRequest next() {
        if (empty) {
          empty = false;
          // Uncomment for Riffa
//          return new AcceleratorRequestImpl(Processor.this.client, null);

           return new AcceleratorRequestImpl(allocatedCardId, allocatedCoreId,
           null, recordReader);
        }

        if (null != pendingRemovedBlock) {
          dataContainer.remove(pendingRemovedBlock);
        }

        Map.Entry<String, ByteBuffer> entry = blockIterator.next();
        dataContainer.put(entry.getKey(), entry.getValue());
        pendingRemovedBlock = entry.getKey();

        info("Handling block {}", PathUtils.getFilename(entry.getKey()));

        // Uncomment for Riffa
//        return new AcceleratorRequestImpl(Processor.this.client, dataContainer);

         return new AcceleratorRequestImpl(allocatedCardId, allocatedCoreId,
         dataContainer, recordReader);
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Unsupported operation");
      }
    }

    public class TaskStarGateApp implements StarGateApp {

      private static final long serialVersionUID = -8503941683420100027L;

      private StarGateApp real;

      private TaskStarGateApp(StarGateApp sga) {
        this.real = sga;
      }

      @Override
      public AcceleratorResponse accelerate(AcceleratorRequest request)
          throws IOException {
        if (null != Processor.this.client) {
          // Uncomment for Riffa
//          Processor.this.client.reset(cardId, coreId);
        }
        return real.accelerate(request);
      }
    }
  }

  private void setTaskOutput(AcceleratorResponse response)
      throws StarGateException {
    StarGateAppState state = response.getHandlerState();
    if (state == StarGateAppState.FAIL) {
      info("Completes handling (state: {})", state);
      return;
    }
    info("Completes handling (state: {})", state);

    if (blockData.getResultBlockInfo() == null) {
      return;
    }

    Map<String, ByteBuffer> buffers = new HashMap<>();
    String outPath = blockData.getResultBlockPath();
    debug("create {} result output path:{}", taskId.getDaemonAppId(), outPath);
    ByteBuffer[] outBuffer =
        response.getOutputResult().toArray(new ByteBuffer[0]);

    for (int i = 0; i < outBuffer.length; i++) {
      buffers.put(outPath, outBuffer[i]);
    }

    BlockId resultId = blockData.getResultBlockInfo().getBlockId();
    BlockStoreLevel level = blockData.getResultBlockInfo().getStoreLevel();
    Block outPut = new Block(resultId, level, buffers);
    getBlockManager().putBlock(outPut);
  }

  private BlockManager getBlockManager() {
    return context.getBlockManagerService().getBlockManager();
  }
}