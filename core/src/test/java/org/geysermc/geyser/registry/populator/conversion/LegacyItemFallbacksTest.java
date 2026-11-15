/*
 * Copyright (c) 2026 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Geyser
 */

package org.geysermc.geyser.registry.populator.conversion;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.cloudburstmc.nbt.NbtMap;
import org.geysermc.geyser.GeyserImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

public class LegacyItemFallbacksTest {
    private static final String[] LEGACY_VERSIONS = {
        "1_21_0", "1_21_20", "1_21_30", "1_21_40", "1_21_50", "1_21_60", "1_21_70",
        "1_21_80", "1_21_90", "1_21_93", "1_21_100", "1_21_110", "1_21_120", "1_21_130"
    };

    private static MockedStatic<GeyserImpl> geyserInstance;

    @BeforeAll
    static void setUp() {
        // The block and item conversion classes touch the Geyser singleton while initialising their lookup tables.
        GeyserImpl geyser = mock(GeyserImpl.class, RETURNS_DEEP_STUBS);
        geyserInstance = mockStatic(GeyserImpl.class);
        geyserInstance.when(GeyserImpl::getInstance).thenReturn(geyser);
    }

    @AfterAll
    static void tearDown() {
        geyserInstance.close();
    }

    @Test
    public void everyMappingResolvesInLegacyPalette() throws Exception {
        Map<String, JsonObject> mappings = loadMappings();

        for (String version : LEGACY_VERSIONS) {
            Set<String> palette = loadPalette("bedrock/runtime_item_states." + version + ".json");
            for (Map.Entry<String, JsonObject> entry : mappings.entrySet()) {
                JsonObject mapping = entry.getValue();
                if (!mapping.has("bedrock_identifier")) {
                    continue;
                }
                String identifier = mapping.get("bedrock_identifier").getAsString();
                int data = mapping.has("bedrock_data") ? mapping.get("bedrock_data").getAsInt() : 0;

                LegacyItemFallbacks.Fallback fallback;
                try {
                    fallback = LegacyItemFallbacks.firstAvailable(identifier, data, palette::contains);
                } catch (Throwable throwable) {
                    throw new AssertionError("Failed to resolve " + entry.getKey() + " (" + identifier + ") for " + version, throwable);
                }
                Assertions.assertTrue(palette.contains(fallback.identifier()),
                    version + ": " + entry.getKey() + " (" + identifier + ") resolved to " + fallback.identifier() + ", which is not in the palette");
            }
        }
    }

    @Test
    public void flattenedBlockIdentifiersUseTheirLegacyVariant() throws Exception {
        Set<String> palette = loadPalette("bedrock/runtime_item_states.1_21_0.json");

        assertFallback(palette, "minecraft:stone_bricks", 0, "minecraft:stonebrick", 0);
        assertFallback(palette, "minecraft:mossy_stone_bricks", 0, "minecraft:stonebrick", 1);
        assertFallback(palette, "minecraft:andesite_slab", 0, "minecraft:stone_block_slab3", 3);
        assertFallback(palette, "minecraft:normal_stone_slab", 0, "minecraft:stone_block_slab4", 2);
        assertFallback(palette, "minecraft:andesite_wall", 0, "minecraft:cobblestone_wall", 4);
        assertFallback(palette, "minecraft:chipped_anvil", 0, "minecraft:anvil", 4);
        assertFallback(palette, "minecraft:dandelion", 0, "minecraft:yellow_flower", 0);
        assertFallback(palette, "minecraft:skeleton_skull", 0, "minecraft:skull", 0);
        assertFallback(palette, "minecraft:player_head", 0, "minecraft:skull", 3);
    }

    @Test
    public void newerItemsFallBackToSomethingThatExists() throws Exception {
        Set<String> palette = loadPalette("bedrock/runtime_item_states.1_21_0.json");

        assertFallback(palette, "minecraft:pale_oak_button", 0, "minecraft:wooden_button", 0);
        assertFallback(palette, "minecraft:pale_oak_door", 0, "minecraft:wooden_door", 0);
        assertFallback(palette, "minecraft:pale_oak_shelf", 0, "minecraft:bookshelf", 0);
        assertFallback(palette, "minecraft:copper_spear", 0, "minecraft:stone_sword", 0);
        assertFallback(palette, "minecraft:netherite_horse_armor", 0, "minecraft:diamond_horse_armor", 0);
        assertFallback(palette, "minecraft:nautilus_spawn_egg", 0, "minecraft:squid_spawn_egg", 0);
        assertFallback(palette, "minecraft:sulfur_spike", 0, "minecraft:stone", 0);
        assertFallback(palette, "minecraft:music_disc_tears", 0, "minecraft:music_disc_13", 0);
    }

    @Test
    public void blockConverterKnowsLegacyRenames() {
        Assertions.assertEquals("minecraft:stonebrick", LegacyBlockConverter.resolve("minecraft:stone_bricks"));
        Assertions.assertEquals("minecraft:stone_block_slab3", LegacyBlockConverter.resolve("minecraft:andesite_slab"));
        Assertions.assertEquals("minecraft:cobblestone_wall", LegacyBlockConverter.resolve("minecraft:andesite_wall"));
        Assertions.assertEquals("minecraft:anvil", LegacyBlockConverter.resolve("minecraft:chipped_anvil"));
        Assertions.assertEquals("minecraft:yellow_flower", LegacyBlockConverter.resolve("minecraft:dandelion"));
        Assertions.assertEquals("minecraft:skull", LegacyBlockConverter.resolve("minecraft:player_head"));
        Assertions.assertEquals("minecraft:oak_button", LegacyBlockConverter.resolve("minecraft:pale_oak_button"));
        Assertions.assertEquals("minecraft:yellow_concrete", LegacyBlockConverter.resolve("minecraft:sulfur"));
        Assertions.assertEquals("minecraft:air", LegacyBlockConverter.resolve("minecraft:copper_golem_statue"));
    }

    @Test
    public void blockConverterRewritesLegacyStates() {
        LegacyBlockConverter converter = new LegacyBlockConverter(java.util.List.of());

        NbtMap stoneBricks = converter.remapBlock(tag("minecraft:stone_bricks"));
        Assertions.assertEquals("minecraft:stonebrick", stoneBricks.getString("name"));
        Assertions.assertEquals("default", stoneBricks.getCompound("states").getString("stone_brick_type"));

        NbtMap chippedAnvil = converter.remapBlock(tag("minecraft:chipped_anvil"));
        Assertions.assertEquals("minecraft:anvil", chippedAnvil.getString("name"));
        Assertions.assertEquals("slightly_damaged", chippedAnvil.getCompound("states").getString("damage"));

        NbtMap andesiteSlab = converter.remapBlock(tag("minecraft:andesite_slab"));
        Assertions.assertEquals("minecraft:stone_block_slab3", andesiteSlab.getString("name"));
        Assertions.assertEquals("andesite", andesiteSlab.getCompound("states").getString("stone_slab_type_3"));

        NbtMap paleOakButton = converter.remapBlock(tag("minecraft:pale_oak_button"));
        Assertions.assertEquals("minecraft:wooden_button", paleOakButton.getString("name"));
    }

    private static NbtMap tag(String name) {
        return NbtMap.builder()
            .putString("name", name)
            .putCompound("states", NbtMap.EMPTY)
            .build();
    }

    private static void assertFallback(Set<String> palette, String identifier, int data,
                                       String expectedIdentifier, int expectedData) {
        LegacyItemFallbacks.Fallback fallback = LegacyItemFallbacks.firstAvailable(identifier, data, palette::contains);
        Assertions.assertEquals(expectedIdentifier, fallback.identifier(), identifier);
        Assertions.assertEquals(expectedData, fallback.data(), identifier);
    }

    private static Set<String> loadPalette(String resource) throws Exception {
        Set<String> names = new HashSet<>();
        try (Reader reader = new InputStreamReader(LegacyItemFallbacksTest.class.getClassLoader().getResourceAsStream(resource), StandardCharsets.UTF_8)) {
            for (JsonElement element : JsonParser.parseReader(reader).getAsJsonArray()) {
                names.add(element.getAsJsonObject().get("name").getAsString());
            }
        }
        return names;
    }

    private static Map<String, JsonObject> loadMappings() throws Exception {
        Map<String, JsonObject> mappings = new HashMap<>();
        try (Reader reader = new InputStreamReader(LegacyItemFallbacksTest.class.getClassLoader().getResourceAsStream("mappings/items.json"), StandardCharsets.UTF_8)) {
            for (Map.Entry<String, JsonElement> entry : JsonParser.parseReader(reader).getAsJsonObject().entrySet()) {
                mappings.put(entry.getKey(), entry.getValue().getAsJsonObject());
            }
        }
        return mappings;
    }
}
