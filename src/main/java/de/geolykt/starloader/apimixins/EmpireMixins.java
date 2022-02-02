package de.geolykt.starloader.apimixins;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Vector;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.geolykt.starloader.DebugNagException;
import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.GameConfiguration;
import de.geolykt.starloader.api.NamespacedKey;
import de.geolykt.starloader.api.NullUtils;
import de.geolykt.starloader.api.actor.ActorSpec;
import de.geolykt.starloader.api.empire.ActiveEmpire;
import de.geolykt.starloader.api.empire.Alliance;
import de.geolykt.starloader.api.empire.ShipCapacityModifier;
import de.geolykt.starloader.api.empire.ShipCapacityModifier.Type;
import de.geolykt.starloader.api.empire.Star;
import de.geolykt.starloader.api.event.EventManager;
import de.geolykt.starloader.api.event.TickCallback;
import de.geolykt.starloader.api.event.empire.EmpireRiotingEvent;
import de.geolykt.starloader.api.event.empire.EmpireSpecialAddEvent;
import de.geolykt.starloader.api.event.empire.EmpireSpecialRemoveEvent;
import de.geolykt.starloader.api.event.empire.EmpireStabiliseEvent;
import de.geolykt.starloader.api.event.empire.EmpireStateChangeEvent;
import de.geolykt.starloader.api.event.empire.EmpireTranscendEvent;
import de.geolykt.starloader.api.event.empire.TechnologyLevelDecreaseEvent;
import de.geolykt.starloader.api.event.empire.TechnologyLevelIncreaseEvent;
import de.geolykt.starloader.api.event.empire.TechnologyLevelSetEvent;
import de.geolykt.starloader.api.gui.BasicDialogBuilder;
import de.geolykt.starloader.api.gui.Drawing;
import de.geolykt.starloader.api.gui.Drawing.TextSize;
import de.geolykt.starloader.api.gui.FlagComponent;
import de.geolykt.starloader.api.gui.text.TextFactory;
import de.geolykt.starloader.api.registry.EmpireStateMetadataEntry;
import de.geolykt.starloader.api.registry.Registry;
import de.geolykt.starloader.api.registry.RegistryKeyed;
import de.geolykt.starloader.api.registry.RegistryKeys;

import snoddasmannen.galimulator.EmpireAchievement.EmpireAchievementType;
import snoddasmannen.galimulator.EmpireSpecial;
import snoddasmannen.galimulator.EmpireState;
import snoddasmannen.galimulator.GalColor;
import snoddasmannen.galimulator.Government;
import snoddasmannen.galimulator.Religion;
import snoddasmannen.galimulator.Space;
import snoddasmannen.galimulator.class_41;
import snoddasmannen.galimulator.actors.Flagship;
import snoddasmannen.galimulator.actors.StateActor;

@Mixin(snoddasmannen.galimulator.Empire.class)
public class EmpireMixins implements ActiveEmpire {

    /**
     * The last year in which the neutral empire was ticked.
     *
     * @deprecated This field only exist for redundancy checking for the {@link de.geolykt.starloader.api.event.TickEvent},
     * which is a class that is deprecated for removal.
     */
    @Deprecated(forRemoval = true, since = "1.5.0")
    private static transient int lastTick = -1;

    @Deprecated(forRemoval = true, since = "1.5.0")
    private static final void emitTick() {
        // Two layers of redundancy should be enough
        if (lastTick != Galimulator.getGameYear() && de.geolykt.starloader.api.event.TickEvent.tryAquireLock()) {
            EventManager.handleEvent(new de.geolykt.starloader.api.event.TickEvent());
            de.geolykt.starloader.api.event.TickEvent.releaseLock();
            lastTick = Galimulator.getGameYear();
        } else {
            DebugNagException.nag("Invalid, nested or recursive tick detected, skipping tick!"
                    + "This usually indicates a broken neutral empire");
        }
    }

    @SuppressWarnings("rawtypes")
    @Shadow
    private Vector agents; // actors - this is indeed the right name - i know, it is misleading

    @Shadow
    protected transient Random b; // internalRandom

    @Shadow
    int birthMilliYear; // foundationYear

    @Shadow
    public transient Deque<Star> c; // recentlyLost

    private transient ArrayList<ShipCapacityModifier> capModifiers;

    @Shadow
    GalColor color; // color

    @Shadow
    private int deathYear; // collapseYear

    private transient boolean flagNoModdedShipCapacity;

    @Shadow
    private Flagship flagship; // flagship

    @SuppressWarnings("rawtypes")
    @Shadow
    private ArrayList fleets; // fleets

    @Shadow
    private Government government;

    @Shadow
    private transient snoddasmannen.galimulator.Alliance h; // alliance

    @Shadow
    private transient float j; // averageWealth

    @Shadow
    public int id; // uniqueId

    @Shadow
    private int lastResearchedYear;

    @Shadow
    public int lastStateChange;

    private transient HashMap<NamespacedKey, Object> metadata;

    @Shadow
    private String motto; // motto

    @Shadow
    String name; // name

    @Shadow
    private Religion religion;

    @SuppressWarnings("rawtypes")
    @Shadow
    ArrayList specials; // empireSpecials

    @Shadow
    private int starCount; // starCount

    @Shadow
    private EmpireState state;

    @Shadow
    private int techLevel; // technologyLevel

    private transient List<TickCallback<ActiveEmpire>> tickCallbacks;

    /**
     * @param a  dummy doc
     */
    @Shadow
    public void a(EmpireAchievementType a) { // addAchievement
        return;
    }

    @Overwrite
    public void a(final EmpireState state) { // setState
        setState(((RegistryKeyed) (Object) state).getRegistryKey(), false);
    }

    @Shadow
    public void a(@NotNull Religion var0) { // setReligion
        religion = var0;
    }

    /**
     * @param var0  dummy doc
     */
    @Shadow
    public void a(StateActor var0) { // addActor
    }

    @Overwrite
    public void ab() { // Degenerate
        decreaseTechnologyLevel(true, false);
    }

    @Override
    public void addActor(@NotNull ActorSpec actor) {
        if (actor instanceof StateActor) {
            a((StateActor) actor);
        } else {
            throw new UnsupportedOperationException("Currently can only assign State Actors to empires.");
        }
    }

    @Override
    public void addActor(@NotNull StateActor actor) {
        a(actor);
    }

    @Override
    public void addCapacityModifier(@NotNull ShipCapacityModifier modifier) {
        if (capModifiers == null) {
            capModifiers = new ArrayList<>();
        }
        capModifiers.add(Objects.requireNonNull(modifier, "Tried to add null modifier!"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean addSpecial(@NotNull NamespacedKey empireSpecial, boolean force) {
        EmpireSpecial special = Registry.EMPIRE_SPECIALS.get(empireSpecial);
        if (special == null) {
            throw new IllegalArgumentException("No special is registered under the given key!");
        }
        if (hasSpecial(empireSpecial)) {
            return false;
        }
        if (!force) {
            EmpireSpecialAddEvent event = new EmpireSpecialAddEvent(this, empireSpecial);
            EventManager.handleEvent(event);
            if (event.isCancelled()) {
                return false;
            }
        }
        return specials.add(special);
    }

    @Override
    public void addTickCallback(TickCallback<ActiveEmpire> callback) {
        if (tickCallbacks == null) {
            tickCallbacks = new ArrayList<>();
        }
        tickCallbacks.add(callback);
    }

    @Inject(method = "ay", at = @At("TAIL"))
    public void applyShipModifiers(CallbackInfoReturnable<Double> ci) {
        if (flagNoModdedShipCapacity || capModifiers == null || capModifiers.isEmpty()) {
            flagNoModdedShipCapacity = false;
            return;
        }
        double baseVal = ci.getReturnValueD();
        double multiplier = 1.0;
        double add = 0.0;
        double addMultiplied = 0.0;
        double multiplierMultiplied = 1.0;
        for (ShipCapacityModifier mod : capModifiers) {
            if (mod.isMultiplicative()) {
                if (mod.getType() == Type.ADD) {
                    addMultiplied += mod.getValue();
                } else {
                    multiplierMultiplied *= mod.getValue();
                }
            } else {
                if (mod.getType() == Type.ADD) {
                    add += mod.getValue();
                } else {
                    multiplier += mod.getValue() - 1;
                }
            }
        }
        baseVal += addMultiplied;
        baseVal *= multiplier;
        baseVal *= multiplierMultiplied;
        baseVal += add;
        ci.setReturnValue(baseVal);
    }

    @Shadow
    public void av() { // voidTreaties
        return;
    }

    @Shadow
    public double ay() { // getShipCapcaity
        return 0.0;
    }

    @Shadow
    public void az() { // danceForJoy
        return;
    }

    @Shadow
    private void bc() { // Reset bonuses
        /*
         * this.specialsAttackBonus = null; this.specialsDefenseBonus = null;
         * this.specialsTechBonus = null; this.specialsIndustryBonus = null;
         * this.specialsStabilityBonus = null; this.specialsPeacefulBonus = null;
         */
    }

    @Overwrite
    public void b(final EmpireSpecial empireSpecial) { // removeSpecial
        if (!this.specials.contains(empireSpecial)) {
            return;
        }
        EmpireSpecialRemoveEvent event = new EmpireSpecialRemoveEvent(this,
                ((RegistryKeyed) empireSpecial).getRegistryKey());
        EventManager.handleEvent(event);
        if (event.isCancelled()) {
            return;
        }
        this.specials.remove(empireSpecial);
        this.bc();
        this.e();
    }

    /**
     * @param var0  dummy doc
     */
    @Shadow
    public void b(StateActor var0) { // removeActor
    }

    /**
     * Broadcasts the news in the galactic bulletin board, provided the empire is
     * known enough.
     *
     * @param news The news to broadcast
     */
    private void broadcastNews(String news) {
        if (this.Z()) {
            TextFactory factory = Drawing.getTextFactory();
            var name = factory.componentBuilder(getEmpireName()).setColor(getColor()).setSize(TextSize.MEDIUM).build();
            var newsComp = factory.componentBuilder(": " + news).setSize(TextSize.MEDIUM).build();
            Drawing.sendBulletin(factory.aggregateComponents(name, newsComp));
        }
    }

    @Overwrite
    @SuppressWarnings("unchecked")
    public void c(final EmpireSpecial empireSpecial) { // addSpecial
        if (!this.specials.contains(empireSpecial)) {
            EmpireSpecialAddEvent event = new EmpireSpecialAddEvent(this,
                    ((RegistryKeyed) empireSpecial).getRegistryKey());
            EventManager.handleEvent(event);
            if (event.isCancelled()) {
                return;
            }
            this.specials.add(empireSpecial);
        }
        this.bc();
        this.e();
    }

    @Override
    public boolean decreaseTechnologyLevel(boolean notify, boolean force) {
        if (getTechnologyLevel() <= 1) {
            return false;
        }

        if (!force) {
            TechnologyLevelDecreaseEvent event = new TechnologyLevelDecreaseEvent(this);
            EventManager.handleEvent(event);
            if (event.isCancelled()) {
                return false;
            }
        }

        techLevel--;
        lastResearchedYear = Galimulator.getGameYear();
        GameConfiguration config = Galimulator.getConfiguration();
        if (config.allowTranscendence() && techLevel == config.getTranscendceLevel()) {
            if (setState(RegistryKeys.GALIMULATOR_TRANSCENDING, false)) {
                if (notify && Galimulator.getPlayerEmpire() == this) {
                    new BasicDialogBuilder("Transcending!", NullUtils.requireNotNull(class_41.b().a("transcending"))).buildAndShow();
                }
            }
        }
        if (!notify) {
            return true;
        }

        broadcastNews("Has degenerated, now tech level: " + getTechnologyLevel());
        if (this == Galimulator.getPlayerEmpire()) {
            new BasicDialogBuilder("Technologicy lost", "You have degenerated to tech level: " + this.techLevel
                    + ". You kindly ask your scientists to make back-ups next time.").buildAndShow();
        }
        return true;
    }

    @Shadow
    public void e() { // No idea what this does
    //    this.g = null;
    }

    @SuppressWarnings({ "unchecked", "null" })
    @Override
    public @NotNull Vector<StateActor> getActors() {
        return agents;
    }

    @Override
    public Alliance getAlliance() {
        return (Alliance) h;
    }

    @Override
    @Deprecated(forRemoval = true, since = "1.5.0")
    public @NotNull Color getAWTColor() {
        return ((de.geolykt.starloader.impl.AWTColorAccesor) getColor()).asAWTColor();
    }

    @Override
    public double getBaseShipCapacity() {
        flagNoModdedShipCapacity = true;
        return ay();
    }

    @Override
    public @NotNull List<ShipCapacityModifier> getCapcityModifiers() {
        if (capModifiers == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(capModifiers);
    }

    @Override
    public float getCapitalX() {
        return ((snoddasmannen.galimulator.Empire) (Object) this).getCoordinates().x;
    }

    @Override
    public float getCapitalY() {
        return ((snoddasmannen.galimulator.Empire) (Object) this).getCoordinates().y;
    }

    @Override
    public int getCollapseYear() {
        return deathYear;
    }

    @SuppressWarnings("null")
    @Override
    public @NotNull GalColor getColor() {
        return color;
    }

    @SuppressWarnings("null")
    @Override
    public @NotNull String getEmpireName() {
        return name;
    }

    @SuppressWarnings({ "null", "unchecked" })
    @Override
    @NotNull
    public Collection<? extends FlagComponent> getFlag() {
        return Collections.unmodifiableCollection(((snoddasmannen.galimulator.Empire) (Object) this).n());
    }

    @Override
    public Flagship getFlagship() {
        return flagship;
    }

    @SuppressWarnings({ "unchecked", "null" })
    @Override
    public @NotNull ArrayList<snoddasmannen.galimulator.Fleet> getFleets() {
        return fleets;
    }

    @Override
    public int getFoundationYear() {
        return birthMilliYear;
    }

    @SuppressWarnings("null")
    @Override
    public com.badlogic.gdx.graphics.@NotNull Color getGDXColor() {
        return getColor().getGDXColor();
    }

    @SuppressWarnings("null")
    @Override
    public @NotNull Random getInternalRandom() {
        return b;
    }

    @Override
    public @Nullable Object getMetadata(@NotNull NamespacedKey key) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        return metadata.get(key);
    }

    @SuppressWarnings("null")
    @Override
    public @NotNull String getMotto() {
        return motto;
    }

    @Override
    public @NotNull Collection<Star> getRecentlyLostStars() {
        return new ArrayList<>(c);
    }

    @SuppressWarnings("null")
    @Override
    public @NotNull Religion getReligion() {
        return religion;
    }

    @Override
    public double getShipCapacity() {
        return ay();
    }

    @SuppressWarnings({ "unchecked", "null" })
    @Override
    public @NotNull Vector<ActorSpec> getSLActors() {
        return agents;
    }

    @Override
    public int getStarCount() {
        return starCount;
    }

    @Override
    public @NotNull NamespacedKey getState() {
        return ((RegistryKeyed) (Object) state).getRegistryKey();
    }

    @Override
    public int getTechnologyLevel() {
        return techLevel;
    }

    @Override
    public int getUID() {
        return id;
    }

    @Override
    public float getWealth() {
        return j;
    }

    @Override
    public boolean hasCollapsed() {
        return getCollapseYear() != -1;
    }

    @Override
    public boolean hasKey(@NotNull NamespacedKey key) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        return metadata.containsKey(key);
    }

    @Override
    public boolean hasSpecial(@NotNull NamespacedKey empireSpecial) {
        EmpireSpecial special = Registry.EMPIRE_SPECIALS.get(empireSpecial);
        if (special == null) {
            return false; // For extension cooperation
        }
        return specials.contains(special);
    }

    @Override
    public boolean increaseTechnologyLevel(boolean notify, boolean force) {
        if (techLevel >= 999) { // implementation is capped there
            return false;
        }

        if (!force) {
            TechnologyLevelIncreaseEvent event = new TechnologyLevelIncreaseEvent(this);
            EventManager.handleEvent(event);
            if (event.isCancelled()) {
                return false;
            }
        }

        techLevel++;
        lastResearchedYear = Galimulator.getGameYear();
        GameConfiguration config = Galimulator.getConfiguration();
        if (config.allowTranscendence() && techLevel == config.getTranscendceLevel()) {
            if (setState(RegistryKeys.GALIMULATOR_TRANSCENDING, false)) {
                if (notify && Galimulator.getPlayerEmpire() == this) {
                    new BasicDialogBuilder("Transcending!", NullUtils.requireNotNull(class_41.b().a("transcending"))).buildAndShow();
                }
            }
        }
        if (!notify) {
            return true;
        }

        broadcastNews("Has advanced, now tech level: " + getTechnologyLevel());
        this.a(EmpireAchievementType.RESEARCHED);
        if (Galimulator.getPlayerEmpire() == this) {
            new BasicDialogBuilder("Technological advance", "You have advanced to tech level: " + this.techLevel
                    + "! This will give your armies and fleets a significant boost.").buildAndShow();
        }
        return true;
    }

    @Override
    public void removeActor(@NotNull ActorSpec actor) {
        if (actor instanceof StateActor) {
            b((StateActor) actor);
        } else {
            return; // The actor cannot be assigned to the agents list, which we can skip this removal.
        }
    }

    @Override
    public void removeActor(@NotNull StateActor actor) {
        b(actor);
    }

    @Override
    public void removeCapacityModifier(@NotNull ShipCapacityModifier modifier) {
        if (capModifiers == null) {
            return; // nothing to remove
        }
        capModifiers.remove(Objects.requireNonNull(modifier, "Tried to remove null modifier!"));
    }

    @Override
    public boolean removeSpecial(@NotNull NamespacedKey empireSpecial, boolean force) {
        EmpireSpecial special = Registry.EMPIRE_SPECIALS.get(empireSpecial);
        if (special == null) {
            throw new IllegalArgumentException("No special is registered under the given key!");
        }
        if (!force) {
            EmpireSpecialRemoveEvent event = new EmpireSpecialRemoveEvent(this, empireSpecial);
            EventManager.handleEvent(event);
            if (event.isCancelled()) {
                return false;
            }
        }
        return specials.remove(special);
    }

    @Override
    public void setAlliance(@Nullable Alliance alliance) {
        h = (snoddasmannen.galimulator.Alliance) alliance;
    }

    @Override
    public void setInternalRandom(@NotNull Random random) {
        b = random;
    }

    @Override
    public void setMetadata(@NotNull NamespacedKey key, @Nullable Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        if (value == null) {
            metadata.remove(key);
        } else {
            metadata.put(key, value);
        }
    }

    @Override
    public void setMotto(@NotNull String motto) {
        this.motto = motto;
    }

    @Override
    public void setRecentlyLostStars(@NotNull Deque<Star> stars) {
        c = NullUtils.requireNotNull(stars);
    }

    @Override
    public void setReligion(@NotNull Religion religion) {
        if (Objects.isNull(religion) && this != Galimulator.getNeutralEmpire()) {
            throw new NullPointerException("religion cannot be null.");
        }
        a(religion);
    }

    @Override
    public boolean setState(@NotNull NamespacedKey stateKey, boolean force) {
        EmpireState state = Registry.EMPIRE_STATES.get(stateKey);
        if (state == null) {
            throw new IllegalArgumentException("The given registry key is not valid!");
        }
        if (state == this.state) {
            return false; // no change occurred. Nothing needs to be changed
        }
        EmpireStateMetadataEntry stateMeta = Registry.EMPIRE_STATES.getMetadataEntry(stateKey);
        if (stateMeta == null) {
            throw new IllegalStateException("Unable to find empire state metadata entry, possible registry corruption.");
        }
        if (!force) {
            EmpireStateChangeEvent event = null;
            if (stateMeta.isStable()) {
                EmpireStateMetadataEntry statemeta = Registry.EMPIRE_STATES.getMetadataEntry(getState());
                if (statemeta == null) {
                    throw new NullPointerException("Internal error occoured while obtaining the metadata entry of an empire state.");
                }
                if (!statemeta.isStable()) {
                    event = new EmpireStabiliseEvent(this, stateKey);
                } else {
                    event = new EmpireStateChangeEvent(this, stateKey);
                }
            } else if (stateKey == RegistryKeys.GALIMULATOR_TRANSCENDING) {
                event = new EmpireTranscendEvent(this);
            } else if (stateKey == RegistryKeys.GALIMULATOR_RIOTING) {
                event = new EmpireRiotingEvent(this);
            } else {
                event = new EmpireStateChangeEvent(this, stateKey);
            }
            EventManager.handleEvent(event);
            if (event.isCancelled()) {
                return false; // No change due to event cancellation
            }
        }
        this.state = state;
        this.lastStateChange = Galimulator.getGameYear();
        broadcastNews("Is now " + state.toString());
        if (stateMeta.isWarmongering()) {
            this.av(); // Diplomatic relations are reset as the empires will go to war
            if (stateKey == RegistryKeys.GALIMULATOR_ALL_WILL_BE_ASHES) {
                // AWBA does not believe in development, and as such development is reset within
                // it
                for (Star star : Galimulator.getStars()) {
                    if (star.getAssignedEmpire() == this) {
                        ((snoddasmannen.galimulator.Star) star).d(0); // resets the development within the empire
                    }
                }
            } else if (stateKey == RegistryKeys.GALIMULATOR_CRUSADING && getEmpireName().contains("Spain")) {
                // Easter egg
                Drawing.sendBulletin("Nobody expects the Spanish inquisition!");
            }
        } else if (stateKey == RegistryKeys.GALIMULATOR_TRANSCENDING) {
            this.az(); // dance for joy
        }
        return true;
    }

    @Inject(method = "b(I)V", at = @At(value = "HEAD"), cancellable = true)
    public void setTechlevel(final int techLevel, final CallbackInfo ci) {
        if (techLevel == getTechnologyLevel()) {
            return;
        }
        TechnologyLevelSetEvent event = new TechnologyLevelSetEvent(this, techLevel);
        EventManager.handleEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
            return;
        }
    }

    @Override
    public @NotNull ActiveEmpire spawnOffspring(@NotNull Star location) {
        snoddasmannen.galimulator.Star star = (snoddasmannen.galimulator.Star) location;
        return (ActiveEmpire) NullUtils.requireNotNull(Space.a((snoddasmannen.galimulator.Empire) (Object) this, star));
    }

    /**
     * Mixin callback. Cannot be called directly at runtime
     *
     * @param info Unused, but required by mixins
     */
    @Inject(method = "J()V", at = @At(value = "HEAD"), cancellable = false)
    public void tick(CallbackInfo info) {
        if (this == Galimulator.getNeutralEmpire()) {
            emitTick();
        }
        if (tickCallbacks == null) {
            tickCallbacks = new ArrayList<>();
        }
        for (TickCallback<ActiveEmpire> callback : tickCallbacks) {
            callback.tick(this);
        }
    }

    @Shadow
    public boolean Z() { // isNoteable
        return false;
    }

    @Overwrite
    public void aa() { // advance
        increaseTechnologyLevel(true, false);
    }
}
