package de.geolykt.starloader.impl.text;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;

import de.geolykt.starloader.DeprecatedSince;
import de.geolykt.starloader.api.NullUtils;
import de.geolykt.starloader.api.gui.text.FormattedTextComponent;
import de.geolykt.starloader.api.gui.text.TextComponent;

@ScheduledForRemoval(inVersion = "3.0.0")
@DeprecatedSince("2.0.0")
@Deprecated
public class SingletonTextComponent implements FormattedTextComponent {

    private final @NotNull TextComponent component;

    public SingletonTextComponent(@NotNull String s) {
        this(new ColoredTextComponent(s));
    }

    /**
     * Constructor.
     *
     * @param s The contents of the component as a string.
     * @param gdxColor The color of the component
     * @since 2.0.0
     */
    public SingletonTextComponent(@NotNull String s, @NotNull Color gdxColor) {
        this(new ColoredTextComponent(s, gdxColor));
    }

    public SingletonTextComponent(@NotNull TextComponent component) {
        this.component = component;
    }

    @Override
    public @NotNull List<@NotNull TextComponent> getComponents() {
        return NullUtils.requireNotNull(Arrays.asList(component));
    }

    @Override
    public @NotNull String getText() {
        return component.getText();
    }

    @Override
    public float renderText(float x, float y) {
        return component.renderText(x, y);
    }

    @Override
    public float renderTextAt(float x, float y, @NotNull Camera camera) {
        return component.renderTextAt(x, y, camera);
    }
}
