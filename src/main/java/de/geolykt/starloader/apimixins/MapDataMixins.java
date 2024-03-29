package de.geolykt.starloader.apimixins;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.badlogic.gdx.graphics.Texture;

import de.geolykt.starloader.DeprecatedSince;
import de.geolykt.starloader.api.Map;
import de.geolykt.starloader.api.NullUtils;
import de.geolykt.starloader.api.resource.DataFolderProvider;

import snoddasmannen.galimulator.MapData;
import snoddasmannen.galimulator.StarGenerator;

@Mixin(MapData.class)
public class MapDataMixins implements Map {

    private BufferedImage awtImage = null;

    @Shadow
    private String backgroundImage;

    @Shadow
    private StarGenerator generator;

    @Override
    @ScheduledForRemoval(inVersion = "3.0.0")
    @DeprecatedSince("2.0.0")
    @Deprecated
    @Nullable
    public BufferedImage getAWTBackground() {
        String backgroundImage = this.backgroundImage;
        if (backgroundImage != null) {
            if (awtImage != null) {
                return awtImage;
            }
            File f = new File(DataFolderProvider.getProvider().provideAsFile(), backgroundImage);
            if (f.exists()) {
                try {
                    awtImage = ImageIO.read(f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public @Nullable String getBackgroundFilename() {
        return backgroundImage;
    }

    @Override
    public @Nullable Texture getGDXBackground() {
        return ((MapData) (Object) this).getTexture();
    }

    @Override
    public @NotNull String getGeneratorName() {
        return NullUtils.requireNotNull(generator.name());
    }

    @Override
    public float getHeight() {
        return generator.getMaxY() * 2;
    }

    @Override
    public float getWidth() {
        return generator.getMaxX() * 2;
    }
}
