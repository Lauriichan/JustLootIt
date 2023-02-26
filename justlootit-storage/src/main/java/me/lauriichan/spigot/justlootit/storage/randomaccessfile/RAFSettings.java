package me.lauriichan.spigot.justlootit.storage.randomaccessfile;

/**
 * Settings for the {@link RAFStorage}
 */
public final class RAFSettings {
    
    public static final int DEFAULT_VALUES_PER_FILE = 1024;
    public static final int DEFAULT_COPY_BUFFER_BYTES = 64;
    
    public static final RAFSettings DEFAULT = new RAFSettings(DEFAULT_VALUES_PER_FILE, DEFAULT_COPY_BUFFER_BYTES);

    public static final int LOOKUP_AMOUNT_SIZE = Short.BYTES;
    public static final int LOOKUP_ENTRY_SIZE = Long.BYTES;

    public static final int VALUE_HEADER_ID_SIZE = Short.BYTES;
    public static final int VALUE_HEADER_LENGTH_SIZE = Integer.BYTES;
    public static final int VALUE_HEADER_SIZE = VALUE_HEADER_ID_SIZE + VALUE_HEADER_LENGTH_SIZE;

    public static final long INVALID_HEADER_OFFSET = 0L;

    public final int copyBufferSize;

    public final int valueIdBits;
    public final int valueIdMask;
    public final int valueIdAmount;

    public final int lookupHeaderSize;

    /**
     * @param valuesPerFile   the amount of values that should fit into on file
     *                            (will be rounded up to the next power of two if
     *                            this isn't a power of two)
     * @param copyBufferBytes the amount of bytes that the copy buffer for shrinking
     *                            and expanding the file should use
     *
     */
    public RAFSettings(int valuesPerFile, int copyBufferBytes) {
        this.copyBufferSize = Math.max(copyBufferBytes * 1024, 1024);
        this.valueIdBits = Integer.bitCount(valuesPerFile - 1);
        this.valueIdMask = -1 >> (Integer.SIZE - valueIdBits);
        this.valueIdAmount = valueIdMask + 1;
        this.lookupHeaderSize = valueIdAmount * LOOKUP_ENTRY_SIZE + LOOKUP_AMOUNT_SIZE;
    }

}
