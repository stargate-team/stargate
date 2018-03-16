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

package tsinghua.stargate.io;

public class RiffaConfig extends ModuleConfig {

  public int writeOverTime;
  public int readOverTime;
  public int writeSize;
  public int readSize;

  public int destoff;
  public int last;

  public void setWriteOverTime(int time) {
    this.writeOverTime = time;
  }

  public int getWriteOverTime() {
    return this.writeOverTime;
  }

  public void setReadOverTime(int time) {
    this.readOverTime = time;
  }

  public int getReadOverTime() {
    return this.readOverTime;
  }

  public void setOutputType(DATATYPE type) {
  }

  public void setInputType(DATATYPE type) {
  }

  public void setReadSize(int size) {
    this.readSize = size;
  }

  public int getReadSize() {
    return this.readSize;
  }

  public int getWriteSize() {
    return this.writeSize;
  }

  public void setWriteSize(int size) {
    this.writeSize = size;
  }

  public void setDestoff(int destoff) {
    this.destoff = destoff;
  }

  public int getDestoff() {
    return this.destoff;
  }

  public void setLast(int last) {
    this.last = last;
  }

  public int getLast() {
    return this.last;
  }
}
