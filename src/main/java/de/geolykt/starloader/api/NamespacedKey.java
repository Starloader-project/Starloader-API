package de.geolykt.starloader.api;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.geolykt.starloader.mod.Extension;

public class NamespacedKey {

    /**
     * Obtains a {@link NamespacedKey} from a string, practically undoing {@link NamespacedKey#toString()}.
     * The string must be in the format of "namespace:key"
     *
     * @return The {@link NamespacedKey} that was parsed from the string.
     * @since 2.0.0
     */
    @SuppressWarnings("null")
    @NotNull
    public static NamespacedKey fromString(String string) {
        int colonIndex = string.indexOf(':');
        if (colonIndex == -1) {
            throw new IllegalArgumentException(string + " is not a valid namespaced key. Namespaced keys must contain a colon");
        }
        return new NamespacedKey(string.substring(0, colonIndex), string.substring(colonIndex + 1));
    }

    /**
     * Creates a namespaced key based on two strings. Practically exposing {@link #NamespacedKey(String, String)} to the public.
     *
     * @return The {@link NamespacedKey} that was parsed from the strings.
     * @since 2.0.0
     */
    @SuppressWarnings("null")
    @NotNull
    public static NamespacedKey fromString(String namespace, String key) {
        return new NamespacedKey(namespace, key);
    }

    @NotNull
    private final String keyString;

    @Nullable
    private final Extension namespaceNamesake;

    private String namespaceString; // initialised in a lazy manner

    public NamespacedKey(@NotNull Extension namespace, @NotNull String key) {
        namespaceNamesake = namespace;
        namespaceString = null; // this is lazy as the empire description is only provided later on after a plugin was made.
        keyString = key;
    }

    protected NamespacedKey(@NotNull String namespace, @NotNull String key) {
        namespaceString = namespace;
        keyString = key;
        namespaceNamesake = null;
    }

    /**
     * Called to calculate the namespace string.
     * This is a workaround to a flaw within the starloader extensions structure.
     */
    private void calculateNamespace() {
        Extension a = namespaceNamesake;
        if (a == null) {
            throw new IllegalStateException("Both namespace and it's namesake is null.");
        }
        if (Objects.isNull(a.getDescription())) {
            throw new IllegalStateException("The descriptor of the namesake is not yet initialized."
                    + "Consider using the namespaced key in the init block of the extension.");
        }
        namespaceString = a.getDescription().getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof NamespacedKey) {
            if (namespaceString == null) {
                calculateNamespace();
            }
            return keyString.equals(((NamespacedKey) obj).keyString)
                    && namespaceString.equals(((NamespacedKey) obj).namespaceString);
        } else {
            return false;
        }
    }

    /**
     * Obtains the key part of the namespaced key.
     *
     * @return The key of this key
     */
    @NotNull
    public String getKey() {
        return keyString;
    }

    @SuppressWarnings("null")
    @NotNull
    public String getNamespace() {
        if (namespaceString == null) {
            calculateNamespace();
        }
        return namespaceString;
    }

    @Override
    public int hashCode() {
        if (namespaceString == null) {
            calculateNamespace();
        }
        return Objects.hash(namespaceString, keyString);
    }

    public boolean matches(@NotNull Extension namespace, @NotNull String key) {
        if (namespaceString == null) {
            calculateNamespace();
        }
        return keyString.equals(key) && namespaceString.equals(namespace.getDescription().getName());
    }

    public boolean matches(@NotNull String namespace, @NotNull String key) {
        if (namespaceString == null) {
            calculateNamespace();
        }
        return namespaceString.equals(namespace) && keyString.equals(key);
    }

    @Override
    public String toString() {
        return this.getNamespace() + ":" + this.keyString;
    }
}
