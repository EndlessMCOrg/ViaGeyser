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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Maps Bedrock item identifiers that were added after 1.21.0 onto items that exist in older palettes.
 *
 * <p>Bedrock flattened most of its block identifiers in 1.21.60. The mappings in this repository are authored
 * for the latest Bedrock version, so every legacy palette needs both the flattened names and the genuinely
 * newer items translated back to something that exists in that palette. The translations follow the historic
 * Geyser conversion classes (1.21.20 through 1.21.50), so block variants such as slabs, walls and anvils keep
 * their damage value.</p>
 *
 * <p>Resolution is driven by the identifiers that actually exist in the target palette. This means a mapping is
 * only ever applied when its result is present, and that a mapping can fall back further (for example
 * {@code pale_oak_button} to {@code oak_button} to {@code wooden_button}) instead of failing the registry.</p>
 */
public final class LegacyItemFallbacks {
    private static final Fallback LAST_RESORT = new Fallback("minecraft:paper", 0);

    private static final Map<String, Fallback> ALIASES = new HashMap<>();
    private static final Map<String, String> PREFIX_ALIASES = new HashMap<>();
    private static final String[][] SUFFIX_ALIASES = {
        // Order matters: the more specific suffixes must be checked first.
        {"_chest_boat", "minecraft:oak_chest_boat"},
        {"_boat", "minecraft:oak_boat"},
        {"_hanging_sign", "minecraft:oak_hanging_sign"},
        {"_sign", "minecraft:oak_sign"},
        {"_trapdoor", "minecraft:trapdoor"},
        {"_door", "minecraft:wooden_door"},
        {"_fence_gate", "minecraft:fence_gate"},
        {"_fence", "minecraft:fence"},
        {"_button", "minecraft:wooden_button"},
        {"_pressure_plate", "minecraft:wooden_pressure_plate"},
        {"_stairs", "minecraft:oak_stairs"},
        {"_slab", "minecraft:oak_slab"},
        {"_wall", "minecraft:cobblestone_wall"},
        {"_planks", "minecraft:oak_planks"},
        {"_log", "minecraft:oak_log"},
        {"_wood", "minecraft:oak_wood"},
        {"_leaves", "minecraft:oak_leaves"},
        {"_sapling", "minecraft:oak_sapling"},
        {"_spawn_egg", "minecraft:chicken_spawn_egg"},
        {"_concrete_powder", "minecraft:white_concrete_powder"},
        {"_concrete", "minecraft:white_concrete"},
        {"_terracotta", "minecraft:terracotta"},
        {"_wool", "minecraft:white_wool"},
        {"_carpet", "minecraft:white_carpet"},
        {"_shelf", "minecraft:bookshelf"},
        {"_chain", "minecraft:chain"},
        {"_bars", "minecraft:iron_bars"},
        {"_lantern", "minecraft:lantern"},
        {"_torch", "minecraft:torch"},
        {"_bundle", "minecraft:bundle"},
        {"_horse_armor", "minecraft:leather_horse_armor"},
        {"_chestplate", "minecraft:iron_chestplate"},
        {"_leggings", "minecraft:iron_leggings"},
        {"_helmet", "minecraft:iron_helmet"},
        {"_boots", "minecraft:iron_boots"},
        {"_pickaxe", "minecraft:iron_pickaxe"},
        {"_shovel", "minecraft:iron_shovel"},
        {"_sword", "minecraft:iron_sword"},
        {"_hoe", "minecraft:iron_hoe"},
        {"_spear", "minecraft:trident"},
        {"copper_golem_statue", "minecraft:stone"},
        {"_lightning_rod", "minecraft:lightning_rod"}
    };

    private static void alias(String from, String to) {
        alias(from, to, 0);
    }

    private static void alias(String from, String to, int data) {
        ALIASES.put("minecraft:" + from, new Fallback("minecraft:" + to, data));
    }

    private static void slab(String from, String bedrock, int data) {
        alias(from, bedrock, data);
    }

    static {
        // Bedrock flattened these identifiers in 1.21.20 (historic Conversion712_685).
        alias("red_sandstone_slab", "stone_block_slab2", 0);
        alias("purpur_slab", "stone_block_slab2", 1);
        alias("prismarine_slab", "stone_block_slab2", 2);
        alias("dark_prismarine_slab", "stone_block_slab2", 3);
        alias("prismarine_brick_slab", "stone_block_slab2", 4);
        alias("mossy_cobblestone_slab", "stone_block_slab2", 5);
        alias("smooth_sandstone_slab", "stone_block_slab2", 6);
        alias("red_nether_brick_slab", "stone_block_slab2", 7);

        alias("end_stone_brick_slab", "stone_block_slab3", 0);
        alias("smooth_red_sandstone_slab", "stone_block_slab3", 1);
        alias("polished_andesite_slab", "stone_block_slab3", 2);
        alias("andesite_slab", "stone_block_slab3", 3);
        alias("diorite_slab", "stone_block_slab3", 4);
        alias("polished_diorite_slab", "stone_block_slab3", 5);
        alias("granite_slab", "stone_block_slab3", 6);
        alias("polished_granite_slab", "stone_block_slab3", 7);

        alias("mossy_stone_brick_slab", "stone_block_slab4", 0);
        alias("smooth_quartz_slab", "stone_block_slab4", 1);
        alias("normal_stone_slab", "stone_block_slab4", 2);
        alias("cut_sandstone_slab", "stone_block_slab4", 3);
        alias("cut_red_sandstone_slab", "stone_block_slab4", 4);

        alias("prismarine", "prismarine", 0);
        alias("dark_prismarine", "prismarine", 1);
        alias("prismarine_bricks", "prismarine", 2);

        alias("sandstone", "sandstone", 0);
        alias("chiseled_sandstone", "sandstone", 1);
        alias("cut_sandstone", "sandstone", 2);
        alias("smooth_sandstone", "sandstone", 3);

        alias("red_sandstone", "red_sandstone", 0);
        alias("chiseled_red_sandstone", "red_sandstone", 1);
        alias("cut_red_sandstone", "red_sandstone", 2);
        alias("smooth_red_sandstone", "red_sandstone", 3);

        alias("quartz_block", "quartz_block", 0);
        alias("chiseled_quartz_block", "quartz_block", 1);
        alias("quartz_pillar", "quartz_block", 2);
        alias("smooth_quartz", "quartz_block", 3);

        alias("infested_stone", "monster_egg", 0);
        alias("infested_cobblestone", "monster_egg", 1);
        alias("infested_stone_bricks", "monster_egg", 2);
        alias("infested_mossy_stone_bricks", "monster_egg", 3);
        alias("infested_cracked_stone_bricks", "monster_egg", 4);
        alias("infested_chiseled_stone_bricks", "monster_egg", 5);

        alias("stone_bricks", "stonebrick", 0);
        alias("mossy_stone_bricks", "stonebrick", 1);
        alias("cracked_stone_bricks", "stonebrick", 2);
        alias("chiseled_stone_bricks", "stonebrick", 3);

        alias("anvil", "anvil", 0);
        alias("chipped_anvil", "anvil", 4);
        alias("damaged_anvil", "anvil", 8);

        alias("dandelion", "yellow_flower");
        alias("red_sand", "sand", 1);
        alias("coarse_dirt", "dirt", 1);
        alias("wet_sponge", "sponge", 1);
        alias("underwater_tnt", "tnt", 1);

        // 1.21.20 flattened walls and purpur (historic Conversion729_712).
        alias("purpur_block", "purpur_block", 0);
        alias("purpur_pillar", "purpur_block", 1);
        alias("cobblestone_wall", "cobblestone_wall", 0);
        alias("mossy_cobblestone_wall", "cobblestone_wall", 1);
        alias("granite_wall", "cobblestone_wall", 2);
        alias("diorite_wall", "cobblestone_wall", 3);
        alias("andesite_wall", "cobblestone_wall", 4);
        alias("sandstone_wall", "cobblestone_wall", 5);
        alias("brick_wall", "cobblestone_wall", 6);
        alias("stone_brick_wall", "cobblestone_wall", 7);
        alias("mossy_stone_brick_wall", "cobblestone_wall", 8);
        alias("nether_brick_wall", "cobblestone_wall", 9);
        alias("end_stone_brick_wall", "cobblestone_wall", 10);
        alias("prismarine_wall", "cobblestone_wall", 11);
        alias("red_sandstone_wall", "cobblestone_wall", 12);
        alias("red_nether_brick_wall", "cobblestone_wall", 13);
        alias("sponge", "sponge", 0);
        alias("tnt", "tnt", 0);

        // 1.21.30 flattened the head items (historic Conversion748_729).
        alias("skeleton_skull", "skull", 0);
        alias("wither_skeleton_skull", "skull", 1);
        alias("zombie_head", "skull", 2);
        alias("player_head", "skull", 3);
        alias("creeper_head", "skull", 4);
        alias("dragon_head", "skull", 5);
        alias("piglin_head", "skull", 6);

        // Copper Age (1.21.90) and other post-1.21.0 content.
        alias("copper_sword", "iron_sword");
        alias("copper_pickaxe", "iron_pickaxe");
        alias("copper_axe", "iron_axe");
        alias("copper_shovel", "iron_shovel");
        alias("copper_hoe", "iron_hoe");
        alias("copper_helmet", "iron_helmet");
        alias("copper_chestplate", "iron_chestplate");
        alias("copper_leggings", "iron_leggings");
        alias("copper_boots", "iron_boots");
        alias("copper_horse_armor", "iron_horse_armor");
        alias("copper_chest", "chest");
        alias("copper_chest_minecart", "chest_minecart");
        alias("copper_golem_spawn_egg", "iron_golem_spawn_egg");
        alias("copper_golem_statue", "stone");
        alias("copper_torch", "torch");
        alias("copper_bars", "iron_bars");
        alias("copper_lantern", "lantern");
        alias("copper_chain", "chain");
        alias("iron_chain", "chain");
        alias("copper_nugget", "iron_nugget");

        alias("golden_dandelion", "dandelion");
        alias("pale_oak_boat", "oak_boat");
        alias("pale_oak_chest_boat", "oak_chest_boat");
        alias("resin_brick", "brick");
        alias("resin_bricks", "brick_block");
        alias("open_eyeblossom", "dandelion");
        alias("closed_eyeblossom", "dandelion");
        alias("creaking_spawn_egg", "zombie_spawn_egg");
        alias("creaking_heart", "oak_wood");
        alias("pale_moss_block", "moss_block");
        alias("pale_moss_carpet", "moss_carpet");
        alias("pale_hanging_moss", "cave_vines");
        alias("bush", "oak_leaves");
        alias("firefly_bush", "oak_leaves");
        alias("leaf_litter", "oak_leaves");
        alias("wildflowers", "dandelion");
        alias("cactus_flower", "cactus");
        alias("short_dry_grass", "short_grass");
        alias("tall_dry_grass", "tall_grass");
        alias("blue_egg", "egg");
        alias("brown_egg", "egg");
        alias("dried_ghast", "ghast_tear");
        alias("shelf_mushroom", "oak_planks");
        alias("straw_bed", "yellow_carpet");
        alias("resin_block", "yellow_terracotta");
        alias("resin_clump", "yellow_terracotta");
        alias("chiseled_resin_bricks", "yellow_terracotta");
        alias("mushroom_stem", "brown_mushroom_block");
        alias("bundle", "leather");

        // Mounts / newer content.
        alias("copper_nautilus_armor", "chainmail_helmet");
        alias("iron_nautilus_armor", "iron_helmet");
        alias("golden_nautilus_armor", "golden_helmet");
        alias("diamond_nautilus_armor", "diamond_helmet");
        alias("netherite_nautilus_armor", "netherite_helmet");
        alias("copper_spear", "stone_sword");
        alias("iron_spear", "iron_sword");
        alias("wooden_spear", "wooden_sword");
        alias("stone_spear", "stone_sword");
        alias("golden_spear", "golden_sword");
        alias("diamond_spear", "diamond_sword");
        alias("netherite_spear", "netherite_sword");
        alias("netherite_horse_armor", "diamond_horse_armor");
        alias("nautilus_spawn_egg", "squid_spawn_egg");
        alias("camel_husk_spawn_egg", "husk_spawn_egg");
        alias("parched_spawn_egg", "husk_spawn_egg");
        alias("zombie_nautilus_spawn_egg", "zombie_spawn_egg");
        alias("happy_ghast_spawn_egg", "ghast_spawn_egg");

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

        // 26.x materials.
        alias("sulfur_cube_bucket", "pufferfish_bucket");
        alias("sulfur_cube_spawn_egg", "slime_spawn_egg");
        alias("sulfur_spike", "stone");
        alias("music_disc_bounce", "music_disc_blocks");
        alias("poplar_boat", "oak_boat");
        alias("poplar_chest_boat", "oak_chest_boat");

        PREFIX_ALIASES.put("minecraft:pale_oak_", "minecraft:oak_");
        PREFIX_ALIASES.put("minecraft:poplar_", "minecraft:oak_");
        PREFIX_ALIASES.put("minecraft:resin_", "minecraft:oak_");
    }

    /**
     * Resolves an identifier that may not exist in the target palette to the first identifier that does.
     *
     * @param identifier the Bedrock identifier from the item mappings
     * @param data the Bedrock data value from the item mappings
     * @param exists checks whether an identifier exists in the target palette
     * @return the mapping that should be used for this palette
     */
    public static Fallback firstAvailable(String identifier, int data, Predicate<String> exists) {
        Set<String> seen = new HashSet<>();
        Fallback current = new Fallback(identifier, data);
        while (true) {
            if (exists.test(current.identifier())) {
                return current;
            }
            if (!seen.add(current.identifier())) {
                break;
            }
            current = next(current);
        }
        return LAST_RESORT;
    }

    private static Fallback next(Fallback current) {
        String identifier = current.identifier();

        Fallback direct = ALIASES.get(identifier);
        if (direct != null) {
            return direct;
        }

        if (identifier.startsWith("minecraft:music_disc_")) {
            return new Fallback("minecraft:music_disc_13", 0);
        }

        String block = LegacyBlockConverter.resolve(identifier);
        if (block != null && !"minecraft:air".equals(block)) {
            return new Fallback(block, 0);
        }

        for (Map.Entry<String, String> prefix : PREFIX_ALIASES.entrySet()) {
            if (identifier.startsWith(prefix.getKey())) {
                return new Fallback(prefix.getValue() + identifier.substring(prefix.getKey().length()), 0);
            }
        }

        for (String[] suffix : SUFFIX_ALIASES) {
            if (identifier.endsWith(suffix[0])) {
                return new Fallback(suffix[1], 0);
            }
        }

        return LAST_RESORT;
    }

    public record Fallback(String identifier, int data) {
    }

    private LegacyItemFallbacks() {
    }
}
