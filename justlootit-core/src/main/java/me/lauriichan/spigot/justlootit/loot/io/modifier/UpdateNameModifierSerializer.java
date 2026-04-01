package me.lauriichan.spigot.justlootit.loot.io.modifier;

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
import me.lauriichan.spigot.justlootit.loot.modifier.UpdateNameModifier;

@Extension
@HandlerId("loot/modifier/update_name")
public class UpdateNameModifierSerializer extends JsonSerializationHandler<UpdateNameModifier> {

    public UpdateNameModifierSerializer(BasePlugin<?> plugin) {
        super(plugin, UpdateNameModifier.class);
    }

    @Override
    public UpdateNameModifier deserialize(JsonObject buffer) {
        JsonString nameJson = KeyedJsonSerializationHandler.STRING.from(buffer.get("name"));
        String name = "N/A";
        if (nameJson != null) {
            name = nameJson.value();
        }
        InsertionMode insertionMode = LootIO.readInsertionMode(buffer, "insertion_mode");
        return new UpdateNameModifier(name, insertionMode);
    }

    @Override
    protected void serialize(JsonObject buffer, UpdateNameModifier value) {
        if (value.newNameText().contains("\n")) {
            JsonArray array = new JsonArray();
            for (String line : value.newNameText().split("\n")) {
                array.add(IJson.of(line));
            }
            buffer.put("name", array);
        } else {
            buffer.put("name", value.newNameText());
        }
        LootIO.writeInsertionMode(buffer, "insertion_mode", value.insertionMode());
    }

}
