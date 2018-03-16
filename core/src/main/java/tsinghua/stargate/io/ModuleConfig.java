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

public abstract class ModuleConfig {

  public enum DATATYPE {
    FILETYPE(0),

    FLOATTYPE(1),

    INTTYPE(2);

    private final int value;

    DATATYPE(int value) {
      this.value = value;
    }

    public static DATATYPE valueOf(int value) {
      switch (value) {
      case 0:
        return FILETYPE;
      case 1:
        return FLOATTYPE;
      case 2:
        return INTTYPE;
      default:
        return null;
      }
    }

    public int getValue() {
      return this.value;
    }
  }

  public String type;

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public abstract void setWriteSize(int size);

  public abstract void setReadSize(int size);

  public abstract void setInputType(DATATYPE type);

  public abstract void setOutputType(DATATYPE type);

  public abstract void setReadOverTime(int time);

  public abstract void setWriteOverTime(int time);
}