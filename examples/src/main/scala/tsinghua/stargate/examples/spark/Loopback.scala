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

package tsinghua.stargate.examples.spark

import java.io.IOException

import org.apache.spark.{Logging, SparkConf, SparkContext}
import tsinghua.stargate.api.StarGateApp
import tsinghua.stargate.conf.StarGateConf
import tsinghua.stargate.examples.Loopback
import tsinghua.stargate.rpc.message.entity.{BlockStoreType, ServiceData, Worker}
import tsinghua.stargate.spark.AcceleratorIm._
import tsinghua.stargate.util._

object Loopback extends Logging {

  val PROJECT_ROOT: String = PathUtils.root()
  val RESULT_PATH: String = PROJECT_ROOT + "data" + PathUtils.SEPARATOR +
    "Loopback" + PathUtils.SEPARATOR + "result"

  def main(args: Array[String]) {
    val sparkConf = new SparkConf().setAppName("Loopback")

    val sc = new SparkContext(sparkConf)

    val listRDD = sc.parallelize(
      List("data_point_1", "data_point_2"), 1
    )
    val tmpRDD = listRDD.map(x ⇒ (x, "tmp"))

    val sgc = new StarGateConf()
      .setWorker(Worker.FPGA)
      .setWorkload("loopback")
      .setProcessor(getProcessor())
      .setOutSD(getOutSD())
      .setResource(Utils.getExampleJar)

    // Create an $AcceleratorRDD implicitly
    val acceleratorRDD = tmpRDD.stargate(sgc)
    Utils.printPurple(acceleratorRDD.collect())

    sc.stop
  }

  def getProcessor(): java.util.HashMap[String, String] = {
    val processor: java.util.HashMap[String, String] =
      new java.util.HashMap[String, String]
    processor.put(
      classOf[StarGateApp].getSimpleName,
      classOf[Loopback].getCanonicalName
    )
    processor
  }

  @throws[IOException]
  private def getOutSD(): ServiceData =
    ServiceData.newInstance(RESULT_PATH)
      .setStoreType(BlockStoreType.DISK)
      .setCached(true)
}
