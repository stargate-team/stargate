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

package tsinghua.stargate.rpc.thrift;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;

import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.conf.NameSpace;
import tsinghua.stargate.exception.StarGateException;
import tsinghua.stargate.rpc.ApplicationStarGateProtocol;
import tsinghua.stargate.rpc.message.*;
import tsinghua.stargate.rpc.message.thrift.*;
import tsinghua.stargate.rpc.thrift.message.GetApplicationReportRequestThrift;
import tsinghua.stargate.rpc.thrift.message.GetNewApplicationRequestThrift;
import tsinghua.stargate.rpc.thrift.message.SubmitApplicationRequestThrift;
import tsinghua.stargate.rpc.workhorse.RpcManager;
import org.apache.thrift.TException;

public class ApplicationStarGateProtocolClientThriftImpl
    implements ApplicationStarGateProtocol, Closeable {

  private ApplicationStarGateProtocolThrift proxy;

  public ApplicationStarGateProtocolClientThriftImpl(long version,
      InetSocketAddress address, Configuration conf) throws IOException {
    RpcManager.setRpcEngine(conf, ApplicationStarGateProtocolThrift.class,
        ThriftRpcEngine.class);
    int rpcTimeout = conf.getInt(NameSpace.RPC_CLIENT_SOCKET_TIMEOUT,
        NameSpace.DEFAULT_CLIENT_RPC_SOCKET_TIMEOUT);
    proxy = RpcManager.getProxy(ApplicationStarGateProtocolThrift.class,
        version, address, conf, null, rpcTimeout);
  }

  @Override
  public void close() {
  }

  @Override
  public GetNewApplicationResponse getNewApplication(
      GetNewApplicationRequest request)
      throws StarGateException, IOException, TException {

    GetNewApplicationRequestThrift requestThrift =
        ((GetNewApplicationRequestThriftImpl) request).getThrift();
    return new GetNewApplicationResponseThriftImpl(
        proxy.getNewApplication(requestThrift));
  }

  @Override
  public SubmitApplicationResponse submitApplication(
      SubmitApplicationRequest request)
      throws StarGateException, IOException, TException {
    SubmitApplicationRequestThrift requestThrift =
        ((SubmitApplicationRequestThriftImpl) request).getThrift();
    return new SubmitApplicationResponseThriftImpl(
        proxy.submitApplication(requestThrift));
  }

  @Override
  public GetApplicationReportResponse getApplicationReport(
      GetApplicationReportRequest request)
      throws StarGateException, IOException, TException {
    GetApplicationReportRequestThrift requestThrift =
        ((GetApplicationReportRequestThriftImpl) request).getThrift();
    return new GetApplicationReportResponseThriftImpl(
        proxy.getApplicationReport(requestThrift));
  }
}
