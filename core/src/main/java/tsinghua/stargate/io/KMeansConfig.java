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

public class KMeansConfig extends ModuleConfig {

  private int writeOverTime;
  private int readOverTime;
  private int writeSize;
  private int readSize;
  private DATATYPE inDataType;
  private DATATYPE outDataType;

  private int minNclusters = 0;
  private int maxNclusters = 0;
  private float threshold = 0;
  private String inFileName;
  private String outFileName;
  private int line;
  private int row;
  private String accelerateBitFile;

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
    this.outDataType = type;
  }

  public int getOutputType() {
    return this.outDataType.getValue();
  }

  public void setInputType(DATATYPE type) {
    this.inDataType = type;
  }

  public int getInputType() {
    return this.inDataType.getValue();
  }

  public void setReadSize(int size) {
    this.writeSize = size;
  }

  public int getReadSize() {
    return this.readSize;
  }

  public void setWriteSize(int size) {
    this.readSize = size;
  }

  public int getWriteSize() {
    return this.writeSize;
  }

  public void setMinNclusters(int min) {
    this.minNclusters = min;
  }

  public int getMinNclusters() {
    return this.minNclusters;
  }

  public void setMaxNclusters(int max) {
    this.maxNclusters = max;
  }

  public int getMaxNclusters() {
    return this.maxNclusters;
  }

  public void setThreshold(float threshold) {
    this.threshold = threshold;
  }

  public float getThreshold() {
    return this.threshold;
  }

  public void setInFileName(String name) {
    this.inFileName = name;
  }

  public String getInFileName() {
    return this.inFileName;
  }

  public void setOutFileName(String name) {
    this.outFileName = name;
  }

  public String getOutFileName() {
    return outFileName;
  }

  public void setLine(int line) {
    this.line = line;
  }

  public int getLine() {
    return line;
  }

  public void setRow(int row) {
    this.row = row;
  }

  public int getRow() {
    return this.row;
  }

  public void setAccelerateBitPath(String filename) {
    this.accelerateBitFile = filename;
  }

  public String getAccelerateBitPath() {
    return this.accelerateBitFile;
  }

  @Override public String toString() {
    return "KMeansConfig{" +
        "writeOverTime=" + writeOverTime +
        ", readOverTime=" + readOverTime +
        ", writeSize=" + writeSize +
        ", readSize=" + readSize +
        ", inDataType=" + inDataType +
        ", outDataType=" + outDataType +
        ", minNclusters=" + minNclusters +
        ", maxNclusters=" + maxNclusters +
        ", threshold=" + threshold +
        ", inFileName='" + inFileName + '\'' +
        ", outFileName='" + outFileName + '\'' +
        ", line=" + line +
        ", row=" + row +
        ", accelerateBitFile='" + accelerateBitFile + '\'' +
        '}';
  }
}
