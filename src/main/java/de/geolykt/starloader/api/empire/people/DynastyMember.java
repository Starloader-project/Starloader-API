package de.geolykt.starloader.api.empire.people;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.Identifiable;
import de.geolykt.starloader.api.empire.ActiveEmpire;
import de.geolykt.starloader.api.empire.Dateable;

/**
 * SL Wrapper interface for Persons. As usual, this class should not be implemented by anything but the Starloader implementation.
 * Dynasty members are guaranteed to have a job and are guaranteed to be a member of a dynasty.
 */
public interface DynastyMember extends Dateable, Identifiable {

    /**
     * {@inheritDoc}
     */
    @Override
    public default int getAge() {
        if (hasDied()) {
            return getDeathYear() - getFoundationYear();
        } else {
            return Galimulator.getGameYear() - getFoundationYear();
        }
    }

    /**
     * Obtains the time in which the individual was born in milliyears. (most often in years though - the game is confusing, I know)
     * This is functionally identical to {@link #getFoundationYear()}, though this one has a better naming and is just here
     * to avoid confusion.
     *
     * @return The age of the
     * @see #getDeathYear()
     * @see #getAge()
     */
    public default int getBirthYear() {
        return getFoundationYear();
    }

    /**
     * Gets the amount of children this member has.
     *
     * @return See above
     */
    public int getChildrenCount();

    /**
     * Obtains the reason to why as the member has died in a user-friendly version.
     *
     * @return The cause of the death, or null if the member is not dead
     */
    public @Nullable String getDeathReason();

    /**
     * Obtains the time of death in milliyears as specified by {@link Galimulator#getGameYear()}.
     * If {@link #hasDied()} returns false, then the return value is unspecified.
     *
     * @return The milliyear in which the member died
     */
    public int getDeathYear();

    /**
     * Gets the empire in which the member is currently active in.
     * After death this should return null, however it should not return null while the member is alive.
     *
     * @return The empire that employs this pawn
     */
    public @Nullable ActiveEmpire getEmpire();

    /**
     * Obtains the full name of the member.
     * This is the simple name of the member appended with the dynasty family name.
     *
     * @return The full name of the dynasty member
     */
    public @NotNull String getFullName();

    /**
     * Obtains the prestige of this member, which contributes to the merit of them which in turn helps them get better jobs.
     * This prestige also contributes to the dynasty prestige.
     *
     * @return The prestige of the member
     */
    public float getPrestige();

    /**
     * Whether the member is no longer contributing to society.
     * If this method returns false, then {@link #getAge()} should no longer increment
     *
     * @return True if the member is no longer alive
     */
    public boolean hasDied();
}
