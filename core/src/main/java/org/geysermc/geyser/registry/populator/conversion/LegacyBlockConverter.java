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

import org.cloudburstmc.nbt.NbtMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Best-effort downgrade of the block states that the current mappings produce to a Bedrock 1.21.x palette.
 *
 * <p>Blocks that were introduced after the target version cannot be represented on that version. They are
 * remapped to a similar older block where one exists, and otherwise to air. Block states that no longer exist
 * in the target palette are removed before the lookup, which keeps blocks such as stairs and fences usable.</p>
 */
public class LegacyBlockConverter implements BlockRemapper {
    private static final Map<String, String> NAME_ALIASES = new HashMap<>();
    private static final Map<String, String> PREFIX_ALIASES = new HashMap<>();

    private static void alias(String from, String to) {
        NAME_ALIASES.put("minecraft:" + from, "minecraft:" + to);
    }

    static {
        // Copper Age blocks (1.21.90) that map cleanly onto older blocks
        alias("copper_bars", "iron_bars");
        alias("copper_chain", "chain");
        alias("copper_lantern", "lantern");
        alias("copper_torch", "torch");
        alias("copper_golem_statue", "air");
        alias("copper_chest", "chest");

        // 1.21.50+ blocks
        alias("golden_dandelion", "dandelion");
        alias("bush", "oak_leaves");
        alias("firefly_bush", "oak_leaves");
        alias("short_dry_grass", "short_grass");
        alias("tall_dry_grass", "tall_grass");
        alias("leaf_litter", "oak_leaves");
        alias("wildflowers", "dandelion");
        alias("cactus_flower", "cactus");
        alias("resin_block", "yellow_terracotta");
        alias("resin_bricks", "yellow_terracotta");
        alias("chiseled_resin_bricks", "yellow_terracotta");
        alias("resin_clump", "yellow_terracotta");
        alias("open_eyeblossom", "dandelion");
        alias("closed_eyeblossom", "dandelion");
        alias("creaking_heart", "oak_wood");
        alias("pale_moss_block", "moss_block");
        alias("pale_moss_carpet", "moss_carpet");
        alias("pale_hanging_moss", "cave_vines");

        // 26.x blocks
        alias("sulfur", "yellow_concrete");
        alias("polished_sulfur", "yellow_concrete");
        alias("sulfur_bricks", "yellow_concrete");
        alias("chiseled_sulfur", "yellow_concrete");
        alias("potent_sulfur", "yellow_concrete");
        alias("sulfur_spike", "pointed_dripstone");
        alias("cinnabar", "red_concrete");
        alias("polished_cinnabar", "red_concrete");
        alias("cinnabar_bricks", "red_concrete");
        alias("chiseled_cinnabar", "red_concrete");
        alias("shelf_mushroom", "oak_planks");
        alias("straw_bed", "yellow_carpet");
        alias("red_shrub", "bush");

        PREFIX_ALIASES.put("minecraft:pale_oak_", "minecraft:oak_");
        PREFIX_ALIASES.put("minecraft:poplar_", "minecraft:oak_");
        PREFIX_ALIASES.put("minecraft:resin_", "minecraft:oak_");
    }

    private final Set<String> names;
    private final Map<String, Set<String>> stateKeys;

    public LegacyBlockConverter(List<NbtMap> palette) {
        this.names = new HashSet<>(palette.size());
        this.stateKeys = new HashMap<>(palette.size());

        for (NbtMap tag : palette) {
            String name = tag.getString("name");
            this.names.add(name);

            Set<String> keys = this.stateKeys.computeIfAbsent(name, key -> new HashSet<>());
            NbtMap states = tag.getCompound("states");
            if (states != null) {
                keys.addAll(states.keySet());
            }
        }
    }

    @Override
    public NbtMap remapBlock(NbtMap tag) {
        // Handles the blocks and block states that were added in 26.50/26.60
        tag = WildernessBoundConverter.INSTANCE.remapBlock(tag);

        String name = tag.getString("name");
        if (!this.names.contains(name)) {
            String replacement = resolve(name);
            if (replacement != null) {
                tag = ConversionHelper.withId(tag, replacement);
                name = replacement;
            }
        }

        Set<String> allowedStates = this.stateKeys.get(name);
        if (allowedStates == null || allowedStates.isEmpty()) {
            return tag;
        }

        NbtMap states = tag.getCompound("states");
        if (states == null || states.isEmpty()) {
            return tag;
        }

        Map<String, Object> filtered = new HashMap<>(states.size());
        for (Map.Entry<String, Object> entry : states.entrySet()) {
            if (allowedStates.contains(entry.getKey())) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }

        if (filtered.size() == states.size()) {
            return tag;
        }
        return tag.toBuilder().putCompound("states", NbtMap.fromMap(filtered)).build();
    }

    /**
     * Resolves a block identifier that is not present in the target palette to the closest available identifier.
     */
    public static String resolve(String name) {
        String direct = NAME_ALIASES.get(name);
        if (direct != null && !"minecraft:air".equals(direct)) {
            return direct;
        }
        if ("minecraft:air".equals(direct)) {
            return direct;
        }

        String wilderness = WildernessBoundConverter.blockFallback(name);
        if (wilderness != null) {
            return resolve(wilderness);
        }

        for (Map.Entry<String, String> prefix : PREFIX_ALIASES.entrySet()) {
            if (name.startsWith(prefix.getKey())) {
                return prefix.getValue() + name.substring(prefix.getKey().length());
            }
        }

        if (name.startsWith("minecraft:waxed_")) {
            String unwaxed = "minecraft:" + name.substring("minecraft:waxed_".length());
            String resolved = resolve(unwaxed);
            if (resolved != null) {
                return resolved;
            }
        }

        // Generic suffixes for content that was added for existing block families
        if (name.endsWith("_chain")) {
            return "minecraft:chain";
        } else if (name.endsWith("_bars")) {
            return "minecraft:iron_bars";
        } else if (name.endsWith("_lantern")) {
            return "minecraft:lantern";
        } else if (name.endsWith("_torch")) {
            return "minecraft:torch";
        } else if (name.endsWith("_shelf")) {
            return "minecraft:bookshelf";
        } else if (name.endsWith("_golem_statue")) {
            return "minecraft:air";
        } else if (name.contains("copper") && name.endsWith("_chest")) {
            return "minecraft:chest";
        }

        return null;
    }
}