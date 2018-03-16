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

package tsinghua.stargate.spark.rdd

import java.nio.ByteBuffer

import org.apache.spark.rdd.RDD
import org.apache.spark.{Partition, TaskContext}
import tsinghua.stargate.StarGateContext
import tsinghua.stargate.conf.StarGateConf
import tsinghua.stargate.rpc.message.entity.BlockStoreType
import tsinghua.stargate.util.{ArrayUtils, PathUtils}

import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

/**
 * A Spark RDD for communicating with StarGate.
 *
 * $AcceleratorRDD employs the service offered by `StarGateDaemon` to accelerate computing. Its
 * behavior is similar to Spark partition $RDD which performs computations on the whole partition
 * at a time. $AcceleratorRDD also manipulates a partition to reduce the communication overhead.
 *
 * $AcceleratorRDD is responsible for sending three kinds of data, i.e. opcodes (CSRs or
 * Configuration Status Registers), operands and necessary accelerator metadata, e.g. $type, $id,
 * $core and so on, to the `StarGateDaemon`. Then `StarGateDaemon` schedules this Spark task for
 * executing once there is an available accelerator and returns the computing results to Spark.
 *
 * @param prev the original Spark $RDD
 * @param sgConf the configuration for a StarGate application
 */
class AcceleratorRDD[K: ClassTag, V: ClassTag](var prev: RDD[(K, V)], sgConf: StarGateConf)
    extends RDD[String](prev) {

  /**
   * Persist the Spark partition using StarGate `BlockManager` and send the configuration of a
   * `StarGateApp` to `StarGateDaemon` via `StarGateContext`.
   *
   * @param split   a partition of this Spark $RDD
   * @param context a Spark task context
   * @return an iterator of accelerator computing result
   */
  override def compute(
    split: Partition,
    context: TaskContext
  ): Iterator[String] = {

    val blockManager = sgConf.getBlockManager

    def persist(dataIter: Iterator[(K, V)]): Unit = {
      dataIter.foreach(data ⇒ {
        val key = data._1.asInstanceOf[String]
        val value = data._2.asInstanceOf[String].getBytes()
        blockManager.getDiskStore
          .writeHashFile(key, ArrayUtils.bytes2Buffer(value))
      })
    }
    persist(firstParent[(K, V)].iterator(split, context))

    val sgc = new StarGateContext(sgConf)

    def outputResult(application: StarGateContext): Iterator[String] = {

      def getOutputDir(StorePath: String, Suffix: String): String = {
        PathUtils.getPathFromURI(StorePath) + PathUtils.SEPARATOR +
          PathUtils.getOutputName(Suffix)
      }

      var result: String = null
      var resultBuf: ByteBuffer = null
      var appStatus = true
      val storeType = sgConf.getOutSD.getStoreType

      val outputPaths = ArrayBuffer[String]()
      outputPaths += getOutputDir(
        sgConf.getOutSD.getStorePath, application.getAppId
      )
      val outPathIter = outputPaths.toIterator

      blockManager.getDiskStore.stop()

      new Iterator[String] {

        override def hasNext: Boolean = {
          result = null
          appStatus && outPathIter.hasNext
        }

        override def next(): String = {
          appStatus = application.isSuccess
          if (!appStatus) {
            result = "Failed to run task on accelerator"
          } else {
            val path = outPathIter.next
            storeType match {
              case BlockStoreType.DISK ⇒
                resultBuf = blockManager.getDiskStore.readBytes(path)
              case BlockStoreType.ALLUXIO ⇒
                resultBuf = blockManager.getAlluxioStore.readBytes(path)
              case _ ⇒
                result = "Storage type not supported";
            }
            if (result == null) {
              result = new String(ArrayUtils.buffer2Bytes(resultBuf))
            }
          }
          result
        }
      }
    }
    outputResult(sgc.waitForCompletion())
  }

  override def getPartitions: Array[Partition] = firstParent[(K, V)].partitions
}

