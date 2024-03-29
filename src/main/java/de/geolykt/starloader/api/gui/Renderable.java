package de.geolykt.starloader.api.gui;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.Camera;

/**
 * Interface that provides methods for rending across many graphical object structures.
 * The actual render process has to be performed by the implementation of the methods.
 */
public interface Renderable {

    /**
     * Renders the object on screen at the given coordinates. The view may get
     * unprojected with the given camera.
     * For galimulator, y = 0, x = 0 is the lower left edge, positive numbers go more towards the upper right.
     * Within most sub-APIs provided by the SLAPI this behaviour is mirrored.
     *
     * @param x The X-Coordinate of the rendering position
     * @param y The Y-Coordinate of the rendering position
     * @param camera The camera to use (used for unprojection)
     * @return The width of the object that was just rendered
     */
    public int renderAt(float x, float y, @NotNull Camera camera);
}
