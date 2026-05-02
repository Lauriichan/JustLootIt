package me.lauriichan.spigot.justlootit.util;

import org.bukkit.NamespacedKey;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectCollections;
import it.unimi.dsi.fastutil.objects.ObjectList;

public class CategorizedKeyMap implements Comparable<CategorizedKeyMap> {

    private final CategorizedKeyMap root;
    private final CategorizedKeyMap parent;

    private final Object2ObjectOpenHashMap<String, CategorizedKeyMap> children = new Object2ObjectOpenHashMap<>();
    private final String path, name;

    public CategorizedKeyMap() {
        this.path = "";
        this.name = "Root";
        this.root = this;
        this.parent = null;
    }

    private CategorizedKeyMap(CategorizedKeyMap parent, String key) {
        this.path = parent.path().isEmpty() ? key : parent.path() + '/' + key;
        this.name = key;
        this.root = parent.root();
        this.parent = parent;
    }

    public CategorizedKeyMap root() {
        return root;
    }

    public CategorizedKeyMap parent() {
        return parent;
    }

    public String path() {
        return path;
    }

    public String name() {
        return name;
    }

    public CategorizedKeyMap get(String path) {
        String[] segments = path.split("/");
        CategorizedKeyMap map = this;
        for (int i = 0; i < segments.length; i++) {
            CategorizedKeyMap tmp = map.children.get(segments[i]);
            if (tmp == null) {
                return map;
            }
            map = tmp;
        }
        return map;
    }

    public boolean isKey() {
        return children.isEmpty();
    }

    public int childrenCount() {
        return children.size();
    }

    public ObjectCollection<CategorizedKeyMap> children() {
        return ObjectCollections.unmodifiable(children.values());
    }

    public ObjectList<CategorizedKeyMap> childrenList() {
        return new ObjectArrayList<>(children.values());
    }

    public void add(NamespacedKey key) {
        add(key.getNamespace(), key.getKey());
    }

    public void add(String namespace, String key) {
        add(root.addMap(namespace), key);
    }

    public void add(String key) {
        add(root, key);
    }

    private void add(CategorizedKeyMap map, String key) {
        String[] segments = key.split("/");
        for (int i = 0; i < segments.length; i++) {
            map = map.addMap(segments[i]);
        }
    }

    private CategorizedKeyMap addMap(String name) {
        CategorizedKeyMap map = children.get(name);
        if (map == null) {
            map = new CategorizedKeyMap(this, name);
            children.put(name, map);
            return map;
        }
        return map;
    }

    @Override
    public int compareTo(CategorizedKeyMap o) {
        int tmp = Boolean.compare(o.isKey(), isKey());
        if (tmp != 0) {
            return tmp;
        }
        return name.compareTo(o.name);
    }

}
