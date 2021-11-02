package de.geolykt.starloader.api.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.geolykt.starloader.DebugNagException;

// I'm open to a refractor for both efficiency and convenience
public final class EventManager {

    private EventManager() {
    } // The class should not be constructed

    private static final Logger LOGGER = LoggerFactory.getLogger(EventManager.class);

    private static final Map<Listener, List<@NotNull Method>> LISTENERS = new HashMap<>();

    private static boolean wasBuilt = false;

    /**
     * Registers an event listener if it was not yet registered and rebuilds if
     * needed.
     *
     * @param listener The {@link Listener} to add to the pool of active listeners
     */
    public static void registerListener(@NotNull Listener listener) {
        if (LISTENERS.containsKey(listener)) {
            return;
        }
        List<@NotNull Method> handlers = new ArrayList<>();
        Method[] methods = listener.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(EventHandler.class)) {
                if (method.getParameterCount() != 1) {
                    DebugNagException.nag("Invalid parameter count for event handler within listener!");
                    continue;
                }
                if (!Modifier.isPublic(method.getModifiers())) {
                    DebugNagException.nag("Inaccessible EventHandler within listener!");
                    continue;
                }
                handlers.add(method);
            }
        }
        LISTENERS.put(listener, handlers);
        if (wasBuilt) {
            rebuild();
        }
    }

    /**
     * Removes the listener from the active listener pool and rebuilds if needed.
     *
     * @param listener The {@link Listener} to remove
     */
    public static void unregisterListener(@NotNull Listener listener) {
        if (LISTENERS.containsKey(listener)) {
            LISTENERS.remove(listener);
            if (wasBuilt) {
                rebuild();
            }
        }
    }

    private static final List<Map<Class<?>, List<Map.Entry<Listener, Method>>>> EVENT_HANDLERS = new ArrayList<>(
            EventPriority.values().length);

    private static void rebuild() {
        wasBuilt = true;
        LOGGER.info("Rebuilding event tree");
        EVENT_HANDLERS.clear();
        for (EventPriority prio : EventPriority.values()) {
            EVENT_HANDLERS.add(prio.ordinal(), new HashMap<>());
        }
        LISTENERS.forEach((listener, handles) -> {
            for (Method handle : handles) {
                EventHandler info = handle.getDeclaredAnnotation(EventHandler.class);
                Class<?> clazz = handle.getParameters()[0].getType();
                while (Event.class.isAssignableFrom(clazz)) {
                    List<Map.Entry<Listener, Method>> eventHandles = EVENT_HANDLERS.get(info.value().ordinal())
                            .get(clazz);
                    if (eventHandles == null) {
                        eventHandles = new ArrayList<>();
                    }
                    eventHandles.add(new AbstractMap.SimpleImmutableEntry<>(listener, handle));
                    EVENT_HANDLERS.get(info.value().ordinal()).put(clazz, eventHandles);
                    clazz = clazz.getSuperclass();
                }
            }
        });
    }

    /**
     * Fires an event by passing it to all registered listeners.
     *
     * @param event The {@link Event} to pass
     */
    public static void handleEvent(@NotNull Event event) {
        if (!wasBuilt) {
            rebuild();
        }
        for (EventPriority prio : EventPriority.values()) {
            List<Map.Entry<Listener, Method>> methods = EVENT_HANDLERS.get(prio.ordinal()).get(event.getClass());
            if (methods == null) {
                continue;
            }
            for (Map.Entry<Listener, Method> method : methods) {
                try {
                    method.getValue().invoke(method.getKey(), event);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Fires an event by passing it to all registered listeners.
     * Unlike {@link #handleEvent(Event)}, this will method will stop
     * on encountering an exception or error and will return it.
     *
     * @param event The {@link Event} to pass
     * @return The caught throwable
     */
    public static @Nullable Throwable handleEventExcept(Event event) {
        if (!wasBuilt) {
            try {
                rebuild();
            } catch (Throwable t) {
                return t;
            }
        }
        for (EventPriority prio : EventPriority.values()) {
            List<Map.Entry<Listener, Method>> methods = EVENT_HANDLERS.get(prio.ordinal()).get(event.getClass());
            if (methods == null) {
                continue;
            }
            for (Map.Entry<Listener, Method> method : methods) {
                try {
                    method.getValue().invoke(method.getKey(), event);
                } catch (Throwable t) {
                    return t;
                }
            }
        }
        return null;
    }
}
