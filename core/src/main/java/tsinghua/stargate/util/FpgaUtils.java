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

package tsinghua.stargate.util;

import java.io.IOException;
import java.nio.channels.ByteChannel;

import tsinghua.stargate.io.Fpga.FpgaInfo;
import tsinghua.stargate.io.FpgaChannel;
import tsinghua.stargate.io.ModuleConfig;

/**
 * This class consists exclusively of static methods that operate on FPGAs.
 */
public final class FpgaUtils {

  private FpgaUtils() {
  }

  public static ByteChannel newByteChannel(int id, int core)
      throws IOException {
    return FpgaChannel.open(id, core);
  }

  public static ByteChannel newByteChannel(int id, int core,
      ModuleConfig config) throws IOException {
    return FpgaChannel.open(id, core, config);
  }

  public static void reset(int cardId) throws IOException {
    FpgaChannel.reset(cardId);
  }

  public static FpgaInfo getInfo() throws IOException {
    return FpgaChannel.getInfo();
  }
}
