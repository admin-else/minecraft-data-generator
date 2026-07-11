package dev.u9g.minecraftdatagenerator.generators;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;

import java.lang.reflect.Method;
import java.util.Map;

public class MultiNoiseBiomeSourceParameterListDataGenerator implements IDataGenerator {
    @Override
    public String getDataName() {
        return "multi_noise_biome_source_parameter_list";
    }

    @Override
    public boolean isEnabled() {
        // Only available in 1.18+
        try {
            Class.forName("net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public JsonObject generateDataJson() {
        JsonObject root = new JsonObject();

        Map<MultiNoiseBiomeSourceParameterList.Preset, Climate.ParameterList<ResourceKey<Biome>>> presets =
                MultiNoiseBiomeSourceParameterList.knownPresets();

        for (Map.Entry<MultiNoiseBiomeSourceParameterList.Preset, Climate.ParameterList<ResourceKey<Biome>>> entry : presets.entrySet()) {
            String presetName = entry.getKey().id().toString();
            JsonArray entries = new JsonArray();

            for (Pair<Climate.ParameterPoint, ResourceKey<Biome>> pair : entry.getValue().values()) {
                Climate.ParameterPoint params = pair.getFirst();
                ResourceKey<Biome> biomeKey = pair.getSecond();

                JsonObject item = new JsonObject();
                item.addProperty("biome", resourceKeyLocation(biomeKey).toString());
                item.add("parameters", serializeParameterPoint(params));
                entries.add(item);
            }

            root.add(presetName, entries);
        }

        return root;
    }

    private static JsonObject serializeParameterPoint(Climate.ParameterPoint params) {
        JsonObject obj = new JsonObject();
        obj.add("temperature", serializeParameter(params.temperature()));
        obj.add("humidity", serializeParameter(params.humidity()));
        obj.add("continentalness", serializeParameter(params.continentalness()));
        obj.add("erosion", serializeParameter(params.erosion()));
        obj.add("depth", serializeParameter(params.depth()));
        obj.add("weirdness", serializeParameter(params.weirdness()));
        obj.addProperty("offset", params.offset() / 10000.0f);
        return obj;
    }

    private static JsonArray serializeParameter(Climate.Parameter parameter) {
        JsonArray arr = new JsonArray();
        arr.add(parameter.min());
        arr.add(parameter.max());
        return arr;
    }

    private static Object resourceKeyLocation(ResourceKey<?> key) {
        try {
            Method location = key.getClass().getMethod("location");
            return location.invoke(key);
        } catch (NoSuchMethodException e) {
            try {
                Method identifier = key.getClass().getMethod("identifier");
                return identifier.invoke(key);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
