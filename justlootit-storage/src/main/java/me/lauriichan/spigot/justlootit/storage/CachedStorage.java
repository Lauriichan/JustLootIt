package me.lauriichan.spigot.justlootit.storage;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public abstract class CachedStorage<S extends Storable> extends Storage<S> {

    private static final class Cached<S extends Storable> {
        
        private final CachedStorage<S> storage;

        private final long id;
        // Possibly use WeakReference instead?
        private volatile S object;
        
        private long timeoutTicks = 0;

        public Cached(final CachedStorage<S> storage, final S object) {
            this.storage = storage;
            this.timeoutTicks = storage.timeoutTicks;
            this.id = object.id();
            this.object = object;
        }
        
        public final S get() {
            if(object != null) {
                return object;
            }
            object = storage.read(id);
            if(object == null) {
                Cached<S> cached = storage.cache.get(id);
                if(cached == this) {
                    storage.cache.remove(id);
                }
            }
            return object;
        }
        
        // TODO: Call in timer
        public final void tick() {
            if(timeoutTicks == 0 || --timeoutTicks != 0) {
                return;
            }
            // Unload object
            object = null;
        }

    }

    protected final Long2ObjectOpenHashMap<Cached<S>> cache = new Long2ObjectOpenHashMap<>();
    protected final long timeoutTicks;
    
    // TODO: Add timer

    public CachedStorage(Class<S> baseType, final long timeoutTicks) {
        super(baseType);
        this.timeoutTicks = Math.max(timeoutTicks, 1);
    }
    
    public S get(long id) {
        Cached<S> cached = cache.get(id);
        if(cached == null) {
            S object = read(id);
            if(object == null) {
                return object;
            }
            cache.put(id, new Cached<>(this, object));
            return object;
        }
        return cached.get();
    }

}
