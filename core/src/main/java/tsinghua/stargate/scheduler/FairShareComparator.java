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

package tsinghua.stargate.scheduler;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compare {@link Schedulable} via weighted fair sharing. In addition,
 * Schedulables below their min share get priority over those whose min share is
 * met.
 *
 * <p>
 * Schedulables below their min share are compared by how far below it they are
 * as a ratio. For example, if job A has 8 out of a min share of 10
 * scheduleTasks and job B has 50 out of a min share of 100, then job B is
 * scheduled next, because B is at 50% of its min share and A is at 80% of its
 * min share.
 *
 * <p>
 * Schedulables above their min share are compared by (runningTasks / weight).
 * If all weights are equal, slots are given to the job with the fewest
 * scheduleTasks; otherwise, jobs with more weight get proportionally more
 * slots.
 *
 * <p>
 * Scheduling in descending order
 */
public class FairShareComparator
    implements Comparator<Schedulable>, Serializable {

  private static final long serialVersionUID = 5564969375856699313L;

  private static final long WAIT_MAX_TIME = Long.MAX_VALUE;

  /**
   * Check whether need to reprogram FPGA or not.
   *
   * <p>
   * If there is no FPGA service in running state matching the requested
   * service, then StarGate has to reprogram one of the selected FPGAs through
   * burning a specified bitstream.
   * 
   * @param requestResources the resources requested by user
   * @param systemResources the resources provided by StarGate
   * @return {@code true} if need to reprogram FPGA, {@code false} otherwise
   */
  private boolean checkIfReprogram(AcceleratorResources requestResources,
      AcceleratorResources systemResources) {
    if (requestResources.getNum() == 0) {
      return false;
    }
    return systemResources.contain(requestResources);
  }

  @Override
  public int compare(Schedulable s1, Schedulable s2) {
    // Check whether the accelerator task wait too long or not
    boolean s1WaitTooLong = s1.getSubmissionDuration() > WAIT_MAX_TIME;
    boolean s2WaitTooLong = s2.getSubmissionDuration() > WAIT_MAX_TIME;

    // Check whether reprogram FPGA or not
    boolean s1Reprogram =
        checkIfReprogram(s1.askResources(), s1.offerMaxShare());
    boolean s2Reprogram =
        checkIfReprogram(s2.askResources(), s2.offerMaxShare());

    // Comparing accelerator workload weight
    float useToWeightRatio1, useToWeightRatio2;
    useToWeightRatio1 = s1.askResources().getNum() / s1.getWeight();
    useToWeightRatio2 = s2.askResources().getNum() / s2.getWeight();

    int res = 0;
    if (s1WaitTooLong && !s2WaitTooLong)
      res = -1;
    else if (!s1WaitTooLong && s2WaitTooLong)
      res = 1;
    if (res != 0)
      return res;

    if (!s1Reprogram && s2Reprogram)
      res = -1;
    else if (s1Reprogram && !s2Reprogram)
      res = 1;
    if (res != 0)
      return res;

    res = (int) Math.signum(useToWeightRatio1 - useToWeightRatio2);
    if (res != 0)
      return res;

    res = (int) Math.signum(s1.getStartTime() - s2.getStartTime());
    if (res != 0)
      return res;

    res = s1.getName().compareTo(s2.getName());
    return res;
  }
}
