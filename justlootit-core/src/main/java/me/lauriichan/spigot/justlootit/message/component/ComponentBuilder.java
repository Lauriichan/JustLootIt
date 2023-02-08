package me.lauriichan.spigot.justlootit.message.component;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import net.md_5.bungee.api.chat.BaseComponent;

public final class ComponentBuilder extends SendableComponent {

    public static ComponentBuilder create() {
        return new ComponentBuilder();
    }

    private final ArrayList<Component> components = new ArrayList<>();

    private ComponentBuilder() {}

    public ComponentBuilder add(Component component) {
        components.add(Objects.requireNonNull(component));
        return this;
    }

    public ComponentBuilder remove(int index) {
        if (index < 0 || index >= components.size()) {
            return this;
        }
        components.remove(index);
        return this;
    }

    public Optional<Component> get(int index) {
        if (index < 0 || index >= components.size()) {
            return Optional.empty();
        }
        return Optional.of(components.get(index));
    }

    @Override
    public BaseComponent[] build() {
        if (this.components.isEmpty()) {
            return EMPTY;
        }
        int length = 0;
        Component[] components = this.components.toArray(Component[]::new);
        ArrayList<BaseComponent[]> messages = new ArrayList<>();
        for (int index = 0; index < components.length; index++) {
            BaseComponent[] message = components[index].build();
            messages.add(message);
            length += message.length;
        }
        BaseComponent[] output = new BaseComponent[length];
        int index = 0;
        for (BaseComponent[] message : messages) {
            System.arraycopy(message, 0, output, index, message.length);
            index += message.length;
        }
        return output;
    }

}