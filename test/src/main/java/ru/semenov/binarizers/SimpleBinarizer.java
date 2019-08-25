/*
 * Copyright 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.semenov.binarizers;

import com.google.zxing.Binarizer;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.GlobalHistogramBinarizer;

/**
 * This class implements a global thresholding algorithm.
 */
public final class SimpleBinarizer extends HybridBinarizer {

  // This class uses 5x5 blocks to compute local luminance, where each block is 8x8 pixels.
  // So this is the smallest dimension in each axis we can accept.
  private int threshold = 128;

  private BitMatrix matrix;

  public SimpleBinarizer(LuminanceSource source) {
    super(source);
  }

  public SimpleBinarizer(LuminanceSource source, int threshold) {
    super(source);
    this.threshold = threshold;
  }


  /**
   * Calculates the final BitMatrix once for all requests. This could be called once from the
   * constructor instead, but there are some advantages to doing it lazily, such as making
   * profiling easier, and not doing heavy lifting when callers don't expect it.
   */
  @Override
  public BitMatrix getBlackMatrix() throws NotFoundException {
    if (matrix != null) {
      return matrix;
    }
    LuminanceSource source = getLuminanceSource();
    int width = source.getWidth();
    int height = source.getHeight();
    byte[] luminances = source.getMatrix();
    BitMatrix newMatrix = new BitMatrix(width, height);
    calculateThresholdForBlock(luminances, width, height, newMatrix);
    matrix = newMatrix;
    return matrix;
  }

  @Override
  public Binarizer createBinarizer(LuminanceSource source) {
    return new SimpleBinarizer(source);
  }

  /**
   * For each block in the image, calculate the average black point using a 5x5 grid
   * of the blocks around it. Also handles the corner cases (fractional blocks are computed based
   * on the last pixels in the row/column which are also used in the previous block).
   */
  private void calculateThresholdForBlock(byte[] luminances,
                                                 int width,
                                                 int height,
                                                 BitMatrix matrix) {
    for (int y = 0; y < height; y++) {
      int offset = y * width;
      for (int x = 0; x < width; x++) {
        if ((luminances[offset + x] & 0xFF) <= threshold) {
          matrix.set(x, y);
        }
      }
    }
  }
}
