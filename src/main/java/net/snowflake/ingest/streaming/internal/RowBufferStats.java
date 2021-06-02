/*
 * Copyright (c) 2021 Snowflake Computing Inc. All rights reserved.
 */

package net.snowflake.ingest.streaming.internal;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Keeps track of the active EP stats, used to generate a file EP info */
class RowBufferStats {
  private String currentMinStrValue;
  private String currentMaxStrValue;
  private BigInteger currentMinIntValue;
  private BigInteger currentMaxIntValue;
  private Double currentMinRealValue;
  private Double currentMaxRealValue;
  private long currentNullCount;
  // for binary or string columns
  private long currentMaxLength;

  private long numRows; // TODO Remove when ndv calculation updated

  /** Creates empty stats */
  RowBufferStats() {
    this.currentMaxStrValue = null;
    this.currentMinStrValue = null;
    this.currentMaxIntValue = null;
    this.currentMinIntValue = null;
    this.currentMaxRealValue = null;
    this.currentMinRealValue = null;
    this.currentNullCount = 0;
    this.currentMaxLength = 0;
  }

  public static RowBufferStats getCombinedStats(RowBufferStats left, RowBufferStats right) {
    RowBufferStats combined = new RowBufferStats();

    if (left.currentMinIntValue != null) {
      combined.addIntValue(left.currentMinIntValue, false);
      combined.addIntValue(left.currentMaxIntValue, false);
    }

    if (right.currentMinIntValue != null) {
      combined.addIntValue(right.currentMinIntValue, false);
      combined.addIntValue(right.currentMaxIntValue, false);
    }

    if (left.currentMinStrValue != null) {
      combined.addStrValue(left.currentMinStrValue, false);
      combined.addStrValue(left.currentMaxStrValue, false);
    }

    if (right.currentMinStrValue != null) {
      combined.addStrValue(right.currentMinStrValue, false);
      combined.addStrValue(right.currentMaxStrValue, false);
    }

    if (left.currentMinRealValue != null) {
      combined.addRealValue(left.currentMinRealValue, false);
      combined.addRealValue(left.currentMaxRealValue, false);
    }

    if (right.currentMinRealValue != null) {
      combined.addRealValue(right.currentMinRealValue, false);
      combined.addRealValue(right.currentMaxRealValue, false);
    }

    combined.currentNullCount = left.currentNullCount + right.currentNullCount;

    combined.numRows = left.numRows + right.numRows;
    return combined;
  }

  void addStrValue(String value) {
    addStrValue(value, true);
  }

  void addStrValue(String value, boolean countDistinct) {
    if (countDistinct) {
      numRows += 1;
    }

    // Snowflake compares strings in UTF-8 encoding, not Java's default UTF-16
    byte[] currentMinStringBytes =
        currentMinStrValue != null ? currentMinStrValue.getBytes(StandardCharsets.UTF_8) : null;
    byte[] currentMaxStringBytes =
        currentMaxStrValue != null ? currentMaxStrValue.getBytes(StandardCharsets.UTF_8) : null;
    byte[] valueBytes = value != null ? value.getBytes(StandardCharsets.UTF_8) : null;

    // Check if new min string
    if (currentMinStrValue == null) {
      currentMinStrValue = value;
    } else if (Arrays.compare(currentMinStringBytes, valueBytes) > 0) {
      currentMinStrValue = value;
    }

    // Check if new max string
    if (this.currentMaxStrValue == null) {
      this.currentMaxStrValue = value;
    } else if (Arrays.compare(currentMaxStringBytes, valueBytes) < 0) {
      this.currentMaxStrValue = value;
    }
  }

  String getCurrentMinStrValue() {
    return currentMinStrValue;
  }

  String getCurrentMaxStrValue() {
    return currentMaxStrValue;
  }

  void addIntValue(BigInteger value) {
    addIntValue(value, true);
  }

  void addIntValue(BigInteger value, boolean countDistinct) {
    if (countDistinct) {
      numRows += 1;
    }

    // Set new min value
    if (this.currentMinIntValue == null) {
      this.currentMinIntValue = value;
    } else if (this.currentMinIntValue.compareTo(value) > 0) {
      this.currentMinIntValue = value;
    }

    // Set new max value
    if (this.currentMaxIntValue == null) {
      this.currentMaxIntValue = value;
    } else if (this.currentMaxIntValue.compareTo(value) < 0) {
      this.currentMaxIntValue = value;
    }
  }

  BigInteger getCurrentMinIntValue() {
    return currentMinIntValue;
  }

  BigInteger getCurrentMaxIntValue() {
    return currentMaxIntValue;
  }

  void addRealValue(Double value) {
    addRealValue(value, true);
  }

  void addRealValue(Double value, boolean countDistinct) {
    if (countDistinct) {
      numRows += 1;
    }

    // Set new min value
    if (this.currentMinRealValue == null) {
      this.currentMinRealValue = value;
    } else if (this.currentMinRealValue.compareTo(value) > 0) {
      this.currentMinRealValue = value;
    }

    // Set new max value
    if (this.currentMaxRealValue == null) {
      this.currentMaxRealValue = value;
    } else if (this.currentMaxRealValue.compareTo(value) < 0) {
      this.currentMaxRealValue = value;
    }
  }

  Double getCurrentMinRealValue() {
    return currentMinRealValue;
  }

  Double getCurrentMaxRealValue() {
    return currentMaxRealValue;
  }

  void incCurrentNullCount() {
    this.currentNullCount += 1;
  }

  long getCurrentNullCount() {
    return currentNullCount;
  }

  void setCurrentMaxLength(long currentMaxLength) {
    if (currentMaxLength > this.currentMaxLength) {
      this.currentMaxLength = currentMaxLength;
    }
  }

  long getCurrentMaxLength() {
    return currentMaxLength;
  }

  long getDistinctValues() {
    return numRows;
  }
}
