package androidx.media3.effect;
/*
 * Copyright 2022 The Android Open Source Project
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
import static androidx.media3.common.util.Assertions.checkArgument;

import androidx.annotation.FloatRange;
import androidx.media3.common.util.GlUtil;
import androidx.media3.common.util.UnstableApi;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/** Contains information to control how an {@link TextureOverlay} is displayed on the screen. */
@UnstableApi
public final class OverlaySettings {
  public final boolean useHdr;
  public final float alpha;
  public final float[] matrix;

  private OverlaySettings(boolean useHdr, float alpha, float[] matrix) {
    this.useHdr = useHdr;
    this.alpha = alpha;
    this.matrix = matrix;
  }

  /** A builder for {@link OverlaySettings} instances. */
  public static final class Builder {
    private boolean useHdr;
    private float alpha = 1;
    private float[] matrix;

    /** Creates a new {@link Builder}. */
    public Builder() {
      matrix = GlUtil.create4x4IdentityMatrix();
    }

    /**
     * Sets whether input overlay comes from an HDR source. If {@code true}, colors will be in
     * linear RGB BT.2020. If {@code false}, colors will be in linear RGB BT.709.
     *
     * <p>Set to {@code false} by default.
     */
    @CanIgnoreReturnValue
    public Builder setUsesHdr(boolean useHdr) {
      this.useHdr = useHdr;
      return this;
    }

    /**
     * Sets the {@link android.opengl.Matrix} used to transform the overlay before applying it to a
     * frame.
     *
     * <p>Set to always return the identity matrix by default.
     */
    @CanIgnoreReturnValue
    public Builder setMatrix(float[] matrix) {
      this.matrix = matrix;
      return this;
    }

    /**
     * Sets the alpha value of the overlay, altering its transparency.
     *
     * <p>Alpha values range from 0 (all transparent) to 1 (completely opaque).
     *
     * <p>Set to always return {@code 1} by default.
     */
    @CanIgnoreReturnValue
    public Builder setAlpha(@FloatRange(from = 0, to = 1) float alpha) {
      checkArgument(0 <= alpha && alpha <= 1, "Alpha needs to be in the interval [0, 1].");
      this.alpha = alpha;
      return this;
    }

    /** Creates an instance of {@link OverlaySettings}, using defaults if values are unset. */
    public OverlaySettings build() {
      return new OverlaySettings(useHdr, alpha, matrix);
    }
  }
}
