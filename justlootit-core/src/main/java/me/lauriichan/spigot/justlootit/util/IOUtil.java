package me.lauriichan.spigot.justlootit.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public final class IOUtil {
    
    private IOUtil() {
        throw new UnsupportedOperationException();
    }

    public static void delete(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        if (!Files.isDirectory(path)) {
            Files.delete(path);
            return;
        }
        Iterator<Path> iter = list(path);
        while (iter.hasNext()) {
            delete(iter.next());
        }
        Files.delete(path);
    }

    public static Iterator<Path> list(Path path) throws IOException {
        final Iterator<Path> delegate = Files.newDirectoryStream(path).iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                try {
                    return delegate.hasNext();
                } catch (DirectoryIteratorException e) {
                    throw new UncheckedIOException(e.getCause());
                }
            }

            @Override
            public Path next() {
                try {
                    return delegate.next();
                } catch (DirectoryIteratorException e) {
                    throw new UncheckedIOException(e.getCause());
                }
            }
        };
    }

}
