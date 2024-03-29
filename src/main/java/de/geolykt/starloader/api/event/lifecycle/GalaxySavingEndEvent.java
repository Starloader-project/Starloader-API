package de.geolykt.starloader.api.event.lifecycle;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.event.Event;

/**
 * Event that is fired when a Galaxy is about to save.
 * Note that this event may get fired outside of the main thread - invoking
 * saves in an async manner is highly discouraged however.
 */
public class GalaxySavingEndEvent extends Event {

    /**
     * The location where the state was saved to.
     */
    protected final @NotNull String location;

    /**
     * Whether the event was emitted due to natural reasons, i. e. the event would be emitted even
     * with just SLAPI installed.
     */
    protected final boolean natural;

    /**
     * Constructor.
     *
     * @param location The location where the game state was saved to
     * @param natural Whether the event was not (indirectly or directly) caused by another mod
     */
    public GalaxySavingEndEvent(@NotNull String location, boolean natural) {
        this.location = Objects.requireNonNull(location, "location must not be null");
        this.natural = natural;
    }

    /**
     * Obtains the location where the savegame will be saved. The file will be relative to the current working directory
     * and may not exist at this point of time (as this event is called just before the game is actually saved).
     * It will likely be created later on.
     * This however may also equal to "unspecified" if {@link #isNatural()} is set to false,
     * as the {@link Galimulator#saveGameState(java.io.OutputStream)} does not control what is done with the savegame.
     *
     * @return The location of the save game
     */
    public @NotNull String getLocation() {
        return location;
    }

    /**
     * Whether the event was emitted due to natural reasons, i. e. the event would be emitted even
     * with just SLAPI installed.
     *
     * @return A boolean that describes whether the event can be considered natural
     */
    public boolean isNatural() {
        return natural;
    }
}
