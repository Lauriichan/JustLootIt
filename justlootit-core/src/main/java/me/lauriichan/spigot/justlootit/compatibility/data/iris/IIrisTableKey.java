package me.lauriichan.spigot.justlootit.compatibility.data.iris;

import org.bukkit.NamespacedKey;

public interface IIrisTableKey extends Comparable<IIrisTableKey> {

    String identifier();

    record VanillaTableKey(NamespacedKey key) implements IIrisTableKey {

        @Override
        public String identifier() {
            return key.toString();
        }

        @Override
        public int compareTo(IIrisTableKey o) {
            if (o instanceof VanillaTableKey) {
                return 0;
            }
            return -1;
        }

    }

    record IrisTableKey(String key) implements IIrisTableKey {

        @Override
        public String identifier() {
            return key;
        }

        @Override
        public int compareTo(IIrisTableKey o) {
            if (o instanceof IrisTableKey) {
                return 0;
            }
            return 1;
        }

    }

}
