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

package tsinghua.stargate.conf;

public class NameSpace {

  ////////////////////////////////////////////////
  ////////////////////////////////////////////////
  /// Global Project Prefix
  ////////////////////////////////////////////////
  ////////////////////////////////////////////////
  private static final String STARGATE_PREFIX = "stargate.";

  ////////////////////////////////////////////////
  ////////////////////////////////////////////////
  /// Application Configurations
  ////////////////////////////////////////////////
  ////////////////////////////////////////////////
  private static final String APP_PREFIX = STARGATE_PREFIX + "app.";

  static final String APP_NAME = APP_PREFIX + "name";
  public static final String DEFAULT_APP_NAME = "default_app";

  static final String APP_RESOURCES = APP_PREFIX + "resources";
  public static final String APP_RESOURCES_JAR = APP_RESOURCES + ".jar";
  public static final String APP_RESOURCES_FILE = APP_RESOURCES + ".file";

  static final String APP_PROCESSORS = APP_PREFIX + "processors";

  static final String APP_SERVICEDATA_INPUT = APP_PREFIX + "servicedata.input";
  static final String APP_SERVICEDATA_OUTPUT =
      APP_PREFIX + "servicedata.output";

  ////////////////////////////////////////////////
  ////////////////////////////////////////////////
  /// RPC Configurations
  ////////////////////////////////////////////////
  ////////////////////////////////////////////////
  private static final String RPC_PREFIX = STARGATE_PREFIX + "rpc.";

  // -- Beginning of RPC Client --
  /** Factory to create RPC Client. */
  public static final String RPC_CLIENT_FACTORY = RPC_PREFIX + "client.factory";
  public static final String DEFAULT_RPC_CLIENT_FACTORY =
      "tsinghua.stargate.rpc.factory.impl.thrift.RpcClientFactoryThriftImpl";

  /** Socket timeout. */
  public static final String RPC_CLIENT_SOCKET_TIMEOUT =
      RPC_PREFIX + "client.socket.timeout";
  public static final int DEFAULT_CLIENT_RPC_SOCKET_TIMEOUT = 60000;

  /** Timeout for RPC client connection. */
  public static final String RPC_CLIENT_CONNECTION_TIMEOUT =
      RPC_PREFIX + "client.connection.timeout";
  public static final int DEFAULT_RPC_CLIENT_CONNECTION_TIMEOUT = 20000; // 20s

  /** The max. no. of connection idle time. */
  public static final String RPC_CLIENT_CONNECTION_MAXIDLETIME =
      RPC_PREFIX + "client.connection.maxidletime";
  public static final int DEFAULT_RPC_CLIENT_CONNECTION_MAXIDLETIME = 10000; // 10s

  /** Switch for Nagle's algorithm . */
  public static final String RPC_CLIENT_CONNECTION_TCPNODELAY =
      RPC_PREFIX + "client.connection.tcpnodelay";
  public static final boolean DEFAULT_RPC_CLIENT_CONNECTION_TCPNODELAY = true;

  /** Enable pings from RPC client to the server. */
  public static final String RPC_CLIENT_PING = RPC_PREFIX + "client.ping";
  public static final boolean DEFAULT_RPC_CLIENT_PING = true;

  /** How often does RPC client send pings to RPC server. */
  public static final String RPC_CLIENT_PING_INTERVAL =
      RPC_PREFIX + "client.ping.interval";
  public static final int DEFAULT_RPC_CLIENT_PING_INTERVAL = 60000; // 1 min
  // -- End of RPC Client --

  // -- Beginning of RPC Server --
  /** Socket address for connecting StarGateDaemon. */
  public static final String RPC_SERVER_ADDRESS = RPC_PREFIX + "server.address";
  public static final int DEFAULT_RPC_SERVER_PORT = 8888;
  public static final String DEFAULT_RPC_SERVER_ADDRESS =
      "0.0.0.0:" + DEFAULT_RPC_SERVER_PORT;

  /** Factory to create RPC Server. */
  public static final String RPC_SERVER_FACTORY = RPC_PREFIX + "server.factory";
  public static final String DEFAULT_RPC_SERVER_FACTORY =
      "tsinghua.stargate.rpc.factory.impl.thrift.RpcServerFactoryThriftImpl";

  /** Queue size for per IO thread. */
  public static final String RPC_SERVER_THREAD_IO_QUEUE_SIZE =
      RPC_PREFIX + "server.thread.io.queue.size";
  public static final int DEFAULT_RPC_SERVER_THREAD_IO_QUEUE_SIZE = 100;

  /** Number of IO threads. */
  public static final String RPC_SERVER_THREAD_IO_COUNT =
      RPC_PREFIX + "server.thread.io.count";
  public static final int DEFAULT_RPC_SERVER_THREAD_IO_COUNT = 2;

  /** Number of worker threads. */
  public static final String RPC_SERVER_THREAD_WORKER_COUNT =
      RPC_PREFIX + "server.thread.worker.count";
  public static final int DEFAULT_RPC_SERVER_THREAD_WORKER_COUNT = 50;
  // -- End of RPC Server --

  // -- Beginning of RPC Message --
  /** Factory to create serializable RPC messages. */
  public static final String RPC_MESSAGE_FACTORY =
      RPC_PREFIX + "message.factory";
  public static final String DEFAULT_RPC_MESSAGE_FACTORY =
      "tsinghua.stargate.rpc.factory.impl.thrift.MessageFactoryThriftImpl";
  // -- End of RPC Message --

  // -- Beginning of Apache Thrift RPC --
  /** Default RPC implementation. */
  public static final String RPC_IMPL = RPC_PREFIX + "impl";
  public static final String DEFAULT_RPC_IMPL =
      "tsinghua.stargate.rpc.thrift.ThriftRPC";

  /** Frame size. */
  public static final String RPC_THRIFT_FRAME_LENGTH_MAX =
      RPC_PREFIX + "thrift.frame.length.max";
  // 16 MB
  public static final int DEFAULT_RPC_THRIFT_FRAME_LENGTH_MAX = 16777216;
  // -- End of Apache Thrift RPC --

  ////////////////////////////////////////////////
  ////////////////////////////////////////////////
  /// Scheduler Configurations
  ////////////////////////////////////////////////
  ////////////////////////////////////////////////
  private static final String SCHEDULER_PREFIX = STARGATE_PREFIX + "scheduler.";

  /** How often does scheduler schedule. */
  public static final String SCHEDULING_INTERVAL =
      SCHEDULER_PREFIX + "interval";
  public static final int DEFAULT_SCHEDULING_INTERVAL = 1000;

  ////////////////////////////////////////////////
  ////////////////////////////////////////////////
  /// Storage Configurations
  ////////////////////////////////////////////////
  ////////////////////////////////////////////////
  private static final String STORAGE_PREFIX = STARGATE_PREFIX + "storage.";

  private static final String STORAGE_FACTORY_PREFIX =
      STORAGE_PREFIX + "factory.";

  /** Factory for persisting block on plain disk. */
  public static final String STORAGE_FACTORY_DISK =
      STORAGE_FACTORY_PREFIX + "disk";
  public static final String DEFAULT_STORAGE_FACTORY_DISK =
      "tsinghua.stargate.storage.factory.impl.DiskFactoryImpl";

  /** Factory for persisting block on memory. */
  public static final String STORAGE_FACTORY_MEMORY =
      STORAGE_FACTORY_PREFIX + "memory";
  public static final String DEFAULT_STORAGE_FACTORY_MEMORY =
      "tsinghua.stargate.storage.factory.impl.MemoryFactoryImpl";

  /** Factory for persisting block via Alluxio. */
  public static final String STORAGE_FACTORY_ALLUXIO =
      STORAGE_FACTORY_PREFIX + "alluxio";
  public static final String DEFAULT_STORAGE_FACTORY_ALLUXIO =
      "tsinghua.stargate.storage.factory.impl.AlluxioFactoryImpl";

  /** Number of threads for fetching blocks. */
  public static final String STORAGE_THREAD_COUNT =
      STORAGE_PREFIX + "thread.count";
  public static final int DEFAULT_STORAGE_THREAD_COUNT = 4;

  ////////////////////////////////////////////////
  ////////////////////////////////////////////////
  /// Task Configurations
  ////////////////////////////////////////////////
  ////////////////////////////////////////////////
  private static final String TASK_PREFIX = STARGATE_PREFIX + "task.";

  /** Number of threads for executing tasks. */
  public static final String TASK_THREAD_COUNT = TASK_PREFIX + "thread.count";
  public static final int DEFAULT_TASK_THREAD_COUNT = 20;

  ////////////////////////////////////////////////
  ////////////////////////////////////////////////
  /// Accelerator Configurations
  ////////////////////////////////////////////////
  ////////////////////////////////////////////////
  private static final String ACCELERATOR_PREFIX =
      STARGATE_PREFIX + "accelerator.";

  // -- Beginning of accelerator worker --
  /** Accelerator worker, e.g., CPU, GPU, FPGA, ASIC. */
  private static final String ACCELERATOR_WORKER_PREFIX =
      ACCELERATOR_PREFIX + "worker.";

  static final String ACCELERATOR_WORKER = ACCELERATOR_WORKER_PREFIX + "type";
  public static final String DEFAULT_ACCELERATOR_WORKER = "FPGA";

  static final String ACCELERATOR_WORKER_CAPACITY =
      ACCELERATOR_WORKER_PREFIX + "capacity";

  static final int DEFAULT_ACCELERATOR_WORKER_CAPACITY = -1;
  // -- End of accelerator worker --

  // -- Beginning of logical accelerator, i.e., workload --
  private static final String ACCELERATOR_WORKLOAD_PREFIX =
      ACCELERATOR_PREFIX + "workload.";

  static final String ACCELERATOR_WORKLOAD = ACCELERATOR_WORKLOAD_PREFIX + "id";

  public static final String ACCELERATOR_WORKLOAD_WEIGHT =
      ACCELERATOR_WORKLOAD_PREFIX + "weight";

  public static final float DEFAULT_ACCELERATOR_WORKLOAD_WEIGHT = 1.0f;
  // -- End of accelerator workload, i.e., workload --

  // -- Beginning of physical accelerator --
  /** Accelerator ids. */
  public static final String ACCELERATOR_IDS = ACCELERATOR_PREFIX + "ids";

  /** Accelerator type. */
  public static final String ACCELERATOR_TYPE_PREFIX =
      ACCELERATOR_PREFIX + "type.";

  /** Accelerator core. */
  private static final String ACCELERATOR_CORE_PREFIX =
      ACCELERATOR_PREFIX + "core.";

  public static final String ACCELERATOR_CORE_IDS_PREFIX =
      ACCELERATOR_CORE_PREFIX + "ids.";

  public static final String ACCELERATOR_CORE_WORKLOADS_PREFIX =
      ACCELERATOR_CORE_PREFIX + "workloads.";

  public static final String ACCELERATOR_CORE_MEMORY_PREFIX =
      ACCELERATOR_CORE_PREFIX + "memory-Mb.";
  public static final int DEFAULT_ACCELERATOR_CORE_MEMORY = -1;

  public static final String ACCELERATOR_CORE_FREQUENCY_PREFIX =
      ACCELERATOR_CORE_PREFIX + "frequency-Mb.";
  public static final int DEFAULT_ACCELERATOR_CORE_FREQUENCY = 200;
  // -- End of physical accelerator --
}
