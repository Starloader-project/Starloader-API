package de.geolykt.starloader.apimixins;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.NullUtils;
import de.geolykt.starloader.api.empire.Empire;
import de.geolykt.starloader.api.empire.War;

import snoddasmannen.galimulator.Lazy.EmpireLazy;

@Mixin(snoddasmannen.galimulator.War.class)
public class WarMixins implements War {

    @Shadow
    int conqueredStarBalance;

    @Shadow
    EmpireLazy e1;

    @Shadow
    EmpireLazy e2;

    @Shadow
    int lastAction;

    @Shadow
    int shipsDestroyed;

    @Shadow
    int startYear;

    @Override
    public int getDateOfLastAction() {
        return lastAction;
    }

    @Override
    public int getDestroyedShips() {
        return shipsDestroyed;
    }

    @Override
    public @NotNull Empire getEmpireA() {
        return NullUtils.requireNotNull(Galimulator.getEmpirePerUID(e1.d()), "Couldn't determine empire from lazy object. Perhaps it was disbanded already?");
    }

    @Override
    public @NotNull Empire getEmpireB() {
        return NullUtils.requireNotNull(Galimulator.getEmpirePerUID(e2.d()), "Couldn't determine empire from lazy object. Perhaps it was disbanded already?");
    }

    @Override
    public int getStarDelta() {
        return conqueredStarBalance;
    }

    @Override
    public int getStartDate() {
        return startYear;
    }

    @Override
    public void noteShipDestruction() {
        shipsDestroyed++;
    }

    @Override
    public void noteStarChange(@NotNull Empire empire) throws IllegalArgumentException {
        int uid = empire.getUID();
        if (e1.d() == uid) {
            conqueredStarBalance++;
        } else if (e2.d() == uid) {
            conqueredStarBalance--;
        } else {
            throw new IllegalArgumentException("The given empire matches no participant.");
        }
    }

    @Override
    public void setDestroyedShips(int count) {
        shipsDestroyed = count;
    }

    @Override
    public void setStarDelta(int count) {
        conqueredStarBalance = count;
    }
}
