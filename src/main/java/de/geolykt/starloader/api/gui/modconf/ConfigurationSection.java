package de.geolykt.starloader.api.gui.modconf;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import de.geolykt.starloader.api.NullUtils;

/**
 * A section within the configuration dialog.
 * It is basically the container for the individual configuration settings.
 *
 * @since 1.3.0
 */
public interface ConfigurationSection {

    /**
     * Creates and registers a configuration option that stores a boolean with the given parameters.
     * The name of the configuration option may be duplicated with another config option,
     * independent of section and type, however this is not encouraged as this may confuse a few people.
     *
     * @param name          The user-friendly name returned by {@link ConfigurationOption#getName()}
     * @param currentValue  The currently active value set for the option
     * @param defaultValue  The default value set for the option
     * @return The newly registered option
     * @since 1.3.0
     */
    public @NotNull BooleanOption addBooleanOption(@NotNull String name, boolean currentValue, boolean defaultValue);

    /**
     * Creates and registers a floating-point value option with the given parameters.
     * The name of the configuration option may be duplicated with another config option,
     * independent of section and type, however this is not encouraged as this may confuse a few people.
     *
     * @param name          The user-friendly name returned by {@link ConfigurationOption#getName()}
     * @param currentValue  The currently active value set for the option
     * @param defaultValue  The default value set for the option
     * @param min           The minimum value acceptable for the option (inclusive)
     * @param max           The maximum value acceptable for the option (inclusive)
     * @param recommended   The recommended values that are shown to the user.
     * @return The newly registered option
     * @since 1.3.0
     */
    public @NotNull FloatOption addFloatOption(@NotNull String name, float currentValue, float defaultValue,
            float min, float max, @NotNull Collection<@NotNull Float> recommended);

    /**
     * Creates and registers an integer-value option with the given parameters.
     * The name of the configuration option may be duplicated with another config option,
     * independent of section and type, however this is not encouraged as this may confuse a few people.
     *
     * @param name          The user-friendly name returned by {@link ConfigurationOption#getName()}
     * @param currentValue  The currently active value set for the option
     * @param defaultValue  The default value set for the option
     * @param min           The minimum value acceptable for the option (inclusive)
     * @param max           The maximum value acceptable for the option (inclusive)
     * @param recommended   The recommended values that are shown to the user.
     * @return The newly registered option
     * @since 1.3.0
     */
    public @NotNull IntegerOption addIntegerOption(@NotNull String name, int currentValue, int defaultValue,
            int min, int max, @NotNull Collection<@NotNull Integer> recommended);

    /**
     * Registers a custom option to this section. This is useful if the developer has custom get/set logic
     * or when the interface that is wanted is not implemented by the SLAPI (such as {@link IntegerChooseOption}.
     *
     * @param option The option to add
     * @since 1.3.0
     */
    public void addOption(@NotNull ConfigurationOption<?> option);

    /**
     * Creates and registers an option which holds a String with the given parameters.
     * The name of the configuration option may be duplicated with another config option,
     * independent of section and type, however this is not encouraged as this may confuse a few people.
     *
     * @param name          The user-friendly name returned by {@link ConfigurationOption#getName()}
     * @param currentValue  The currently active value set for the option
     * @param defaultValue  The default value set for the option
     * @param recommended   The recommended values that are shown to the user.
     * @return The newly registered option
     * @since 1.3.0
     */
    public @NotNull StringOption addStringOption(@NotNull String name, @NotNull String currentValue,
            @NotNull String defaultValue, @NotNull Collection<@NotNull String> recommended);

    /**
     * Creates and registers an option which holds a String with the given parameters.
     * Other than {@link #addStringOption(String, String, String, Collection)} the return value does not
     * accept arbitrary strings and must pass the given test.
     * The name of the configuration option may be duplicated with another config option,
     * independent of section and type, however this is not encouraged as this may confuse a few people.
     *
     * @param name          The user-friendly name returned by {@link ConfigurationOption#getName()}
     * @param currentValue  The currently active value set for the option
     * @param defaultValue  The default value set for the option
     * @param test          The test that the potential values must pass when the option value is changed.
     * @param recommended   The recommended values that are shown to the user.
     * @return The newly registered option
     * @since 1.3.0
     */
    @NotNull
    public StrictStringOption addStringOption(@NotNull String name, @NotNull String currentValue,
            @NotNull String defaultValue, @NotNull Predicate<@NotNull String> test, @NotNull Collection<@NotNull String> recommended);

    /**
     * Creates and registers an option which holds a String with the given parameters.
     * Other than {@link #addStringOption(String, String, String, Collection)} the return value does not
     * accept arbitrary strings and must pass the given test.
     * The name of the configuration option may be duplicated with another config option,
     * independent of section and type, however this is not encouraged as this may confuse a few people.
     *
     * <p>This method only recommends the default value. Outside of recommendations, this method has no notable difference
     * to {@link #addStringOption(String, String, String, Predicate, Collection)}.
     *
     * @param name          The user-friendly name returned by {@link ConfigurationOption#getName()}
     * @param currentValue  The currently active value set for the option
     * @param defaultValue  The default value set for the option
     * @param test          The test that the potential values must pass when the option value is changed.
     * @return The newly registered option
     * @since 2.0.0
     */
    @NotNull
    public default StrictStringOption addCustomStringOption(@NotNull String name, @NotNull String currentValue,
            @NotNull String defaultValue, @NotNull Predicate<@NotNull String> test) {
        return this.addStringOption(name, currentValue, defaultValue, test, NullUtils.asList(defaultValue, "Custom"));
    }

    /**
     * Obtains the child options that are assigned to this section.
     *
     * @return A {@link List} of {@link ConfigurationOption} registered to this section.
     * @since 1.3.0
     */
    public @NotNull List<@NotNull ConfigurationOption<?>> getChildren();

    /**
     * Obtains the name (header) of the section.
     *
     * @return The name of the section
     * @since 1.3.0
     */
    public @NotNull String getName();
}
