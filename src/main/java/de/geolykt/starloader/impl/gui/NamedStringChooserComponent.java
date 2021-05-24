package de.geolykt.starloader.impl.gui;

import java.util.Objects;
import java.util.Vector;

import org.jetbrains.annotations.NotNull;

import de.geolykt.starloader.api.gui.Drawing;
import de.geolykt.starloader.api.gui.modconf.StrictStringOption;
import de.geolykt.starloader.api.gui.modconf.StringChooseOption;
import de.geolykt.starloader.api.gui.modconf.StringOption;
import de.geolykt.starloader.api.gui.screen.Screen;
import de.geolykt.starloader.api.gui.screen.ScreenComponent;

import snoddasmannen.galimulator.hk;

public class NamedStringChooserComponent extends hk implements ScreenComponent {

    protected final Screen parent;
    protected final StringOption option;

    public NamedStringChooserComponent(@NotNull Screen parent, @NotNull StringOption option) {
        // Irrelevant  / name / currentValue / options / category / Irrelevant
        super(null, option.getName(), option.get(), getOptions(option), option.getParent().getName(), null);
        this.parent = Objects.requireNonNull(parent);
        this.option = option;
    }

    protected static Vector<Object> getOptions(StringOption option) {
        final Vector<Object> options = new Vector<>(option.getRecommendedValues());
        if (!(option instanceof StrictStringOption)) {
            options.add("Custom");
        }
        return options;
    }

    public void a(final String o) {
        if ("Custom".equals(o.toString()) && !(option instanceof StringChooseOption)) {
            var builder = Drawing.textInputBuilder("Change value of setting", option.get(), option.getName());
            if (option instanceof StrictStringOption) {
                builder.addHook(text -> {
                    if (((StrictStringOption) option).isValid(text)) {
                        option.set(text);
                        getParentScreen().markDirty();
                    } else {
                        Drawing.toast("The input value is not valid!");
                    }
                });
            } else {
                builder.addHook(text -> {
                    option.set(text);
                    getParentScreen().markDirty();
                });
            }
            builder.build();
            return;
        }
        option.set(o.toString());
    }

    @Override
    public @NotNull Screen getParentScreen() {
        return parent;
    }
}
