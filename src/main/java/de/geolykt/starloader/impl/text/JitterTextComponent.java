package de.geolykt.starloader.impl.text;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;

import de.geolykt.starloader.DeprecatedSince;
import de.geolykt.starloader.api.gui.Drawing;

@ScheduledForRemoval(inVersion = "3.0.0")
@DeprecatedSince("2.0.0")
@Deprecated
public class JitterTextComponent extends ColoredTextComponent {

    protected final float intensity;

    public JitterTextComponent(@NotNull String text, @NotNull Color color, double intensity) {
        this(text, color, intensity, Drawing.TextSize.SMALL);
    }

    public JitterTextComponent(@NotNull String text, @NotNull Color color, double intensity, Drawing.@NotNull TextSize size) {
        super(text, color, Objects.requireNonNull(size));
        this.intensity = (float) intensity;
    }

    @Override
    public float renderText(float x, float y) {
        float jitterX = (ThreadLocalRandom.current().nextFloat() - 0.5f) * intensity;
        float jitterY = (ThreadLocalRandom.current().nextFloat() - 0.5f) * intensity;
        return super.renderText(jitterX + x, jitterY + y);
    }

    @Override
    public float renderTextAt(float x, float y, @NotNull Camera camera) {
        float jitterX = (ThreadLocalRandom.current().nextFloat() - 0.5f) * intensity;
        float jitterY = (ThreadLocalRandom.current().nextFloat() - 0.5f) * intensity;
        return super.renderTextAt(jitterX + x, jitterY + y, camera);
    }
}
