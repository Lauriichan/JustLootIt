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

import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.packet.AbstractPacket;

public final class PacketContainer {

    private final ExecutorService mainService;
    private final ExecutorService asyncService;

    private final IPacketListener instance;
    private final List<PacketExecutor> executors;

    private boolean global = false;
    private final ArrayList<UUID> users = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final boolean acceptCancelled;

    public PacketContainer(final ExecutorService mainService, final ExecutorService asyncService, final IPacketListener instance) {
        this.mainService = Objects.requireNonNull(mainService);
        this.asyncService = Objects.requireNonNull(asyncService);
        this.instance = Objects.requireNonNull(instance);
        ArrayList<PacketExecutor> executors = new ArrayList<>();
        Method[] methods = ClassUtil.getMethods(instance.getClass());
        boolean acceptCancelled = false;
        for (Method method : methods) {
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            PacketHandler handler = ClassUtil.getAnnotation(method, PacketHandler.class);
            if (handler == null) {
                continue;
            }
            boolean receiveCancelled = handler.value();
            PacketExecutor executor = PacketExecutor.analyze(method, receiveCancelled);
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

    public boolean hasUser(UUID id) {
        lock.readLock().lock();
        try {
            return users.contains(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean addUser(UUID id) {
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

    public boolean removeUser(UUID id) {
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

    public PacketContainer setGlobal(boolean global) {
        this.global = global;
        return this;
    }

    public boolean isGlobal() {
        return global;
    }

    final boolean onPacket(PlayerAdapter player, AbstractPacket adapter, boolean cancelled) {
        if (!global && !hasUser(player.getUniqueId())) {
            return false;
        }
        Class<?> packetType = adapter.getClass();
        for (PacketExecutor executor : executors) {
            if (!executor.getPacketType().isAssignableFrom(packetType) || (cancelled && !executor.doesAllowCancelled())) {
                continue;
            }
            return executor.execute(this, player, adapter);
        }
        return false;
    }

}
