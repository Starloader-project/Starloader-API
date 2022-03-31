package de.geolykt.starloader.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Vector;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.Main;

import de.geolykt.starloader.ExpectedObfuscatedValueException;
import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.Map;
import de.geolykt.starloader.api.NamespacedKey;
import de.geolykt.starloader.api.NullUtils;
import de.geolykt.starloader.api.actor.ActorSpec;
import de.geolykt.starloader.api.actor.WeaponsManager;
import de.geolykt.starloader.api.empire.ActiveEmpire;
import de.geolykt.starloader.api.empire.Alliance;
import de.geolykt.starloader.api.empire.Star;
import de.geolykt.starloader.api.empire.War;
import de.geolykt.starloader.api.empire.people.DynastyMember;
import de.geolykt.starloader.api.event.EventManager;
import de.geolykt.starloader.api.event.lifecycle.GalaxySavingEndEvent;
import de.geolykt.starloader.api.event.lifecycle.GalaxySavingEvent;
import de.geolykt.starloader.api.gui.Dynbind;
import de.geolykt.starloader.api.gui.MapMode;
import de.geolykt.starloader.api.registry.Registry;
import de.geolykt.starloader.api.resource.DataFolderProvider;
import de.geolykt.starloader.api.sound.SoundHandler;

import snoddasmannen.galimulator.DeviceConfiguration;
import snoddasmannen.galimulator.EmploymentAgency;
import snoddasmannen.galimulator.GalFX;
import snoddasmannen.galimulator.MapMode.MapModes;
import snoddasmannen.galimulator.Player;
import snoddasmannen.galimulator.Religion;
import snoddasmannen.galimulator.Space;
import snoddasmannen.galimulator.SpaceState;
import snoddasmannen.galimulator.VanityHolder;
import snoddasmannen.galimulator.guides.class_0;

public class GalimulatorImplementation implements Galimulator.GameImplementation, Galimulator.Unsafe {

    /**
     * The logger that is used within this class.
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(GalimulatorImplementation.class);

    /**
     * Whether to suppress any events that are generated by {@link #saveGameState(OutputStream)}.
     */
    public static boolean suppressSaveEvent = false;

    /**
     * Converts a Galimulator map mode into a starloader API map mode.
     * This is a clean cast and should never throw exception, except if there is an issue unrelated to this method.
     *
     * @param mode The map mode to convert
     * @return The converted map mode
     */
    private static @NotNull MapMode toSLMode(@NotNull MapModes mode) {
        return (MapMode) (Object) mode;
    }

    /**
     * Converts a Galimulator map mode into a starloader API map mode.
     * This is a clean cast and should never throw exception, except if there is an issue unrelated to this method.
     * This is the nullable alternative to {@link #toSLMode(MapModes)} and only the annotations have changed.
     *
     * @param mode The map mode to convert
     * @return The converted map mode
     */
    private static @Nullable MapMode toSLModeNullable(@Nullable MapModes mode) {
        return (MapMode) (Object) mode;
    }

    @Override
    public void connectStars(@NotNull Star starA, @NotNull Star starB) {
        starA.addNeighbour(starB);
        starB.addNeighbour(starA);
    }

    @Override
    public void disconnectStars(@NotNull Star starA, @NotNull Star starB) {
        starA.removeNeighbour(starB);
        starB.removeNeighbour(starA);
    }

    @SuppressWarnings("null")
    @Override
    public @NotNull MapMode getActiveMapmode() {
        return toSLMode(snoddasmannen.galimulator.MapMode.getCurrentMode());
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Vector<ActorSpec> getActorsUnsafe() {
        return NullUtils.requireNotNull((Vector) Space.actors);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Vector<Alliance> getAlliancesUnsafe() {
        return NullUtils.requireNotNull((Vector) Space.alliances);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Vector<?> getArtifactsUnsafe() {
        return NullUtils.requireNotNull((Vector) Space.artifacts);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Vector<?> getCooperationsUnsafe() {
        return NullUtils.requireNotNull((Vector) Space.corporations);
    }

    @Override
    public Vector<Star> getDisruptedStarsUnsafe() {
        return NullUtils.requireNotNull(Space.disruptedStars);
    }

    @Override
    public @Nullable ActiveEmpire getEmpireByUID(int uid) {
        return (ActiveEmpire) Space.e(uid);
    }

    @Override
    @Deprecated(forRemoval = true, since = "1.5.0")
    public @Nullable ActiveEmpire getEmpirePerUID(int uid) {
        return getEmpireByUID(uid);
    }

    @SuppressWarnings({ "null" })
    @Override
    public @NotNull List<@NotNull ActiveEmpire> getEmpires() {
        return getEmpiresUnsafe(); // TODO change this to a clone after the spec permits us that
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Vector<ActiveEmpire> getEmpiresUnsafe() {
        return NullUtils.requireNotNull((Vector) Space.empires);
    }

    @SuppressWarnings({ "unchecked", "rawtypes", "null" })
    @Override
    @NotNull
    public Vector<DynastyMember> getFollowedPeopleUnsafe() {
        return (Vector) Space.t;
    }

    @Override
    public int getGameYear() {
        return Space.getMilliYear();
    }

    @SuppressWarnings("null")
    @Override
    public @NotNull Map getMap() {
        return (Map) Space.getMapData();
    }

    @Override
    public @Nullable MapMode getMapmodeByKey(@NotNull NamespacedKey key) {
        return toSLModeNullable(Registry.MAP_MODES.get(key));
    }

    @SuppressWarnings("null")
    @Override
    public @NotNull MapMode[] getMapModes() {
        return (MapMode[]) (Object[]) Registry.MAP_MODES.getValues();
    }

    @SuppressWarnings("null")
    @Override
    public @NotNull ActiveEmpire getNeutralEmpire() {
        return (ActiveEmpire) Space.neutralEmpire;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Vector<DynastyMember> getPeopleUnsafe() {
        return NullUtils.requireNotNull((Vector) Space.getPersons());
    }

    public @Nullable snoddasmannen.galimulator.Player getPlayer() {
        return Space.getPlayer();
    }

    @Override
    public @Nullable ActiveEmpire getPlayerEmpire() {
        snoddasmannen.galimulator.Player plyr = getPlayer();
        if (plyr == null) {
            // It likely can never be null, however before the map is generated,
            // this might return null, so we are going to make sure just in case.
            return null;
        }
        return (ActiveEmpire) plyr.getEmpire();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Vector<?> getQuestsUnsafe() {
        return NullUtils.requireNotNull((Vector) Space.quests);
    }

    @Override
    public @NotNull SoundHandler getSoundHandler() {
        return SLSoundHandler.getInstance();
    }

    @SuppressWarnings({ "null" })
    @Override
    public @NotNull List<@NotNull Star> getStars() {
        return getStarsUnsafe();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Vector<Star> getStarsUnsafe() {
        return NullUtils.requireNotNull((Vector) Space.stars);
    }

    @Override
    public int getTranscendedEmpires() {
        return Space.getTranscended();
    }

    @Override
    @Deprecated(forRemoval = false, since = "1.5.0")
    public Galimulator.@NotNull Unsafe getUnsafe() {
        return this;
    }

    /**
     * Obtains the currently valid vanity holder instance.
     *
     * @return The valid vanity holder instance
     */
    public VanityHolder getVanityHolder() {
        return Space.vanity;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Vector<War> getWarsUnsafe() {
        return NullUtils.requireNotNull((Vector) Space.wars);
    }

    @Override
    public @NotNull WeaponsManager getWeaponsManager() {
        return SLWeaponsManager.getInstance();
    }

    @Override
    public boolean hasUsedSandbox() {
        return Space.sandboxUsed;
    }

    @Override
    public boolean isPaused() {
        return Space.isPaused();
    }

    @Override
    public synchronized void loadGameState(byte[] data) throws IOException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
            loadGameState(in);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public synchronized void loadGameState(@NotNull InputStream input) throws IOException {
        Object readObject;
        try (ObjectInputStream in = new ObjectInputStream(input)) {
            try {
                readObject = in.readObject();
            } catch (ClassNotFoundException | IOException e) {
                throw new IOException("Failed to read savegame.", e);
            }
            if (!(readObject instanceof SpaceState)) {
                throw new IOException("The read object was not the excepted obect.");
            }
        }
        SpaceState spaceState = (SpaceState) readObject;
        Space.history = spaceState.history;
        setMap((Map) NullUtils.requireNotNull(spaceState.mapData));
        setGameYear(spaceState.milliYear);
        setNeutralEmpire(NullUtils.requireNotNull((ActiveEmpire) spaceState.neutralEmpire));
        setPlayer(spaceState.player);
        setUsedSandbox(spaceState.sandboxUsed);
        setTranscendedEmpires(spaceState.transcended);
        setWarsUnsafe((Vector) spaceState.wars);
        setVanityHolder(spaceState.vanity);
        setActorsUnsafe(NullUtils.requireNotNull((Vector) spaceState.actors));
        setAlliancesUnsafe(NullUtils.requireNotNull((Vector) spaceState.alliances));
        setArtifactsUnsafe(NullUtils.requireNotNull((Vector<?>) spaceState.artifacts));
        setCooperationsUnsafe(NullUtils.requireNotNull((Vector<?>) spaceState.corporations));
        setDisruptedStarsUnsafe(NullUtils.requireNotNull((Vector<Star>) spaceState.disruptedStars));
        setEmpiresUnsafe(NullUtils.requireNotNull((Vector) spaceState.empires));
        setPeopleUnsafe(NullUtils.requireNotNull((Vector) spaceState.persons));
        setQuestsUnsafe(NullUtils.requireNotNull((Vector<?>) spaceState.quests));
        setStarsUnsafe(NullUtils.requireNotNull((Vector) spaceState.stars));
        Space.H = spaceState.stars.size();
        EmploymentAgency.a(spaceState.employmentAgency);

        // Many magic methods and stuff. See Space#i(String) (as of galimulator-4.9-STABLE)
        Space.o = null;
        Space.C = true;

        HashMap<Integer, Star> uidToStar = new HashMap<>();
        HashMap<Integer, ActiveEmpire> uidToEmpire = new HashMap<>();

        for (Star star : getStarsUnsafe()) {
            star.setInternalRandom(new Random());
            uidToStar.put(star.getUID(), star);
        }

        for (ActiveEmpire empire : getEmpiresUnsafe()) {
            empire.setRecentlyLostStars(new ArrayDeque<>());
            empire.setInternalRandom(new Random());
            uidToEmpire.put(empire.getUID(), empire);
        }

        getNeutralEmpire().setInternalRandom(new Random());
        getNeutralEmpire().setRecentlyLostStars(new ArrayDeque<>());
        @SuppressWarnings("null")
        @NotNull
        final Religion nullReligion = (Religion) NullUtils.provideNull();
        getNeutralEmpire().setReligion(nullReligion);
        Space.ar(); // probably sets up the background effects. Accesses the LET_IT_SNOW setting as well as creating AmbientStarEffect among others
        Space.getMapData().getGenerator().i(); // Change the xmax and ymax of the generator area
        Space.ao(); // big calculations with voronoi diagrams
        Space.ak = Space.q(); // set the width/height of the board
        Space.al = Space.r();

        // repopulate the starlanes (this was extracted from another method)
        // Also sets the owner empire, which was also extracted from another method
        for (Star star : getStarsUnsafe()) {
            Vector<Star> neighbours = new Vector<>();
            for (Integer starB : star.getNeighbourIDs()) {
                neighbours.add(uidToStar.get(starB));
            }
            star.setNeighbours(neighbours);
            ActiveEmpire owner = uidToEmpire.get(star.getAssignedEmpireUID());
            if (owner == null) {
                owner = getNeutralEmpire();
            }
            star.setAssignedEmpire(owner);
        }

        Space.am(); // setup quad trees
        if (getAlliancesUnsafe() == null) {
            setAlliancesUnsafe(new Vector<>());
        } else {
            for (Alliance alliance : getAlliancesUnsafe()) {
                for (ActiveEmpire member : alliance.getMembers()) {
                    member.setAlliance(alliance);
                }
            }
        }

        Vector<DynastyMember> followedMembers = new Vector<>();
        for (DynastyMember member : getPeopleUnsafe()) {
            if (member.isFollowed()) {
                followedMembers.add(member);
            }
        }
        setFollowedPeopleUnsafe(followedMembers);
        class_0.b();

        Space.getMapData().getGenerator().n();
        GalFX.l.zoom = GalFX.e();
        GalFX.l.update();
    }

    @Override
    public void pauseGame() {
        Space.setPaused(true);
    }

    @Override
    public void recalculateVoronoiGraphs() {
        Space.ao();
    }

    @Override
    @Deprecated(forRemoval = true, since = "1.3.0")
    public void registerKeybind(de.geolykt.starloader.api.gui.@NotNull Keybind bind) {
        Objects.requireNonNull(bind, "the parameter \"bind\" must not be null");
        if (bind.getCharacter() != '\0') {
            Main.shortcuts.add(new SLKeybind(bind, bind.getCharacter()));
        } else {
            String desc = bind.getKeycodeDescription();
            if (desc == null) {
                throw new IllegalArgumentException("The keycode description of the argument is null!");
            }
            Main.shortcuts.add(new SLKeybind(bind, desc, bind.getKeycode()));
        }
    }

    @Override
    public void registerKeybind(@NotNull Dynbind bind) {
        Objects.requireNonNull(bind, "the parameter \"bind\" must not be null");
        Main.shortcuts.add(new SLDynbind(bind));
    }

    @Override
    public void resumeGame() {
        Space.setPaused(false);
    }

    @Override
    public void saveFile(@NotNull String name, byte[] data) {
        File out = new File(DataFolderProvider.getProvider().provideAsFile(), NullUtils.requireNotNull(name));
        if (!out.exists()) {
            try (FileOutputStream fos = new FileOutputStream(out)) {
                fos.write(data);
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void saveFile(@NotNull String name, InputStream data) {
        File out = new File(DataFolderProvider.getProvider().provideAsFile(), NullUtils.requireNotNull(name));
        if (!out.exists()) {
            try (FileOutputStream fos = new FileOutputStream(out)) {
                data.transferTo(fos);
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @NotNull
    @Contract(pure = true)
    private final SpaceState createState() {
        return new SpaceState((Vector) getStarsUnsafe(),
                (Vector) getEmpiresUnsafe(),
                (Vector) getArtifactsUnsafe(),
                (Vector) getActorsUnsafe(),
                (Vector) getDisruptedStarsUnsafe(),
                (snoddasmannen.galimulator.Empire) getNeutralEmpire(),
                getGameYear(),
                getTranscendedEmpires(),
                getVanityHolder(),
                (Vector) getQuestsUnsafe(),
                getPlayer(), 
                Space.getMapData(),
                hasUsedSandbox(),
                snoddasmannen.galimulator.EmploymentAgency.a(),
                (Vector) getPeopleUnsafe(),
                Space.history,
                (List) null,
                (Vector) getAlliancesUnsafe(),
                (Vector) getCooperationsUnsafe(),
                (Vector) getWarsUnsafe());
    }

    @Override
    public void saveGameState(@NotNull OutputStream out) {
        if (!suppressSaveEvent) {
            EventManager.handleEvent(new GalaxySavingEvent("Programmer issued save", "unspecified", false));
        }
        Space.G = 0; // reset Stack depth
        SpaceState var2 = createState();
        if (DeviceConfiguration.getConfiguration().useXStream()) {
            LOGGER.warn("XStream is not supported for saving directly.");
        }
        try {
            ObjectOutputStream var5 = new ObjectOutputStream(out);
            var5.writeObject(var2);
            var5.close();
            out.close();
        } catch (Throwable var6) {
            if (!suppressSaveEvent) {
                EventManager.handleEvent(new GalaxySavingEndEvent("unspecified", false));
            }
            throw new RuntimeException("Issue during serialisation.", var6);
        }
        if (!suppressSaveEvent) {
            EventManager.handleEvent(new GalaxySavingEndEvent("unspecified", false));
        }
    }

    @Override
    public void setActiveMapmode(@NotNull MapMode mode) {
        snoddasmannen.galimulator.MapMode.setCurrentMode(ExpectedObfuscatedValueException.requireMapMode(mode));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setActorsUnsafe(Vector<ActorSpec> actors) {
        Space.actors = NullUtils.requireNotNull((Vector) actors);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setAlliancesUnsafe(Vector<Alliance> alliances) {
        Space.alliances = NullUtils.requireNotNull((Vector) alliances);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setArtifactsUnsafe(Vector<?> artifacts) {
        Space.artifacts = NullUtils.requireNotNull((Vector) artifacts);
    }

    @SuppressWarnings("rawtypes")
    @Override
    // TODO rename to corporations - old name is a typo
    public void setCooperationsUnsafe(Vector<?> cooperations) {
        Space.corporations = NullUtils.requireNotNull((Vector) cooperations);
    }

    @Override
    public void setDisruptedStarsUnsafe(Vector<Star> disruptedStars) {
        Space.disruptedStars = NullUtils.requireNotNull(disruptedStars);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setEmpiresUnsafe(Vector<ActiveEmpire> empires) {
        Space.empires = NullUtils.requireNotNull((Vector) empires);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setFollowedPeopleUnsafe(@NotNull Vector<DynastyMember> people) {
        Space.t = NullUtils.requireNotNull((Vector) people);
    }

    @Override
    public void setGameYear(int year) {
        Space.milliYear = year;
    }

    @Override
    public void setMap(@NotNull Map map) {
        if (!(map instanceof snoddasmannen.galimulator.MapData)) {
            throw new ExpectedObfuscatedValueException();
        }
        Space.mapData = (snoddasmannen.galimulator.MapData) map;
    }

    @Override
    public void setNeutralEmpire(@NotNull ActiveEmpire empire) {
        Space.neutralEmpire = ExpectedObfuscatedValueException.requireEmpire(NullUtils.requireNotNull(empire));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setPeopleUnsafe(Vector<DynastyMember> members) {
        Space.persons = NullUtils.requireNotNull((Vector) members);
    }

    public void setPlayer(Player player) {
        Space.player = player;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setQuestsUnsafe(Vector<?> quests) {
        Space.quests = NullUtils.requireNotNull((Vector) quests);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setStarsUnsafe(Vector<Star> stars) {
        Space.stars = NullUtils.requireNotNull((Vector) stars);
    }

    @Override
    public void setTranscendedEmpires(int count) {
        Space.transcended = count;
    }

    @Override
    public void setUsedSandbox(boolean state) {
        Space.sandboxUsed = state;
    }

    /**
     * Sets the valid vanity holder instance that dictates the vanity names to use.
     *
     * @param holder The vanity holder to use
     */
    public void setVanityHolder(VanityHolder holder) {
        Space.vanity = holder;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setWarsUnsafe(Vector<War> wars) {
        Space.wars = NullUtils.requireNotNull((Vector) wars);
    }
}
