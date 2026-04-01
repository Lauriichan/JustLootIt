package me.lauriichan.spigot.justlootit.loot.io.modifier;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.laylib.json.JsonString;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.HandlerId;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.JsonSerializationHandler;
import me.lauriichan.minecraft.pluginbase.io.serialization.json.KeyedJsonSerializationHandler;
import me.lauriichan.spigot.justlootit.loot.io.LootIO;
import me.lauriichan.spigot.justlootit.loot.modifier.InsertionMode;
import me.lauriichan.spigot.justlootit.loot.modifier.UpdateLoreModifier;

@Extension
@HandlerId("loot/modifier/update_lore")
public class UpdateLoreModifierSerializer extends JsonSerializationHandler<UpdateLoreModifier> {

    public UpdateLoreModifierSerializer(BasePlugin<?> plugin) {
        super(plugin, UpdateLoreModifier.class);
    }

    @Override
    public UpdateLoreModifier deserialize(JsonObject buffer) {
        ObjectList<String> lines = ObjectLists.unmodifiable(buffer.getAsArray("lines").stream().map(json -> {
            JsonString str = KeyedJsonSerializationHandler.STRING.from(json);
            if (str == null) {
                return null;
            }
            return str.value();
        }).filter(str -> str != null).collect(ObjectArrayList.toList()));
        InsertionMode insertionMode = LootIO.readInsertionMode(buffer, "insertion_mode");
        return new UpdateLoreModifier(lines, insertionMode);
    }

    @Override
    protected void serialize(JsonObject buffer, UpdateLoreModifier value) {
        JsonArray array = new JsonArray();
        for (String str : value.lines()) {
            if (str.contains("\n")) {
                JsonArray innerArray = new JsonArray();
                for (String line : str.split("\n")) {
                    innerArray.add(IJson.of(line));
                }
                array.add(innerArray);
                continue;
            }
            array.add(IJson.of(str));
        }
        buffer.put("lines", array);
        LootIO.writeInsertionMode(buffer, "insertion_mode", value.insertionMode());
    }

}
