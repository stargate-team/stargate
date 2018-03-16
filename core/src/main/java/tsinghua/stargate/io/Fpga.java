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

import java.io.Serializable;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * An abstract representation of FPGA.
 *
 * <p>
 * User interfaces use <em>integer identifiers</em> to identify FPGAs. This
 * class presents an abstract, system-independent view of FPGA. An <em>abstract
 * identifier</em> has two components:
 *
 * <ol>
 * <li>An <em>integer id</em> specifies which FPGA card.</li>
 * <li>An <em>enum core</em> specifies FPGA clock frequency.</li>
 * </ol>
 */
public class Fpga implements Serializable {

  private static final long serialVersionUID = -8319510710426629511L;

  public enum Core {

    CLOCK_FREQUENCY_5MHZ("5 MHz", 0),

    CLOCK_FREQUENCY_10MHZ("10 MHz", 1),

    CLOCK_FREQUENCY_25MHZ("25 MHz", 2),

    CLOCK_FREQUENCY_50MHZ("50 MHz", 3),

    CLOCK_FREQUENCY_75MHZ("75 MHz", 4),

    CLOCK_FREQUENCY_100MHZ("100 MHz", 5),

    CLOCK_FREQUENCY_125MHZ("125 MHz", 6),

    CLOCK_FREQUENCY_150MHZ("150 MHz", 7),

    CLOCK_FREQUENCY_175MHZ("175 MHz", 8),

    CLOCK_FREQUENCY_200MHZ("200 MHz", 9),

    CLOCK_FREQUENCY_225MHZ("225 MHz", 10),

    CLOCK_FREQUENCY_250MHZ("250 MHz", 11);

    private String name;
    private int value;

    Core(String name, int value) {
      this.name = name;
      this.value = value;
    }

    public static String getName(int value) {
      Preconditions.checkArgument(value >= 0 && value <= 11,
          "Invalid FPGA core");
      String name = null;
      for (Core c : Core.values()) {
        if (c.getValue() == value) {
          name = c.getName();
          break;
        }
      }
      return name;
    }

    public String getName() {
      return name;
    }

    public int getValue() {
      return value;
    }

    @Override
    public String toString() {
      return "Core " + getValue() + " works on " + getName();
    }
  }

  /** Id for opening a specific FPGA card. */
  private final int id;

  /** Current clock frequency of the underlying FPGA card. */
  private Core core;

  /**
   * Create a new {@code Fpga} instance with a given {@code id}.
   *
   * @param id the FPGA card identification
   */
  public Fpga(int id) {
    this(id, Core.CLOCK_FREQUENCY_250MHZ);
  }

  /**
   * Create a new {@code Fpga} instance with a given {@code id} and
   * {@code core}.
   *
   * @param id the FPGA card identification
   * @param core the FPGA core
   */
  public Fpga(int id, Core core) {
    Preconditions.checkArgument(id >= 0, "Invalid FPGA id");
    Preconditions.checkArgument(core.getValue() >= 0 && core.getValue() <= 11,
        "Invalid FPGA core");
    this.id = id;
    this.core = core;
  }

  public int getId() {
    return id;
  }

  public Core getCore() {
    return core;
  }

  public void setCore(Core core) {
    Preconditions.checkArgument(core.getValue() >= 0 && core.getValue() <= 11,
        "Invalid FPGA core");
    this.core = core;
  }

  /**
   * Tests this FPGA object for equality with the given object. Returns
   * {@code true} if and only if the objects or their all fields are the same.
   *
   * @param o the object to be compared with this FPGA object
   * @return {@code true} if and only if the objects are the same, {@code false}
   *         otherwise
   */
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Fpga fpga = (Fpga) o;
    return id == fpga.id && core == fpga.core;
  }

  /**
   * Computes a hash code for this FPGA object as per its fields.
   *
   * @return a hash code for this FPGA object
   */
  @Override
  public int hashCode() {
    return Objects.hashCode(id, core);
  }

  /**
   * Container for holding information about all the installed FPGAs accessible
   * by Riffa.
   */
  public static class FpgaInfo {

    private static final int NUM_FPGAS = 5;
    private int numFpgas;
    private int[] numChannels;
    private int[] id;
    private int[] vendorId;
    private int[] deviceId;
    private String[] name;

    FpgaInfo() {
      this.numFpgas = 0;
      this.name = new String[NUM_FPGAS];
      this.id = new int[NUM_FPGAS];
      this.numChannels = new int[NUM_FPGAS];
      this.deviceId = new int[NUM_FPGAS];
      this.vendorId = new int[NUM_FPGAS];
    }

    /**
     * Returns the number of Riffa accessible FPGAs installed in the system.
     *
     * @return the number of Riffa accessible FPGAs installed in the system
     */
    int getNumFpgas() {
      return this.numFpgas;
    }

    /**
     * Sets the number of Riffa accessible FPGAs installed in the system.
     *
     * @param val the number of Riffa accessible FPGAs installed in the system
     */
    public void setNumFpgas(int val) {
      this.numFpgas = val;
    }

    /**
     * Returns the number of Riffa channels configured on the FPGA at position
     * {@code pos}.
     *
     * @return the number of Riffa channels configured on the FPGA at position
     *         {@code pos}
     */
    int getNumChannels(int pos) {
      return this.numChannels[pos];
    }

    /**
     * Sets the number of Riffa channels configured on the FPGA at position
     * {@code pos}.
     *
     * @param pos the position of FPGA
     * @param val the number of Riffa channels configured on the FPGA at
     *          position {@code pos}
     */
    public void setNumChannels(int pos, int val) {
      this.numChannels[pos] = val;
    }

    /**
     * Returns the FPGA id at position {@code pos}. This id is used to open the
     * FPGA on the FPGA's open method.
     *
     * @return the FPGA id at position {@code pos}
     */
    int getId(int pos) {
      return this.id[pos];
    }

    /**
     * Sets the FPGA id at position {@code pos}.
     *
     * @param pos the position of FPGA
     * @param val the FPGA id at position {@code pos}
     */
    public void setId(int pos, int val) {
      this.id[pos] = val;
    }

    /**
     * Returns the location of the FPGA at position {@code pos}. This is
     * typically the PCIe bus and slot number.
     *
     * @return the location of the FPGA at position {@code pos}
     */
    String getName(int pos) {
      return this.name[pos];
    }

    /**
     * Sets the location of the FPGA at position {@code pos}.
     *
     * @param pos the position of FPGA
     * @param val the location of the FPGA at position {@code pos}
     */
    public void setName(int pos, String val) {
      this.name[pos] = val;
    }

    /**
     * Returns the FPGA vendor id at position {@code pos}.
     *
     * @return the FPGA vendor id at position {@code pos}
     */
    int getVendorId(int pos) {
      return this.vendorId[pos];
    }

    /**
     * Sets the FPGA vendor id at position {@code pos}.
     *
     * @param pos the position of FPGA
     * @param val the FPGA vendor id at position {@code pos}
     */
    public void setVendorId(int pos, int val) {
      this.vendorId[pos] = val;
    }

    /**
     * Returns the FPGA device id at position {@code pos}.
     *
     * @return the FPGA device id at position {@code pos}
     */
    int getDeviceId(int pos) {
      return this.deviceId[pos];
    }

    /**
     * Sets the FPGA device id at position {@code pos}.
     *
     * @param pos the position of FPGA
     * @param val the FPGA device id at position {@code pos}
     */
    public void setDeviceId(int pos, int val) {
      this.deviceId[pos] = val;
    }

    /**
     * Returns a nicely formatted listing of all the Riffa FPGAs detected.
     *
     * @return a nicely formatted String of all the Riffa FPGAs detected
     */
    public String toString() {
      StringBuilder sb = new StringBuilder();
      String eol = System.getProperty("line.separator");
      sb.append("Total FPGAs: ").append(this.numFpgas).append(eol);
      for (int i = 0; i < this.numFpgas; i++) {
        sb.append("FPGA id: ").append(this.id[i]).append(eol);
        sb.append("Total cores: ").append(this.numChannels[i]).append(eol);
        sb.append("Vendor id: ").append(Integer.toHexString(this.vendorId[i]))
            .append(eol);
        sb.append("Device id: ").append(Integer.toHexString(this.deviceId[i]))
            .append(eol);
        sb.append("Device name: ").append(this.name[i]).append(eol);
      }
      return sb.toString();
    }
  }
}
