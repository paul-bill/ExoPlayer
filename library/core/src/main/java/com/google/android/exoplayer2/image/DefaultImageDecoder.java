/*
 * Copyright 2023 The Android Open Source Project
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
package com.google.android.exoplayer2.ext.image;

import static com.google.android.exoplayer2.decoder.DecoderInputBuffer.BUFFER_REPLACEMENT_MODE_NORMAL;
import static com.google.android.exoplayer2.util.Assertions.checkArgument;
import static com.google.android.exoplayer2.util.Assertions.checkNotNull;
import static com.google.android.exoplayer2.util.Assertions.checkState;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;
import com.google.android.exoplayer2.decoder.DecoderInputBuffer;
import com.google.android.exoplayer2.decoder.SimpleDecoder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * An image decoder that uses {@link BitmapFactory} to decode images.
 *
 * <p>Only supports decoding one input buffer into one output buffer (i.e. one {@link Bitmap}
 * alongside one timestamp)).
 *
 * @deprecated com.google.android.exoplayer2 is deprecated. Please migrate to androidx.media3 (which
 *     contains the same ExoPlayer code). See <a
 *     href="https://developer.android.com/guide/topics/media/media3/getting-started/migration-guide">the
 *     migration guide</a> for more details, including a script to help with the migration.
 */
@Deprecated
public class DefaultImageDecoder
    extends SimpleDecoder<DecoderInputBuffer, ImageOutputBuffer, ImageDecoderException>
    implements ImageDecoder {

  /** Creates an instance. */
  public DefaultImageDecoder() {
    super(new DecoderInputBuffer[1], new ImageOutputBuffer[1]);
  }

  @Override
  public final String getName() {
    return "DefaultImageDecoder";
  }

  @Override
  protected DecoderInputBuffer createInputBuffer() {
    return new DecoderInputBuffer(BUFFER_REPLACEMENT_MODE_NORMAL);
  }

  @Override
  protected ImageOutputBuffer createOutputBuffer() {
    return new ImageOutputBuffer() {
      @Override
      public void release() {
        DefaultImageDecoder.this.releaseOutputBuffer(this);
      }
    };
  }

  @Override
  protected ImageDecoderException createUnexpectedDecodeException(Throwable error) {
    return new ImageDecoderException("Unexpected decode error", error);
  }

  @Nullable
  @Override
  protected ImageDecoderException decode(
      DecoderInputBuffer inputBuffer, ImageOutputBuffer outputBuffer, boolean reset) {
    try {
      ByteBuffer inputData = checkNotNull(inputBuffer.data);
      checkState(inputData.hasArray());
      checkArgument(inputData.arrayOffset() == 0);
      outputBuffer.bitmap = decode(inputData.array(), inputData.remaining());
      outputBuffer.timeUs = inputBuffer.timeUs;
      return null;
    } catch (ImageDecoderException e) {
      return e;
    }
  }

  /**
   * Decodes data into a {@link Bitmap}.
   *
   * @param data An array holding the data to be decoded, starting at position 0.
   * @param length The length of the input to be decoded.
   * @return The decoded {@link Bitmap}.
   * @throws ImageDecoderException If a decoding error occurs.
   */
  protected Bitmap decode(byte[] data, int length) throws ImageDecoderException {
    @Nullable Bitmap bitmap = BitmapFactory.decodeByteArray(data, /* offset= */ 0, length);
    if (bitmap == null) {
      throw new ImageDecoderException(
          "Could not decode image data with BitmapFactory. (data length = " + data.length + ")");
    }
    // BitmapFactory doesn't read the exif header, so we use the ExifInterface to this do ensure the
    // bitmap is correctly orientated.
    ExifInterface exifInterface;
    try (InputStream inputStream = new ByteArrayInputStream(data)) {
      exifInterface = new ExifInterface(inputStream);
    } catch (IOException e) {
      throw new ImageDecoderException(e);
    }
    int rotationDegrees = exifInterface.getRotationDegrees();
    if (rotationDegrees != 0) {
      Matrix matrix = new Matrix();
      matrix.postRotate(rotationDegrees);
      bitmap =
          Bitmap.createBitmap(
              bitmap,
              /* x= */ 0,
              /* y= */ 0,
              bitmap.getWidth(),
              bitmap.getHeight(),
              matrix,
              /* filter= */ false);
    }
    return bitmap;
  }
}