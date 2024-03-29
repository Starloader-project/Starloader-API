package de.geolykt.starloader.api.gui;

import java.util.Collection;

import org.jetbrains.annotations.ApiStatus.AvailableSince;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;

import de.geolykt.starloader.DeprecatedSince;
import de.geolykt.starloader.api.CoordinateGrid;
import de.geolykt.starloader.api.gui.canvas.Canvas;
import de.geolykt.starloader.api.gui.canvas.CanvasManager;
import de.geolykt.starloader.api.gui.canvas.MultiCanvas;
import de.geolykt.starloader.api.gui.rendercache.RendercacheUtils;
import de.geolykt.starloader.api.gui.screen.Screen;
import de.geolykt.starloader.api.gui.screen.ScreenComponent;

/**
 * Abstract interface for the implementation of the {@link Drawing} class.
 */
public interface DrawingImpl {

    /**
     * Converts coordinates between two representations.
     * Note: Not all conversions are supported as of yet.
     *
     * @param from The source grid
     * @param to   The target grid
     * @param x    The x coordinate in the source grid
     * @param y    The y coordinate in the source grid
     * @return The coordinate vector in the target grid
     * @since 2.0.0
     */
    @NotNull
    public Vector3 convertCoordinates(@NotNull CoordinateGrid from, @NotNull CoordinateGrid to, float x, float y);

    /**
     * Draws a line on the user interface. Due to libGDX not supporting this
     * operation in a conventional way this method will by default be implemented by
     * stretching and rotating a pixel. As such this method indirectly calls
     * {@link Math#atan2(double, double)} and {@link Math#sqrt(double)} - but even
     * if libGDX had a .drawLine method, it would more or less involve using one of
     * these methods in the first place, so the caller should not worry about
     * performance all too much.
     *
     * @param x1     The X position of the origin point of the line
     * @param y1     The Y position of the origin point of the line
     * @param x2     The X position of the target point of the line
     * @param y2     The Y position of the target point of the line
     * @param width  The width of the line to draw
     * @param color  The color of the line that should be drawn
     * @param camera The camera, used to move the input positions to the global
     *               context.
     */
    public void drawLine(double x1, double y1, double x2, double y2, float width, @NotNull Color color,
            @NotNull Camera camera);

    /**
     * Draws text at the given location. The default color is used, which under
     * normal circumstances that's white, however the exact color is dependent on
     * the specification. Additionally the font shall be left unspecified.
     *
     * @param message The message to write
     * @param x       The X-location of the text
     * @param y       The Y-location of the text
     * @return The width of the text that was just drawn
     */
    public float drawText(@NotNull String message, float x, float y);

    /**
     * Draws text at the given location. The specified color should be used.
     * Additionally the font shall be left unspecified.
     *
     * @param message The message to write
     * @param x       The X-location of the text
     * @param y       The Y-location of the text
     * @param color   The color of the message
     * @return The width of the text that was just drawn
     */
    public default float drawText(@NotNull String message, float x, float y, @NotNull Color color) {
        return this.drawText(message, x, y, color, Drawing.TextSize.SMALL);
    }

    /**
     * Draws text at the given location. The specified color should be used.
     * Additionally the font shall be inferred by the given font size,
     * however no further guarantees are made.
     * The text may not persist across frames.
     *
     * @param message The message to write
     * @param x       The X-location of the text
     * @param y       The Y-location of the text
     * @param color   The color of the message
     * @param size    The font size.
     * @return The width of the text that was just drawn
     */
    public float drawText(@NotNull String message, float x, float y, @NotNull Color color, Drawing.@NotNull TextSize size);

    /**
     * Draws text at the given location. The specified color should be used.
     * Additionally the font shall be inferred by the given font size,
     * however no further guarantees are made.
     * The text may not persist across frames.
     *
     * @param message The message to write
     * @param x       The X-location of the text
     * @param y       The Y-location of the text
     * @param color   The color of the message
     * @param size    The font size.
     * @param camera  The camera to use (used for internal unprojecting)
     * @return The width of the text that was just drawn
     */
    public float drawText(@NotNull String message, float x, float y, @NotNull Color color, Drawing.@NotNull TextSize size, @NotNull Camera camera);

    /**
     * Fills a rectangle on the main drawing batch (as provided by {@link #getMainDrawingBatch()})
     * with a certain color.
     *
     * @param x         The X-position to draw on; it is the left corner of the rectangle.
     * @param y         The Y-position to draw on; it is not known which corner it corresponds to. Caution is advised
     * @param width     The width of the rectangle.
     * @param height    The height of the rectangle.
     * @param camera    The camera to use. It transforms x/y-positions of the drawn rectangle. <b>Width and height are unaffected</b>
     * @param fillColor The GDX color to fill it with.
     * @since 1.5.0
     * @deprecated This method has numerous flaws that cannot be addressed correctly. The most obvious one is that
     * it does not make sense for width and height to be unaffected by the camera. Another issue is that
     * this method and it's implementation predates SLAPI 2.0.0 and with that Galimulator 5.X, which means
     * that the implementation will directly draw to the main drawing batch and thus bypasses the rendercache
     * system - which can cause a crash with galimulator 5.X.
     * <br>It is advisable to migrate to {@link AsyncRenderer#fillRect(double, double, double, double, Color, Camera)},
     * although one would need to beware that width and height may be affected by the camera there.
     */
    @ScheduledForRemoval(inVersion = "3.0.0")
    @DeprecatedSince("2.0.0")
    @Deprecated
    public void fillRect(float x, float y, float width, float height, @NotNull Color fillColor, @NotNull Camera camera);

    /**
     * <b>As specified by the APINote, this method has unintended consequences. It does not only operate
     * like a fillRect() method, but also draws a frame around the rectangle. More specifically
     * this frame is assumed to be linked with {@link TextureProvider#getAlternateWindowNinepatch()}.</b>
     *
     * <p>Fills a rectangle with a given width and height with a specified color. As a
     * friendly reminder, the position 0,0 is the lower left corner and as the
     * values increase it moves to the to top right corner.
     *
     * @param x      The X position of the top left corner of the area to fill.
     * @param y      The Y position of the top left corner of the area to fill.
     * @param width  The width of the rectangle to fill.
     * @param height The height of the rectangle to fill.
     * @param color  The color used for the operation.
     * @param camera The camera used for the operation.
     * @apiNote It is not known what the effects are if this method is called by
     *          anything but the Widget. Act carefully for you may not want to call
     *          this method.
     * @since 1.5.0
     * @deprecated Bridges to {@link AsyncRenderer#fillWindow(float, float, float, float, Color, Camera)},
     * which should be preferred over this method as this method will eventually be removed.
     */
    @ScheduledForRemoval(inVersion = "3.0.0")
    @DeprecatedSince("2.0.0")
    @Deprecated
    public default void fillWindow(float x, float y, float width, float height, @NotNull Color color,
            @NotNull Camera camera) {
        AsyncRenderer.fillWindow(x, y, width, height, color, camera);
    }

    /**
     * Obtains the font types that are available in this implementation.
     *
     * @return A collection of all Font names available at this current time
     */
    public @NotNull Collection<String> getAvailiableFonts();

    /**
     * Obtains the currently active {@link CanvasManager} instance, which is used to create and open
     * {@link Canvas canvases} and {@link MultiCanvas multi-canvases}.
     *
     * @return The current active {@link CanvasManager}.
     * @since 2.0.0
     */
    @NotNull
    public CanvasManager getCanvasManager();

    /**
     * Obtains the {@link BitmapFont} associated with the font name. May return null
     * if the font name is not known or registered.
     *
     * @param font The font name from which the BitmapFont belong to
     * @return The {@link BitmapFont} associated under that name
     */
    @Nullable
    public BitmapFont getFontBitmap(@NotNull String font);

    /**
     * Obtains the main drawing sprite batch. Operations performed on this batch
     * will result in them getting displayed on the user interface.
     *
     * <p>SLAPI guarantees that during {@link ScreenComponent#renderAt(float, float, Camera)},
     * {@link SpriteBatch#isDrawing()} is returning true. In other circumstances the drawing
     * batch may allow drawing, but it might also not allow it. For safety reasons, other mods must
     * use {@link SpriteBatch#begin()} and {@link SpriteBatch#end()} before and after drawing respectively
     * if {@link SpriteBatch#isDrawing()} returns false. It is furthermore recommended to put the end
     * call in a finally block where as they try block encompasses the main drawing logic.
     *
     * @return The main drawing batch.
     */
    @NotNull
    public SpriteBatch getMainDrawingBatch();

    /**
     * Obtains the currently valid instance of the {@link RendercacheUtils} interface.
     *
     * @return The instance of that interface.
     * @since 2.0.0
     */
    @NotNull
    public RendercacheUtils getRendercacheUtils();

    /**
     * Obtains the {@link BitmapFont} that corresponds to the "SPACE" font type (returned by
     * {@link #getAvailiableFonts()} and used for {@link #getFontBitmap(String)}. As of the latest galimulator
     * 5.0 alpha build for October 15th 2022, the concrete font used is "Signika2.fnt", located in the
     * "data/fonts" directory.
     *
     * <p>Should for whatever reason the font type not exist in future galimulator releases, an adequate replacement
     * needs to be returned - this method shouldn't throw an exception.
     *
     * @return The {@link BitmapFont} used for the "SPACE" font type.
     * @since 2.0.0
     */
    @NotNull
    public BitmapFont getSpaceFont();

    /**
     * Obtains the instance's {@link de.geolykt.starloader.api.gui.text.TextFactory}.
     *
     * @return The {@link de.geolykt.starloader.api.gui.text.TextFactory} bound to the implementation
     * @deprecated The Text API is deprecated and marked for removal
     */
    @ScheduledForRemoval(inVersion = "3.0.0")
    @DeprecatedSince("2.0.0")
    @Deprecated
    public de.geolykt.starloader.api.gui.text.@NotNull TextFactory getTextFactory();

    /**
     * Obtains the texture provider that is valid for this drawing instance.
     *
     * @return The connected {@link TextureProvider}.
     */
    @NotNull
    public TextureProvider getTextureProvider();

    /**
     * Reads the file at the given path (which is relative to the data directory)
     * as a texture and binds it into the game's texture atlas.
     * If a texture is already bound, then that bound texture is returned.
     * If the texture cannot be bound then a placeholder texture is returned.
     * By default this is the smiling flower texture.
     *
     * @param path The path to the image file to load.
     * @return The bound texture.
     */
    @NotNull
    public Texture loadTexture(@NotNull String path);

    /**
     * Sends a bulletin to the player which is visible in the bottom left in most
     * cases. The message will be prefixed by the Space Oddity message; useful for
     * making your own space oddities
     *
     * @param text The text to send
     * @deprecated The Text API was deprecated for removal. There are no planned alternative
     * to this method. If you wish to use colored text in your bulletins, use GDX Color escapes
     * such has "[red]this text is red![] While this one is in the standard color."
     */
    @ScheduledForRemoval(inVersion = "3.0.0")
    @DeprecatedSince("2.0.0")
    @Deprecated
    public void sendBulletin(de.geolykt.starloader.api.gui.text.@NotNull FormattedText text);

    /**
     * Sends a bulletin to the player which is visible in the bottom left in most
     * cases.
     *
     * @param message The message to send
     */
    public void sendBulletin(@NotNull String message);

    /**
     * Sends a bulletin to the player which is visible in the bottom left in most
     * cases. The message will be prefixed by the Space Oddity message; useful for
     * making your own space oddities
     *
     * @param message The message to send
     */
    public void sendOddityBulletin(@NotNull String message);

    /**
     * Display a given {@link Stage}, overwriting the currently active stage.
     * This method does not pause the game nor does it unpause it. This may need to be done manually.
     * This method is also not blocking and may not be called asynchronously!
     *
     * <p>The currently active stage will be disposed.
     *
     * @param stage The stage to display, or null to make use of the standard drawing logic (i.e. vanilla galimulator).
     * @since 2.0.0-a20240102
     */
    @AvailableSince(value = "2.0.0-a20240102")
    @NonBlocking
    public void setShownStage(@Nullable Stage stage);

    /**
     * Shows this specific screen to the user.
     *
     * @param screen The screen to display
     */
    public void showScreen(@NotNull Screen screen);

    /**
     * Creates a {@link TextInputBuilder} for obtaining String input from the User.
     * The returned Builder should implicitly honour the native key input preference
     * unless otherwise specified.
     *
     * @param title The title of the input dialog.
     * @param text The text of the dialog, is - misleadingly - the text of the widget, not the prefilled text.
     * @param hint The hint of the dialog, is - misleadingly - the text of the widget, not the prefilled text. Hint and text only differ in colour.
     * @return A new {@link TextInputBuilder} instance
     */
    public @NotNull TextInputBuilder textInputBuilder(@NotNull String title, @NotNull String text,
            @NotNull String hint);

    /**
     * Displays a toast message to the user. In vanilla galimulator this is the orange box
     * in the top left corner.
     *
     * @param text The text to display.
     */
    public void toast(@NotNull String text);
}
