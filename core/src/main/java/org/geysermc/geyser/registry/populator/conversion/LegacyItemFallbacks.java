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

import java.util.HashMap;
import java.util.Map;

/**
 * Maps Bedrock item identifiers that were added after 1.21.0 onto items that exist in older palettes.
 */
public final class LegacyItemFallbacks {
    private static final Map<String, String> ALIASES = new HashMap<>();

    private static void alias(String from, String to) {
        ALIASES.put("minecraft:" + from, "minecraft:" + to);
    }

    static {
        // Copper Age (1.21.90)
        alias("copper_sword", "iron_sword");
        alias("copper_pickaxe", "iron_pickaxe");
        alias("copper_axe", "iron_axe");
        alias("copper_shovel", "iron_shovel");
        alias("copper_hoe", "iron_hoe");
        alias("copper_helmet", "iron_helmet");
        alias("copper_chestplate", "iron_chestplate");
        alias("copper_leggings", "iron_leggings");
        alias("copper_boots", "iron_boots");
        alias("copper_horse_armor", "leather_horse_armor");
        alias("copper_chest", "chest");
        alias("copper_chest_minecart", "chest_minecart");
        alias("copper_golem_spawn_egg", "iron_golem_spawn_egg");
        alias("copper_golem_statue", "iron_bars");

        // 1.21.50+ / 1.21.60+
        alias("golden_dandelion", "dandelion");
        alias("pale_oak_boat", "oak_boat");
        alias("pale_oak_chest_boat", "oak_chest_boat");
        alias("resin_brick", "brick");
        alias("open_eyeblossom", "dandelion");
        alias("closed_eyeblossom", "dandelion");
        alias("creaking_spawn_egg", "zombie_spawn_egg");

        // Mounts / newer content
        alias("copper_nautilus_armor", "turtle_helmet");
        alias("iron_nautilus_armor", "iron_horse_armor");
        alias("gold_nautilus_armor", "golden_horse_armor");
        alias("diamond_nautilus_armor", "diamond_horse_armor");
        alias("netherite_nautilus_armor", "netherite_horse_armor");
        alias("copper_spear", "trident");
        alias("iron_spear", "trident");
        alias("wooden_spear", "trident");
        alias("stone_spear", "trident");
        alias("golden_spear", "trident");
        alias("diamond_spear", "trident");
        alias("netherite_spear", "trident");
        alias("white_harness", "saddle");
        alias("orange_harness", "saddle");
        alias("magenta_harness", "saddle");
        alias("light_blue_harness", "saddle");
        alias("yellow_harness", "saddle");
        alias("lime_harness", "saddle");
        alias("pink_harness", "saddle");
        alias("gray_harness", "saddle");
        alias("light_gray_harness", "saddle");
        alias("cyan_harness", "saddle");
        alias("purple_harness", "saddle");
        alias("blue_harness", "saddle");
        alias("brown_harness", "saddle");
        alias("green_harness", "saddle");
        alias("red_harness", "saddle");
        alias("black_harness", "saddle");

        // 26.x materials
        alias("sulfur_cube_bucket", "pufferfish_bucket");
        alias("sulfur_cube_spawn_egg", "slime_spawn_egg");
        alias("music_disc_bounce", "music_disc_blocks");
        alias("poplar_boat", "oak_boat");
        alias("poplar_chest_boat", "oak_chest_boat");
    }

    /**
     * @return an item identifier that is more likely to exist in an old palette
     */
    public static String fallback(String identifier) {
        String direct = ALIASES.get(identifier);
        if (direct != null) {
            return direct;
        }

        String block = LegacyBlockConverter.resolve(identifier);
        if (block != null && !block.equals("minecraft:air")) {
            return block;
        }

        if (identifier.endsWith("_chest_boat")) {
            return "minecraft:oak_chest_boat";
        } else if (identifier.endsWith("_boat")) {
            return "minecraft:oak_boat";
        } else if (identifier.endsWith("_hanging_sign")) {
            return "minecraft:oak_hanging_sign";
        } else if (identifier.endsWith("_sign")) {
            return "minecraft:oak_sign";
        } else if (identifier.endsWith("_door")) {
            return "minecraft:oak_door";
        } else if (identifier.endsWith("_trapdoor")) {
            return "minecraft:oak_trapdoor";
        } else if (identifier.endsWith("_fence_gate")) {
            return "minecraft:oak_fence_gate";
        } else if (identifier.endsWith("_fence")) {
            return "minecraft:oak_fence";
        } else if (identifier.endsWith("_button")) {
            return "minecraft:oak_button";
        } else if (identifier.endsWith("_pressure_plate")) {
            return "minecraft:oak_pressure_plate";
        } else if (identifier.endsWith("_stairs")) {
            return "minecraft:oak_stairs";
        } else if (identifier.endsWith("_slab")) {
            return "minecraft:oak_slab";
        } else if (identifier.endsWith("_wall")) {
            return "minecraft:cobblestone_wall";
        } else if (identifier.endsWith("_planks")) {
            return "minecraft:oak_planks";
        } else if (identifier.endsWith("_log")) {
            return "minecraft:oak_log";
        } else if (identifier.endsWith("_wood")) {
            return "minecraft:oak_wood";
        } else if (identifier.endsWith("_leaves")) {
            return "minecraft:oak_leaves";
        } else if (identifier.endsWith("_sapling")) {
            return "minecraft:oak_sapling";
        } else if (identifier.endsWith("_spawn_egg")) {
            return "minecraft:chicken_spawn_egg";
        } else if (identifier.endsWith("_wool")) {
            return "minecraft:white_wool";
        } else if (identifier.endsWith("_carpet")) {
            return "minecraft:white_carpet";
        } else if (identifier.endsWith("_concrete_powder")) {
            return "minecraft:white_concrete_powder";
        } else if (identifier.endsWith("_concrete")) {
            return "minecraft:white_concrete";
        } else if (identifier.endsWith("_terracotta")) {
            return "minecraft:terracotta";
        } else if (identifier.endsWith("_shelf")) {
            return "minecraft:bookshelf";
        } else if (identifier.endsWith("_chain")) {
            return "minecraft:chain";
        } else if (identifier.endsWith("_bars")) {
            return "minecraft:iron_bars";
        } else if (identifier.endsWith("_lantern")) {
            return "minecraft:lantern";
        } else if (identifier.endsWith("_torch")) {
            return "minecraft:torch";
        } else if (identifier.endsWith("_bundle")) {
            return "minecraft:bundle";
        } else if (identifier.startsWith("minecraft:music_disc_")) {
            return "minecraft:music_disc_13";
        } else if (identifier.endsWith("_sword")) {
            return "minecraft:iron_sword";
        } else if (identifier.endsWith("_pickaxe")) {
            return "minecraft:iron_pickaxe";
        } else if (identifier.endsWith("_axe")) {
            return "minecraft:iron_axe";
        } else if (identifier.endsWith("_shovel")) {
            return "minecraft:iron_shovel";
        } else if (identifier.endsWith("_hoe")) {
            return "minecraft:iron_hoe";
        } else if (identifier.endsWith("_spear")) {
            return "minecraft:trident";
        } else if (identifier.endsWith("_helmet")) {
            return "minecraft:iron_helmet";
        } else if (identifier.endsWith("_chestplate")) {
            return "minecraft:iron_chestplate";
        } else if (identifier.endsWith("_leggings")) {
            return "minecraft:iron_leggings";
        } else if (identifier.endsWith("_boots")) {
            return "minecraft:iron_boots";
        } else if (identifier.endsWith("_horse_armor")) {
            return "minecraft:leather_horse_armor";
        }

        return "minecraft:paper";
    }

    private LegacyItemFallbacks() {
    }
}