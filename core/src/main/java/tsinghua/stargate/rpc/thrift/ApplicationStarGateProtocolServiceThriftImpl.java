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

import java.io.IOException;

import org.apache.thrift.TException;

import tsinghua.stargate.exception.StarGateException;
import tsinghua.stargate.rpc.ApplicationStarGateProtocol;
import tsinghua.stargate.rpc.message.*;
import tsinghua.stargate.rpc.message.thrift.*;
import tsinghua.stargate.rpc.thrift.message.*;

public class ApplicationStarGateProtocolServiceThriftImpl
    implements ApplicationStarGateProtocolThrift {

  ApplicationStarGateProtocol real;

  public ApplicationStarGateProtocolServiceThriftImpl(
      ApplicationStarGateProtocol real) {
    this.real = real;
  }

  @Override
  public GetNewApplicationResponseThrift getNewApplication(
      GetNewApplicationRequestThrift request) throws TException {
    GetNewApplicationRequest getNewApplicationRequest =
        new GetNewApplicationRequestThriftImpl(request);
    try {
      GetNewApplicationResponse response =
          real.getNewApplication(getNewApplicationRequest);
      return ((GetNewApplicationResponseThriftImpl) response).getThrift();
    } catch (StarGateException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public SubmitApplicationResponseThrift submitApplication(
      SubmitApplicationRequestThrift request) throws TException {
    SubmitApplicationRequest submitApplicationRequest =
        new SubmitApplicationRequestThriftImpl(request);
    try {
      SubmitApplicationResponse response =
          real.submitApplication(submitApplicationRequest);
      return ((SubmitApplicationResponseThriftImpl) response).getThrift();
    } catch (StarGateException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public GetApplicationReportResponseThrift getApplicationReport(
      GetApplicationReportRequestThrift request) throws TException {
    GetApplicationReportRequest getApplicationReportRequest =
        new GetApplicationReportRequestThriftImpl(request);
    try {
      GetApplicationReportResponse response =
          real.getApplicationReport(getApplicationReportRequest);
      return ((GetApplicationReportResponseThriftImpl) response).getThrift();
    } catch (StarGateException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
