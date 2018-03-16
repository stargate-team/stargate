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

package tsinghua.stargate.storage;

import static org.junit.Assert.assertTrue;

import tsinghua.stargate.conf.Configuration;
import tsinghua.stargate.conf.StarGateConf;
import tsinghua.stargate.exception.StarGateException;
import tsinghua.stargate.storage.impl.BlockStoreDiskImpl;
import tsinghua.stargate.util.ArrayUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestDiskStore {

  private BlockStoreDiskImpl diskStore;
  private Configuration conf;

  @Before
  public void setUp() throws Exception {
    conf = new StarGateConf();
    diskStore = new BlockStoreDiskImpl(conf);
  }

  private void testWrite() throws StarGateException {
    String s = "this is first starGate demo,dear me";
    diskStore.writeBytes("D:\\tmp\\tmp.txt",
        ArrayUtils.bytes2Buffer(s.getBytes()));
  }

  @Test
  public void testExist() throws StarGateException {
    testWrite();
    assertTrue(diskStore.exists("D:\\tmp\\tmp.txt"));
    testRead();
  }

  private void testRead() throws StarGateException {
    String s = new String(
        ArrayUtils.buffer2Bytes(diskStore.readBytes("D:\\tmp\\tmp.txt")));
    System.out.println(s);
  }

  @After
  public void testDelete() throws StarGateException {
    diskStore.delete("D:\\tmp\\tmp.txt");
  }
}
