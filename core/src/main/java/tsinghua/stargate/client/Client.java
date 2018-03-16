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

package tsinghua.stargate.client;

import java.io.IOException;

import org.apache.thrift.TException;

import tsinghua.stargate.client.impl.ClientImpl;
import tsinghua.stargate.exception.StarGateException;
import tsinghua.stargate.rpc.message.GetNewApplicationResponse;
import tsinghua.stargate.rpc.message.entity.ApplicationId;
import tsinghua.stargate.rpc.message.entity.ApplicationReport;
import tsinghua.stargate.rpc.message.entity.ApplicationState;
import tsinghua.stargate.rpc.message.entity.ApplicationSubmissionContext;
import tsinghua.stargate.service.AbstractService;

/** Abstraction to create a client. */
public abstract class Client extends AbstractService {

  protected Client(String name) {
    super(name);
  }

  /**
   * Create a new instance of {@link ClientImpl}.
   *
   * @return a new {@link ClientImpl} instance
   */
  public static Client create() {
    return new ClientImpl();
  }

  /**
   * Obtain a {@link Application app} standing for a new application, which
   * contains {@link ApplicationSubmissionContext ApplicationSubmissionContext}
   * and {@link GetNewApplicationResponse GetNewApplicationResponse}.
   *
   * @return {@code Application} built for a new application
   */
  public abstract Application createApplication()
      throws StarGateException, IOException, TException;

  public abstract ApplicationState submitApplication(
      ApplicationSubmissionContext appSubmissionContext)
      throws StarGateException, IOException, TException;

  /**
   * This interface is used by clients to retrieve a report of an application
   * from {@code StarGateDaemon ANM}.
   *
   * <p>
   * Client sends a request for application report via {@link ApplicationId
   * ApplicationId}.
   *
   * <p>
   * ANM responds with a {@link ApplicationReport ApplicationReport} which
   * includes the aggregate metadata information of the specified application.
   *
   * @return an application report
   */
  public abstract ApplicationReport getApplicationReport(ApplicationId appId)
      throws StarGateException, IOException, TException;
}
