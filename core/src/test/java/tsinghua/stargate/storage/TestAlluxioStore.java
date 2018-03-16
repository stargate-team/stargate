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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import tsinghua.stargate.exception.StarGateException;
import tsinghua.stargate.storage.impl.BlockStoreAlluxioImpl;
import tsinghua.stargate.util.ArrayUtils;

public class TestAlluxioStore {

  private BlockStoreAlluxioImpl store;

  @Before
  public void setUp() throws Exception {
    store = new BlockStoreAlluxioImpl();
  }

  private void testWrite() throws StarGateException {
    String s = "this is first starGate demo,dear me";
    store.writeBytes("/stargate/test.txt",
        ArrayUtils.bytes2Buffer(s.getBytes()));
  }

  @Test
  public void testExist() throws StarGateException {
    testWrite();
    assertTrue(store.exists("/stargate/test.txt"));
    testRead();
  }

  private void testRead() throws StarGateException {
    String s = new String(
        ArrayUtils.buffer2Bytes(store.readBytes("/stargate/test.txt")));
    System.out.println(s);
  }

  @After
  public void testDelete() throws StarGateException {
    store.delete("/stargate/test.txt");
  }
}
