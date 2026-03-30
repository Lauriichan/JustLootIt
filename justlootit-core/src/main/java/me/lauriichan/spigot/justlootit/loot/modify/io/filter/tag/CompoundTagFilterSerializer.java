package me.lauriichan.spigot.justlootit.loot.modify.io.filter.tag;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.loot.modify.filter.tag.CompoundTagFilter;
import me.lauriichan.spigot.justlootit.loot.modify.filter.tag.CompoundTagFilter.*;
import me.lauriichan.spigot.justlootit.loot.modify.io.FilterIO;
import me.lauriichan.spigot.justlootit.loot.modify.filter.tag.ITagFilter;
import me.lauriichan.spigot.justlootit.loot.modify.filter.tag.MatchType;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;

@Extension
@HandlerId("loot/filter/tag/compound")
public class CompoundTagFilterSerializer extends JsonSerializationHandler<CompoundTagFilter> {

    public CompoundTagFilterSerializer(BasePlugin<?> plugin) {
        super(plugin, CompoundTagFilter.class);
    }

    @Override
    public CompoundTagFilter deserialize(JsonObject buffer) {
        JsonArray array = buffer.getAsArray("compound_filters");
        MatchType matchType = FilterIO.readMatchType(buffer, "match_type");
        ObjectArrayList<ICompoundFilter> filters = new ObjectArrayList<>();
        for (IJson<?> entry : array) {
            if (!entry.isObject()) {
                continue;
            }
            filters.add(fromObject(entry.asJsonObject()));
        }
        return new CompoundTagFilter(filters, matchType);
    }

    @Override
    protected void serialize(JsonObject buffer, CompoundTagFilter value) {
        JsonArray array = new JsonArray();
        buffer.put("compound_filters", array);
        FilterIO.writeMatchType(buffer, "match_type", value.matchType());
        for (ICompoundFilter filter : value.filters()) {
            array.add(asObject(filter));
        }
    }

    private ICompoundFilter fromObject(JsonObject object) {
        if (object == null) {
            throw new IllegalStateException("Undefined compound filter");
        }
        String id = object.getAsString("id", "").toLowerCase();
        return switch (id) {
        case "value" -> {
            String key = object.getAsString("key");
            ITagFilter<?> filter = JsonIO.deserialize(ioManager, object.getAsObject("tag_filter"), ITagFilter.class);
            yield new ValueFilter(key, filter);
        }
        case "has" -> {
            String key = object.getAsString("key");
            TagType<?> type = FilterIO.readTagType(object, "tag_type");
            boolean numeric = object.getAsBoolean("numeric");
            boolean list = object.getAsBoolean("list");
            yield new HasFilter(key, type, numeric, list);
        }
        case "not" -> {
            yield new NotFilter(fromObject(object.getAsObject("compound_filter")));
        }
        default -> {
            throw new IllegalStateException("Unknown compound filter type '%s'".formatted(id));
        }
        };
    }

    private JsonObject asObject(ICompoundFilter filter) {
        if (filter instanceof ValueFilter valueFilter) {
            JsonObject object = new JsonObject();
            object.put("id", "value");
            object.put("key", valueFilter.key());
            object.put("tag_filter", JsonIO.serialize(ioManager, valueFilter.filter()));
            return object;
        }
        if (filter instanceof HasFilter hasFilter) {
            JsonObject object = new JsonObject();
            object.put("id", "has");
            object.put("key", hasFilter.key());
            FilterIO.writeTagType(object, "tag_type", hasFilter.type());
            object.put("numeric", hasFilter.numeric());
            object.put("list", hasFilter.list());
            return object;
        }
        if (filter instanceof NotFilter notFilter) {
            JsonObject object = new JsonObject();
            object.put("id", "not");
            object.put("compound_filter", asObject(notFilter.filter()));
            return object;
        }
        throw new IllegalStateException("Unknown filter type");
    }

}
