package me.lauriichan.spigot.justlootit.message.impl;

import me.lauriichan.laylib.localization.source.IProviderFactory;

public final class SimpleMessageProviderFactory implements IProviderFactory {

    @Override
    public SimpleMessageProvider build(String id, String fallback) {
        return new SimpleMessageProvider(id, fallback);
    }

}
