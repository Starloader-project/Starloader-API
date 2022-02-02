package de.geolykt.starloader.impl.text;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.Camera;

import de.geolykt.starloader.api.NullUtils;
import de.geolykt.starloader.api.gui.Drawing;
import de.geolykt.starloader.api.gui.text.TextComponent;

import snoddasmannen.galimulator.GalColor;

public class ColoredTextComponent implements TextComponent {

    protected final @NotNull GalColor color;
    protected final Drawing.@NotNull TextSize size;
    protected final @NotNull String text;

    public ColoredTextComponent(@NotNull String s) {
        this(s, NullUtils.requireNotNull(GalColor.WHITE), Drawing.TextSize.SMALL);
    }

    public ColoredTextComponent(@NotNull String s, @NotNull GalColor color) {
        this(s, color, Drawing.TextSize.SMALL);
    }

    public ColoredTextComponent(@NotNull String s, @NotNull GalColor color, Drawing.@NotNull TextSize size) {
        this.text = s;
        this.color = color;
        this.size = NullUtils.requireNotNull(size);
    }

    @Override
    public @NotNull String getText() {
        return text;
    }

    @Override
    public float renderText(float x, float y) {
        return Drawing.drawText(text, x, y, color, size);
    }

    @Override
    public float renderTextAt(float x, float y, @NotNull Camera camera) {
        return Drawing.drawText(text, x, y, color, size, camera);
    }
}
