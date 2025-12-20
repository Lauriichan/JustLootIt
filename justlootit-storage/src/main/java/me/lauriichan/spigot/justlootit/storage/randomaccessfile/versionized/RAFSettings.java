package me.lauriichan.spigot.justlootit.storage.randomaccessfile.versionized;

import me.lauriichan.spigot.justlootit.storage.randomaccessfile.IRAFSettings;

/**
 * Settings for the {@link RAFLegacyMultiStorage}
 */
public final class RAFSettings implements IRAFSettings {
    
    public static final record MigrationSettings(int valuesPerFileBeforeV1) {}
    
    static final record MigrationSupport(int valueIdBitsBeforeV1) {}

    public static final class Builder {

        private MigrationSupport migrationSupport;
        private int valuesPerFile = DEFAULT_VALUES_PER_FILE;
        private int copyBufferBytes = DEFAULT_COPY_BUFFER_BYTES;
        private long fileCacheTicks = DEFAULT_FILE_CACHE_TICKS;
        private long fileCachePurgeStep = DEFAULT_FILE_CACHE_PURGE_STEP;
        private int fileCacheMaxAmount = DEFAULT_FILE_CHANNEL_MAX_AMOUNT;

        private Builder() {}
        
        /**
         * Sets up migration support for RAF files.
         * 
         * @param  settings the migration settings for the migration support
         *
         * @return          the same builder
         */
        public Builder migrationSupport(MigrationSettings settings) {
            int valueIdAmount = Math.max(Integer.highestOneBit((settings.valuesPerFileBeforeV1 - 1) & 0xFFFF), 1) << 1;
            int valueIdMask = valueIdAmount - 1;
            int valueIdBits = Integer.bitCount(valueIdMask);
            this.migrationSupport = new MigrationSupport(valueIdBits);
            return this;
        }

        /**
         * Sets the amount of values that should be written to one file
         * 
         * The value specified will be rounded up to the next power of two if the value
         * itself isn't a power of two
         * 
         * Max support value is 2^16 (65536)
         * 
         * @param  valuesPerFile the value to set
         *
         * @return               the same builder
         */
        public Builder valuesPerFile(final int valuesPerFile) {
            this.valuesPerFile = valuesPerFile;
            return this;
        }

        /**
         * Sets the amount of kilobytes that the should be used to move data inside a
         * file when shrinking or expanding it
         * 
         * @param  copyBufferBytes the value to set
         *
         * @return                 the same builder
         */
        public Builder copyBufferBytes(final int copyBufferBytes) {
            this.copyBufferBytes = copyBufferBytes;
            return this;
        }

        /**
         * Sets the amount of ticks that a file channel can stay in cache while being
         * unused
         * 
         * @param  fileCacheTicks the value to set
         *
         * @return                the same builder
         */
        public Builder fileCacheTicks(final long fileCacheTicks) {
            this.fileCacheTicks = fileCacheTicks;
            return this;
        }

        /**
         * Sets the amount of ticks that should be subtracted from the
         * {@code fileCacheTicks} before each purge step when purging file channels from
         * cache
         * 
         * @param  fileCachePurgeStep the value to set
         *
         * @return                    the same builder
         */
        public Builder fileCachePurgeStep(final long fileCachePurgeStep) {
            this.fileCachePurgeStep = fileCachePurgeStep;
            return this;
        }

        /**
         * Sets the maximal amount of concurrently cached file channels
         * 
         * @param  fileCacheMaxAmount the value to set
         *
         * @return                    the same builder
         */
        public Builder fileCacheMaxAmount(final int fileCacheMaxAmount) {
            this.fileCacheMaxAmount = fileCacheMaxAmount;
            return this;
        }

        public RAFSettings build() {
            return new RAFSettings(migrationSupport, valuesPerFile, copyBufferBytes, fileCacheTicks, fileCachePurgeStep, fileCacheMaxAmount);
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    public static final int DEFAULT_VALUES_PER_FILE = 1024;
    public static final int DEFAULT_COPY_BUFFER_BYTES = 2048;

    public static final long DEFAULT_FILE_CACHE_TICKS = 180;
    public static final long DEFAULT_FILE_CACHE_PURGE_STEP = 10;

    public static final int DEFAULT_FILE_CHANNEL_MAX_AMOUNT = 64;

    public static final RAFSettings DEFAULT = new RAFSettings(null, DEFAULT_VALUES_PER_FILE, DEFAULT_COPY_BUFFER_BYTES,
        DEFAULT_FILE_CACHE_TICKS, DEFAULT_FILE_CACHE_PURGE_STEP, DEFAULT_FILE_CHANNEL_MAX_AMOUNT);

    public static final int FORMAT_VERSION = Short.BYTES;
    public static final int LOOKUP_TABLE_BITS_SIZE = Byte.BYTES;

    public static final int LOOKUP_AMOUNT_SIZE = Short.BYTES;
    public static final int LOOKUP_ENTRY_SIZE = Long.BYTES;

    public static final int LOOKUP_AMOUNT_OFFSET = FORMAT_VERSION + LOOKUP_TABLE_BITS_SIZE;
    public static final int LOOKUP_ENTRY_BASE_OFFSET = LOOKUP_AMOUNT_OFFSET + LOOKUP_AMOUNT_SIZE;

    public static final int VALUE_HEADER_ID_SIZE = Short.BYTES;
    public static final int VALUE_HEADER_VERSION_SIZE = Short.BYTES;
    public static final int VALUE_HEADER_LENGTH_SIZE = Integer.BYTES;

    public static final int VALUE_HEADER_ID_VERSION_SIZE = VALUE_HEADER_ID_SIZE + VALUE_HEADER_VERSION_SIZE;
    public static final int VALUE_HEADER_SIZE = VALUE_HEADER_ID_SIZE + VALUE_HEADER_VERSION_SIZE + VALUE_HEADER_LENGTH_SIZE;

    public static final long INVALID_HEADER_OFFSET = 0L;

    public final int copyBufferSize;

    public final int valueIdBits;
    public final int valueIdMask;
    public final int valueIdAmount;

    public final int lookupHeaderSize;

    public final long fileCacheTicks;
    public final long fileCachePurgeStep;

    public final int fileCacheMaxAmount;
    
    public final MigrationSupport migrationSupport;

    private RAFSettings(final MigrationSupport migrationSupport, final int valuesPerFile, final int copyBufferBytes, final long fileCacheTicks, final long fileCachePurgeStep,
        final int fileCacheMaxAmount) {
        this.migrationSupport = migrationSupport;
        this.copyBufferSize = Math.max(copyBufferBytes * 1024, 1024);
        this.valueIdAmount = Math.max(Integer.highestOneBit((valuesPerFile - 1) & 0xFFFF), 1) << 1;
        this.valueIdMask = valueIdAmount - 1;
        this.valueIdBits = Integer.bitCount(valueIdMask);
        this.lookupHeaderSize = valueIdAmount * LOOKUP_ENTRY_SIZE + LOOKUP_ENTRY_BASE_OFFSET;
        this.fileCacheTicks = Math.max(fileCacheTicks, 30);
        this.fileCachePurgeStep = Math.max(fileCachePurgeStep, 1);
        this.fileCacheMaxAmount = Math.max(fileCacheMaxAmount, 4);
    }

    @Override
    public int valueIdBits() {
        return valueIdBits;
    }

    @Override
    public int valueIdMask() {
        return valueIdMask;
    }

    @Override
    public int valueIdAmount() {
        return valueIdAmount;
    }

    @Override
    public long fileCacheTicks() {
        return fileCacheTicks;
    }

    @Override
    public int fileCacheMaxAmount() {
        return fileCacheMaxAmount;
    }

    @Override
    public long fileCachePurgeStep() {
        return fileCachePurgeStep;
    }

}
