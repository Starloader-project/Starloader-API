package de.geolykt.starloader.impl;

import de.geolykt.starloader.api.gui.text.FormattedText;

import snoddasmannen.galimulator.TextBulletin;

@Deprecated(forRemoval = true, since = "2.0.0")
public class FormattedBulletinWrapper extends TextBulletin {

    private final FormattedText text;

    public FormattedBulletinWrapper(FormattedText text) {
        super(text.getText());
        this.text = text;
    }

    @Override
    public void drawAt(float x, float y) {
        text.renderText(x, y);
    }
}
