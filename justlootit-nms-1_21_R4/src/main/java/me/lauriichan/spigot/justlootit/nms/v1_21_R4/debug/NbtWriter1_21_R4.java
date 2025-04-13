package me.lauriichan.spigot.justlootit.nms.v1_21_R4.debug;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public final class NbtWriter1_21_R4 extends Writer {

    public static String serialize(Tag tag) {
        return serialize(tag, true);
    }

    public static String serialize(Tag tag, boolean pretty) {
        StringWriter stringWriter = new StringWriter();
        NbtWriter1_21_R4 writer = new NbtWriter1_21_R4(stringWriter, pretty);
        try {
            writer.writeTag(tag);
            writer.endLn();
            writer.flush();
            writer.close();
        } catch (IOException e) {
        }
        return stringWriter.toString();
    }

    private static final String NEWLINE = System.getProperty("line.separator"), INDENT = "    ";

    private static final Pattern SIMPLE_STRING = Pattern.compile("[A-Za-z0-9._+-]+");

    private final Writer writer;
    private final boolean pretty;

    private int indent = 0;

    public NbtWriter1_21_R4(Writer writer, boolean pretty) {
        this.writer = Objects.requireNonNull(writer);
        this.pretty = pretty;
    }

    public NbtWriter1_21_R4(Writer writer) {
        this(writer, false);
    }

    public void writeNamedTag(String name, Tag root) throws IOException {
        if (!name.isEmpty()) {
            write(escapeString(name));
            write(':');
            if (pretty) {
                write(' ');
            }
        }
        writeTag(root);
    }

    public void writeTag(Tag tag) throws IOException {
        if (!pretty) {
            write(tag.toString());
            return;
        }

        byte type = tag.getId();
        if (type == 0 || isPrimitive(type) || isArray(type)) {
            writer.write(tag.toString());
        } else if (type == 10) {
            writeCompound((CompoundTag) tag);
        } else if (type == 9) {
            writeList((ListTag) tag);
        } else {
            throw new AssertionError(type);
        }
    }

    private void writeCompound(CompoundTag compound) throws IOException {
        if (!pretty) {
            write(compound.toString());
            return;
        }

        write('{');

        if (!compound.isEmpty()) {
            boolean simple = isPrimitive(compound);

            if (!simple) {
                indent++;
                endLn();
            }

            Set<String> keys = compound.keySet();
            boolean first = true;

            if (simple) {
                for (String key : keys) {
                    if (first) {
                        first = false;
                    } else {
                        write(", ");
                    }
                    write(SIMPLE_STRING.matcher(key).matches() ? key : escapeString(key));
                    write(": ");
                    writeTag(compound.get(key));
                }
            } else {
                for (String key : keys) {
                    if (first) {
                        first = false;
                    } else {
                        write(",");
                        endLn();
                    }
                    write(SIMPLE_STRING.matcher(key).matches() ? key : escapeString(key));
                    write(": ");
                    writeTag(compound.get(key));
                }
            }

            if (!simple) {
                indent--;
                endLn();
            }
        }

        write('}');
    }

    private void writeList(ListTag list) throws IOException {
        if (!pretty) {
            write(list.toString());
            return;
        }

        write('[');

        if (!list.isEmpty()) {
            boolean simple = isPrimitive(list);
            if (!simple) {
                indent++;
                endLn();
            }
            boolean first = true;
            if (simple) {
                for (Tag tag : list) {
                    if (first) {
                        first = false;
                    } else {
                        write(", ");
                    }
                    writeTag(tag);
                }
            } else {
                for (Tag tag : list) {
                    if (first) {
                        first = false;
                    } else {
                        write(",");
                        endLn();
                    }
                    writeTag(tag);
                }
            }
            if (!simple) {
                indent--;
                endLn();
            }
        }

        write(']');
    }

    protected void indent() throws IOException {
        if (indent == 1) {
            writer.write(INDENT);
        } else if (indent > 0) {
            for (int i = 0; i < indent; i++) {
                writer.append(INDENT);
            }
        }
    }

    protected void endLn() throws IOException {
        writer.write(NEWLINE);
        indent();
    }

    // WRITER IMPL

    @Override
    public void write(int c) throws IOException {
        writer.write(c);
    }

    @Override
    public void write(String str) throws IOException {
        writer.write(str);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        writer.write(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    // UTIL

    private static boolean isPrimitive(byte type) {
        return (type >= 1 && type <= 6) || type == 8;
    }

    private static boolean isArray(byte type) {
        return type == 7 || type > 10;
    }

    private static boolean isPrimitive(ListTag tag) {
        return tag.isEmpty() || isPrimitive(tag.identifyRawElementType());
    }

    private static boolean isPrimitive(CompoundTag compound) {
        if (compound.isEmpty()) {
            return true;
        }
        for (String key : compound.keySet()) {
            Tag tag = compound.get(key);
            if (tag == null) {
                return false;
            }
            if (!isPrimitive(tag.getId())) {
                return false;
            }
        }
        return true;
    }

    private static String escapeString(String str) {
        StringBuilder builder = new StringBuilder("\"");
        char[] chars = str.toCharArray();
        for (char c : chars) {
            if ((c == '\\') || (c == '"')) {
                builder.append('\\');
            }
            builder.append(c);
        }
        return builder.append('\"').toString();
    }

}
