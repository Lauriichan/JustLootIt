package me.lauriichan.spigot.justlootit.storage;

public final class StorageMigrationFailedException extends StorageException {

    private static final long serialVersionUID = -8543752837139689144L;

    public StorageMigrationFailedException(String message) {
        super(message);
    }

    public StorageMigrationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
