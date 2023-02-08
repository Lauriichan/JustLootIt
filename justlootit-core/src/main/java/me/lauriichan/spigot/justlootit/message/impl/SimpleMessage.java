package me.lauriichan.spigot.justlootit.message.impl;

import me.lauriichan.laylib.localization.IMessage;

public final class SimpleMessage implements IMessage {

    private final SimpleMessageProvider provider;
    private final String language;

    private String translation;

    public SimpleMessage(final SimpleMessageProvider provider, final String language) {
        this.provider = provider;
        this.language = language;
    }

    public void translation(String translation) {
        if (translation == null || translation.isBlank()) {
            this.translation = null;
            return;
        }
        this.translation = translation;
    }

    @Override
    public String id() {
        return provider.getId();
    }

    @Override
    public String language() {
        return language;
    }

    @Override
    public String value() {
        if (translation == null) {
            return provider.getFallback();
        }
        return translation;
    }

}
