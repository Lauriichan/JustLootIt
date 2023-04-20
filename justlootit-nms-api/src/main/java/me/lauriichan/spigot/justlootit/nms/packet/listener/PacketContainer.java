package me.lauriichan.spigot.justlootit.nms.packet.listener;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.packet.AbstractPacket;

public final class PacketContainer {

    private final ISimpleLogger logger;

    private final ExecutorService mainService;
    private final ExecutorService asyncService;

    private final IPacketListener instance;
    private final List<PacketExecutor> executors;

    private boolean global = false;
    private final ArrayList<UUID> users = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final boolean acceptCancelled;

    public PacketContainer(final ISimpleLogger logger, final ExecutorService mainService, final ExecutorService asyncService,
        final IPacketListener instance) {
        this.logger = Objects.requireNonNull(logger);
        this.mainService = Objects.requireNonNull(mainService);
        this.asyncService = Objects.requireNonNull(asyncService);
        this.instance = Objects.requireNonNull(instance);
        final ArrayList<PacketExecutor> executors = new ArrayList<>();
        final Method[] methods = ClassUtil.getMethods(instance.getClass());
        boolean acceptCancelled = false;
        for (final Method method : methods) {
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            final PacketHandler handler = ClassUtil.getAnnotation(method, PacketHandler.class);
            if (handler == null) {
                continue;
            }
            final boolean receiveCancelled = handler.value();
            final PacketExecutor executor = PacketExecutor.analyze(method, receiveCancelled);
            if (executor == null) {
                continue;
            }
            executors.add(executor);
            if (receiveCancelled) {
                acceptCancelled = true;
            }
        }
        if (executors.isEmpty()) {
            throw new IllegalStateException("PacketContainer is not supposed to be empty!");
        }
        this.acceptCancelled = acceptCancelled;
        this.executors = Collections.unmodifiableList(executors);
    }

    public ISimpleLogger logger() {
        return logger;
    }

    public ExecutorService mainService() {
        return mainService;
    }

    public ExecutorService asyncService() {
        return asyncService;
    }

    public IPacketListener getInstance() {
        return instance;
    }

    public boolean doesAcceptCancelled() {
        return acceptCancelled;
    }

    public UUID[] getUsers() {
        lock.readLock().lock();
        try {
            return users.toArray(UUID[]::new);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean hasUser(final UUID id) {
        lock.readLock().lock();
        try {
            return users.contains(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean addUser(final UUID id) {
        if (hasUser(id)) {
            return false;
        }
        lock.writeLock().lock();
        try {
            return users.add(id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean removeUser(final UUID id) {
        if (!hasUser(id)) {
            return false;
        }
        lock.writeLock().lock();
        try {
            return users.remove(id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public PacketContainer setGlobal(final boolean global) {
        this.global = global;
        return this;
    }

    public boolean isGlobal() {
        return global;
    }

    boolean onPacket(final PlayerAdapter player, final AbstractPacket adapter, final boolean cancelled) {
        if (!global && !hasUser(player.getUniqueId())) {
            return false;
        }
        final Class<?> packetType = adapter.getClass();
        for (final PacketExecutor executor : executors) {
            if (!executor.getPacketType().isAssignableFrom(packetType) || cancelled && !executor.doesAllowCancelled()) {
                continue;
            }
            return executor.execute(this, player, adapter);
        }
        return false;
    }

}
