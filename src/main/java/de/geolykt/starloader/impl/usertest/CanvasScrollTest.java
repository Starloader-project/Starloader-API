package de.geolykt.starloader.impl.usertest;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import de.geolykt.starloader.api.NullUtils;
import de.geolykt.starloader.api.gui.Drawing;
import de.geolykt.starloader.api.gui.canvas.Canvas;
import de.geolykt.starloader.api.gui.canvas.CanvasContext;
import de.geolykt.starloader.api.gui.canvas.CanvasManager;
import de.geolykt.starloader.api.gui.canvas.CanvasSettings;
import de.geolykt.starloader.api.gui.canvas.ChildObjectOrientation;

public class CanvasScrollTest extends Usertest {

    public static class CanvasScrollChildContext implements CanvasContext {

        @NotNull
        private final Color color;
        private int value;
        private int x;
        private int y;

        @NotNull
        private final BitmapFont font;

        public CanvasScrollChildContext(Color color) {
            this.color = NullUtils.requireNotNull(color);
            this.font = NullUtils.requireNotNull(Drawing.getFontBitmap("FRIENDLY"), "Unable to find expected font");
        }

        @Override
        public void render(@NotNull SpriteBatch batch, @NotNull Camera camera) {
            font.setColor(color);
            font.draw(batch, Integer.toString(value), this.x, this.y);
            Drawing.drawLine(0, 0, getWidth(), getHeight(), 3, color, camera);
        }

        @Override
        public int getHeight() {
            return 200;
        }

        @Override
        public int getWidth() {
            return 200;
        }

        @Override
        public void onScroll(int canvasX, int canvasY, @NotNull Camera camera, int amount, @NotNull Canvas canvas) {
            this.value += amount;
            this.x = canvasX;
            this.y = canvasY;
            canvas.markDirty();
        }
    }

    @Override
    public void runTest() {
        CanvasManager manager = Drawing.getInstance().getCanvasManager();

        Canvas c1 = manager.newCanvas(new CanvasScrollChildContext(Color.WHITE), CanvasSettings.CHILD_TRANSPARENT);
        Canvas c2 = manager.newCanvas(new CanvasScrollChildContext(Color.LIGHT_GRAY), CanvasSettings.CHILD_TRANSPARENT);
        Canvas c3 = manager.newCanvas(new CanvasScrollChildContext(Color.CORAL), CanvasSettings.CHILD_TRANSPARENT);
        Canvas c4 = manager.newCanvas(new CanvasScrollChildContext(Color.RED), CanvasSettings.CHILD_TRANSPARENT);

        Canvas row1 = manager.multiCanvas(manager.dummyContext(400, 200), new CanvasSettings(NullUtils.requireNotNull(Color.BLUE)), ChildObjectOrientation.LEFT_TO_RIGHT, c1, c2);
        Canvas row2 = manager.multiCanvas(manager.dummyContext(400, 200), CanvasSettings.CHILD_TRANSPARENT, ChildObjectOrientation.LEFT_TO_RIGHT, c3, c4);

        CanvasSettings settings = new CanvasSettings("CanvasScrollTest");
        Canvas fullCanvas = manager.multiCanvas(manager.dummyContext(400, 400), settings, ChildObjectOrientation.BOTTOM_TO_TOP, row1, row2);
        fullCanvas.openCanvas();
    }

    @Override
    @NotNull
    public String getName() {
        return "CanvasScrollTest";
    }

    @Override
    @NotNull
    public String getCategoryName() {
        return "SLAPI";
    }
}
