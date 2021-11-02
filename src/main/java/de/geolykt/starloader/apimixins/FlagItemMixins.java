package de.geolykt.starloader.apimixins;

import java.awt.Color;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import de.geolykt.starloader.api.gui.FlagComponent;
import de.geolykt.starloader.api.gui.FlagSymbol;
import de.geolykt.starloader.impl.AWTColorAccesor;

import snoddasmannen.galimulator.FlagItem;
import snoddasmannen.galimulator.FlagItem.BuiltinSymbols;
import snoddasmannen.galimulator.GalColor;

@Mixin(FlagItem.class)
public class FlagItemMixins implements FlagComponent {

    @Shadow
    public boolean border;

    @Shadow
    boolean center;

    @Shadow
    private GalColor color;

    @Shadow
    int height;

    @Shadow
    private float rotation;

    @Shadow
    BuiltinSymbols symbol;

    @Shadow
    int width;

    @Shadow
    float x;

    @Shadow
    float y;

    @Override
    @NotNull
    public Color getAWTColor() {
        return ((AWTColorAccesor) color).asAWTColor();
    }

    @SuppressWarnings("null")
    @Override
    @NotNull
    public com.badlogic.gdx.graphics.Color getGDXColor() {
        return color.getGDXColor();
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public float getRotation() {
        return rotation;
    }

    @SuppressWarnings("null")
    @Override
    public @NotNull FlagSymbol getSymbol() {
        return (FlagSymbol) (Object) symbol;
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public boolean hasBorder() {
        return border;
    }

    @Override
    public boolean isCentering() {
        return center;
    }
}
