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
import tsinghua.stargate.api.{RecordReader, StarGateApp}
import tsinghua.stargate.conf.StarGateConf
import tsinghua.stargate.examples.KMeans
import tsinghua.stargate.examples.KMeans.KMeansRR
import tsinghua.stargate.rpc.message.entity.{BlockStoreType, ServiceData, Worker}
import tsinghua.stargate.spark.AcceleratorIm._
import tsinghua.stargate.util._

object KMeans extends Logging {

  val PROJECT_ROOT: String = PathUtils.root
  val APP_DATA_PATH: String =
    PROJECT_ROOT + "data" + PathUtils.SEPARATOR + "KMeans"
  val SOURCE_PATH: String = APP_DATA_PATH + PathUtils.SEPARATOR + "source"
  val RESULT_PATH: String = APP_DATA_PATH + PathUtils.SEPARATOR + "result"

  def main(args: Array[String]) {
    val sparkConf = new SparkConf().setAppName("KMeans")

    val sc = new SparkContext(sparkConf)

    // Read all features from $FEATURE_PATH into $featureRDD
    val featureRDD = sc.wholeTextFiles(SOURCE_PATH, 1)

    val sgc = new StarGateConf()
      .setWorker(Worker.FPGA)
      .setWorkload("kmeans")
      .setProcessor(getProcessor())
      .setInSD(getInSD())
      .setOutSD(getOutSD())
      .setResource(Utils.getExampleJar)

    val listRDD = sc.parallelize(
      List("data_point_1", "data_point_2"), 1
    )
    val tmpRDD = listRDD.map(x ⇒ (x, "tmp"))
    // Create an $AcceleratorRDD implicitly
    val acceleratorRDD = tmpRDD.stargate(sgc)

    Utils.printPurple(acceleratorRDD.collect())

    sc.stop
  }

  def getProcessor(): java.util.HashMap[String, String] = {
    val processors = new java.util.HashMap[String, String]
    processors.put(
      classOf[StarGateApp].getSimpleName,
      classOf[KMeans].getCanonicalName
    )
    processors.put(
      classOf[RecordReader].getSimpleName,
      Utils.getClassName(classOf[KMeansRR])
    )
    processors
  }

  @throws[IOException]
  private def getInSD(): ServiceData = {
    ServiceData.newInstance(SOURCE_PATH)
      .setStoreType(BlockStoreType.DISK).setCached(true)
  }

  @throws[IOException]
  private def getOutSD(): ServiceData = {
    ServiceData.newInstance(RESULT_PATH)
      .setStoreType(BlockStoreType.DISK).setCached(true)
  }
}
