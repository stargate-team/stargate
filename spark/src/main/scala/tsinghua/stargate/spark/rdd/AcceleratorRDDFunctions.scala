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

import org.apache.spark.Logging
import org.apache.spark.rdd.RDD
import tsinghua.stargate.conf.StarGateConf

import scala.reflect.ClassTag

/**
 * Package an original $RDD into an $AcceleratorRDD via $stargate() API.
 * Note: The whole procedure is implicit.
 *
 * @param rdd the original $RDD
 * @tparam K the key type of internal value in $RDD
 * @tparam V the value type of internal value in $RDD
 */
class AcceleratorRDDFunctions[K: ClassTag, V: ClassTag](rdd: RDD[(K, V)])
    extends Logging {

  /**
   * An API provided for wrapping an original $RDD into an $AcceleratorRDD.
   *
   * @param sgConf the configuration for a StarGate application
   * @tparam K the key type of computing results
   * @tparam V the value type of computing results
   * @return an $AcceleratorRDD instance
   */
  def stargate[K, V](sgConf: StarGateConf) = {
    sgConf.setAppName(rdd.context.applicationId)
    new AcceleratorRDD(rdd, sgConf)
  }
}
