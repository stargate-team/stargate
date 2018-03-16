/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tsinghua.stargate.service;

import java.io.Serializable;

import tsinghua.stargate.annotation.InterfaceAudience.Public;
import tsinghua.stargate.annotation.InterfaceStability.Evolving;

/**
 * A serializable lifecycle event: the time a state transition occurred, and
 * what state was entered.
 */
@Public
@Evolving
public class LifecycleEvent implements Serializable {

  private static final long serialVersionUID = -8525349977943633510L;

  /** Local time in milliseconds when the event occurred. */
  public long time;

  /** New state. */
  public Service.STATE state;
}
