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
import org.cloudburstmc.nbt.NbtMapBuilder;

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
 *
 * <p>Bedrock flattened most block identifiers in 1.21.60. The renames below are the same ones Geyser used when
 * it still supported 1.21.0 through 1.21.50 (see the historic {@code Conversion712_685}, {@code Conversion729_712}
 * and {@code Conversion748_729} classes), including the block state that carries the variant.</p>
 */
public class LegacyBlockConverter implements BlockRemapper {
    private static final Map<String, String> NAME_ALIASES = new HashMap<>();
    private static final Map<String, StateRename> STATE_RENAMES = new HashMap<>();
    private static final Map<String, CoralFan> CORAL_FANS = new HashMap<>();
    private static final Map<String, String> PREFIX_ALIASES = new HashMap<>();

    private static void alias(String from, String to) {
        NAME_ALIASES.put("minecraft:" + from, "minecraft:" + to);
    }

    private static void coralFan(String from, String to, boolean hangTypeBit, boolean dead) {
        CORAL_FANS.put("minecraft:" + from, new CoralFan("minecraft:" + to, hangTypeBit, dead));
    }

    private static void rename(String from, String to) {
        STATE_RENAMES.put("minecraft:" + from, new StateRename("minecraft:" + to, null, null));
    }

    private static void rename(String from, String to, String state, Object value) {
        STATE_RENAMES.put("minecraft:" + from, new StateRename("minecraft:" + to, state, value));
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
        alias("dried_ghast", "unknown");

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
        alias("mushroom_stem", "brown_mushroom_block");
        alias("lightning_rod", "lightning_rod");

        // Bedrock kept the legacy names for these oak blocks. The item mappings use the Java-style names.
        alias("oak_button", "wooden_button");
        alias("oak_door", "wooden_door");
        alias("oak_trapdoor", "trapdoor");
        alias("oak_fence", "fence");
        alias("oak_fence_gate", "fence_gate");
        alias("oak_pressure_plate", "wooden_pressure_plate");

        // Identifiers that Bedrock flattened in 1.21.20 (historic Conversion712_685 / Conversion729_712).
        // The second argument is the block identifier that existed before flattening, and the third and
        // fourth are the Bedrock state and value that select the same variant.
        rename("red_sandstone_slab", "stone_block_slab2", "stone_slab_type_2", "red_sandstone");
        rename("purpur_slab", "stone_block_slab2", "stone_slab_type_2", "purpur");
        rename("prismarine_slab", "stone_block_slab2", "stone_slab_type_2", "prismarine_rough");
        rename("dark_prismarine_slab", "stone_block_slab2", "stone_slab_type_2", "prismarine_dark");
        rename("prismarine_brick_slab", "stone_block_slab2", "stone_slab_type_2", "prismarine_brick");
        rename("mossy_cobblestone_slab", "stone_block_slab2", "stone_slab_type_2", "mossy_cobblestone");
        rename("smooth_sandstone_slab", "stone_block_slab2", "stone_slab_type_2", "smooth_sandstone");
        rename("red_nether_brick_slab", "stone_block_slab2", "stone_slab_type_2", "red_nether_brick");

        rename("end_stone_brick_slab", "stone_block_slab3", "stone_slab_type_3", "end_stone_brick");
        rename("smooth_red_sandstone_slab", "stone_block_slab3", "stone_slab_type_3", "smooth_red_sandstone");
        rename("polished_andesite_slab", "stone_block_slab3", "stone_slab_type_3", "polished_andesite");
        rename("andesite_slab", "stone_block_slab3", "stone_slab_type_3", "andesite");
        rename("diorite_slab", "stone_block_slab3", "stone_slab_type_3", "diorite");
        rename("polished_diorite_slab", "stone_block_slab3", "stone_slab_type_3", "polished_diorite");
        rename("granite_slab", "stone_block_slab3", "stone_slab_type_3", "granite");
        rename("polished_granite_slab", "stone_block_slab3", "stone_slab_type_3", "polished_granite");

        rename("mossy_stone_brick_slab", "stone_block_slab4", "stone_slab_type_4", "mossy_stone_brick");
        rename("smooth_quartz_slab", "stone_block_slab4", "stone_slab_type_4", "smooth_quartz");
        rename("normal_stone_slab", "stone_block_slab4", "stone_slab_type_4", "stone");
        rename("cut_sandstone_slab", "stone_block_slab4", "stone_slab_type_4", "cut_sandstone");
        rename("cut_red_sandstone_slab", "stone_block_slab4", "stone_slab_type_4", "cut_red_sandstone");

        // Double slabs were flattened together with their single slab counterparts.
        rename("quartz_double_slab", "double_stone_block_slab", "stone_slab_type", "quartz");
        rename("petrified_oak_double_slab", "double_stone_block_slab", "stone_slab_type", "wood");
        rename("stone_brick_double_slab", "double_stone_block_slab", "stone_slab_type", "stone_brick");
        rename("brick_double_slab", "double_stone_block_slab", "stone_slab_type", "brick");
        rename("sandstone_double_slab", "double_stone_block_slab", "stone_slab_type", "sandstone");
        rename("nether_brick_double_slab", "double_stone_block_slab", "stone_slab_type", "nether_brick");
        rename("cobblestone_double_slab", "double_stone_block_slab", "stone_slab_type", "cobblestone");
        rename("smooth_stone_double_slab", "double_stone_block_slab", "stone_slab_type", "smooth_stone");

        rename("red_sandstone_double_slab", "double_stone_block_slab2", "stone_slab_type_2", "red_sandstone");
        rename("purpur_double_slab", "double_stone_block_slab2", "stone_slab_type_2", "purpur");
        rename("prismarine_double_slab", "double_stone_block_slab2", "stone_slab_type_2", "prismarine_rough");
        rename("dark_prismarine_double_slab", "double_stone_block_slab2", "stone_slab_type_2", "prismarine_dark");
        rename("prismarine_brick_double_slab", "double_stone_block_slab2", "stone_slab_type_2", "prismarine_brick");
        rename("mossy_cobblestone_double_slab", "double_stone_block_slab2", "stone_slab_type_2", "mossy_cobblestone");
        rename("smooth_sandstone_double_slab", "double_stone_block_slab2", "stone_slab_type_2", "smooth_sandstone");
        rename("red_nether_brick_double_slab", "double_stone_block_slab2", "stone_slab_type_2", "red_nether_brick");

        rename("end_stone_brick_double_slab", "double_stone_block_slab3", "stone_slab_type_3", "end_stone_brick");
        rename("smooth_red_sandstone_double_slab", "double_stone_block_slab3", "stone_slab_type_3", "smooth_red_sandstone");
        rename("polished_andesite_double_slab", "double_stone_block_slab3", "stone_slab_type_3", "polished_andesite");
        rename("andesite_double_slab", "double_stone_block_slab3", "stone_slab_type_3", "andesite");
        rename("diorite_double_slab", "double_stone_block_slab3", "stone_slab_type_3", "diorite");
        rename("polished_diorite_double_slab", "double_stone_block_slab3", "stone_slab_type_3", "polished_diorite");
        rename("granite_double_slab", "double_stone_block_slab3", "stone_slab_type_3", "granite");
        rename("polished_granite_double_slab", "double_stone_block_slab3", "stone_slab_type_3", "polished_granite");

        rename("mossy_stone_brick_double_slab", "double_stone_block_slab4", "stone_slab_type_4", "mossy_stone_brick");
        rename("smooth_quartz_double_slab", "double_stone_block_slab4", "stone_slab_type_4", "smooth_quartz");
        rename("normal_stone_double_slab", "double_stone_block_slab4", "stone_slab_type_4", "stone");
        rename("cut_sandstone_double_slab", "double_stone_block_slab4", "stone_slab_type_4", "cut_sandstone");
        rename("cut_red_sandstone_double_slab", "double_stone_block_slab4", "stone_slab_type_4", "cut_red_sandstone");

        // Coral wall fans were flattened into per-colour identifiers in 1.21.20.
        coralFan("tube_coral_wall_fan", "coral_fan_hang", false, false);
        coralFan("brain_coral_wall_fan", "coral_fan_hang", true, false);
        coralFan("bubble_coral_wall_fan", "coral_fan_hang2", false, false);
        coralFan("fire_coral_wall_fan", "coral_fan_hang2", true, false);
        coralFan("horn_coral_wall_fan", "coral_fan_hang3", false, false);
        coralFan("dead_tube_coral_wall_fan", "coral_fan_hang", false, true);
        coralFan("dead_brain_coral_wall_fan", "coral_fan_hang", true, true);
        coralFan("dead_bubble_coral_wall_fan", "coral_fan_hang2", false, true);
        coralFan("dead_fire_coral_wall_fan", "coral_fan_hang2", true, true);
        coralFan("dead_horn_coral_wall_fan", "coral_fan_hang3", false, true);

        rename("dark_prismarine", "prismarine", "prismarine_block_type", "dark");
        rename("prismarine_bricks", "prismarine", "prismarine_block_type", "bricks");

        rename("chiseled_sandstone", "sandstone", "sand_stone_type", "heiroglyphs");
        rename("cut_sandstone", "sandstone", "sand_stone_type", "cut");
        rename("smooth_sandstone", "sandstone", "sand_stone_type", "smooth");

        rename("chiseled_red_sandstone", "red_sandstone", "sand_stone_type", "heiroglyphs");
        rename("cut_red_sandstone", "red_sandstone", "sand_stone_type", "cut");
        rename("smooth_red_sandstone", "red_sandstone", "sand_stone_type", "smooth");

        rename("chiseled_quartz_block", "quartz_block", "chisel_type", "chiseled");
        rename("quartz_pillar", "quartz_block", "chisel_type", "lines");
        rename("smooth_quartz", "quartz_block", "chisel_type", "smooth");

        rename("infested_stone", "monster_egg", "monster_egg_stone_type", "stone");
        rename("infested_cobblestone", "monster_egg", "monster_egg_stone_type", "cobblestone");
        rename("infested_stone_bricks", "monster_egg", "monster_egg_stone_type", "stone_brick");
        rename("infested_mossy_stone_bricks", "monster_egg", "monster_egg_stone_type", "mossy_stone_brick");
        rename("infested_cracked_stone_bricks", "monster_egg", "monster_egg_stone_type", "cracked_stone_brick");
        rename("infested_chiseled_stone_bricks", "monster_egg", "monster_egg_stone_type", "chiseled_stone_brick");

        rename("stone_bricks", "stonebrick", "stone_brick_type", "default");
        rename("mossy_stone_bricks", "stonebrick", "stone_brick_type", "mossy");
        rename("cracked_stone_bricks", "stonebrick", "stone_brick_type", "cracked");
        rename("chiseled_stone_bricks", "stonebrick", "stone_brick_type", "chiseled");

        rename("chipped_anvil", "anvil", "damage", "slightly_damaged");
        rename("damaged_anvil", "anvil", "damage", "broken");

        rename("dandelion", "yellow_flower");
        rename("red_sand", "sand", "sand_type", "red");
        rename("coarse_dirt", "dirt", "dirt_type", "coarse");
        rename("wet_sponge", "sponge", "sponge_type", "wet");
        rename("underwater_tnt", "tnt", "allow_underwater_bit", (byte) 1);

        rename("purpur_pillar", "purpur_block", "chisel_type", "lines");

        rename("mossy_cobblestone_wall", "cobblestone_wall", "wall_block_type", "mossy");
        rename("granite_wall", "cobblestone_wall", "wall_block_type", "granite");
        rename("diorite_wall", "cobblestone_wall", "wall_block_type", "diorite");
        rename("andesite_wall", "cobblestone_wall", "wall_block_type", "andesite");
        rename("sandstone_wall", "cobblestone_wall", "wall_block_type", "sandstone");
        rename("brick_wall", "cobblestone_wall", "wall_block_type", "brick");
        rename("stone_brick_wall", "cobblestone_wall", "wall_block_type", "stone_brick");
        rename("mossy_stone_brick_wall", "cobblestone_wall", "wall_block_type", "mossy_stone_brick");
        rename("nether_brick_wall", "cobblestone_wall", "wall_block_type", "nether_brick");
        rename("end_stone_brick_wall", "cobblestone_wall", "wall_block_type", "end_stone_brick");
        rename("prismarine_wall", "cobblestone_wall", "wall_block_type", "prismarine");
        rename("red_sandstone_wall", "cobblestone_wall", "wall_block_type", "red_sandstone");
        rename("red_nether_brick_wall", "cobblestone_wall", "wall_block_type", "red_nether_brick");

        // Heads were split from the skull block in 1.21.30 (historic Conversion748_729).
        rename("skeleton_skull", "skull", "skull_type", "skeleton");
        rename("wither_skeleton_skull", "skull", "skull_type", "wither");
        rename("zombie_head", "skull", "skull_type", "zombie");
        rename("player_head", "skull", "skull_type", "player");
        rename("creeper_head", "skull", "skull_type", "creeper");
        rename("dragon_head", "skull", "skull_type", "dragon");
        rename("piglin_head", "skull", "skull_type", "piglin");

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
        for (int attempts = 0; attempts < 8; attempts++) {
            if (name.startsWith("minecraft:light_block_")) {
                int level;
                try {
                    level = Integer.parseInt(name.substring("minecraft:light_block_".length()));
                } catch (NumberFormatException exception) {
                    break;
                }
                NbtMapBuilder states = tag.getCompound("states").toBuilder();
                states.putInt("block_light_level", level);
                tag = ConversionHelper.withId(tag, "minecraft:light_block")
                    .toBuilder().putCompound("states", states.build()).build();
                name = "minecraft:light_block";
                continue;
            }

            CoralFan coralFan = CORAL_FANS.get(name);
            if (coralFan != null) {
                NbtMapBuilder states = tag.getCompound("states").toBuilder();
                states.putByte("coral_hang_type_bit", (byte) (coralFan.hangTypeBit() ? 1 : 0));
                states.putByte("dead_bit", (byte) (coralFan.dead() ? 1 : 0));
                tag = ConversionHelper.withId(tag, coralFan.identifier())
                    .toBuilder().putCompound("states", states.build()).build();
                name = coralFan.identifier();
                continue;
            }

            // 1.21.50 and older used a numeric direction for doors and fence gates.
            NbtMap rawStates = tag.getCompound("states");
            if ((name.endsWith("_door") || name.endsWith("_fence_gate") || name.equals("minecraft:wooden_door")
                || name.equals("minecraft:fence_gate")) && rawStates.containsKey("minecraft:cardinal_direction")) {
                String direction = (String) rawStates.get("minecraft:cardinal_direction");
                NbtMapBuilder states = rawStates.toBuilder();
                states.remove("minecraft:cardinal_direction");
                states.putInt("direction", switch (direction) {
                    case "south" -> 0;
                    case "west" -> 1;
                    case "north" -> 2;
                    case "east" -> 3;
                    default -> 0;
                });
                tag = tag.toBuilder().putCompound("states", states.build()).build();
            }

            StateRename stateRename = STATE_RENAMES.get(name);
            if (stateRename != null) {
                tag = ConversionHelper.withId(tag, stateRename.identifier());
                if (stateRename.state() != null) {
                    NbtMapBuilder states = tag.getCompound("states").toBuilder();
                    states.put(stateRename.state(), stateRename.value());
                    tag = tag.toBuilder().putCompound("states", states.build()).build();
                }
                name = stateRename.identifier();
                continue;
            }

            if (this.names.contains(name)) {
                break;
            }

            String replacement = resolve(name);
            if (replacement == null || replacement.equals(name)) {
                break;
            }
            tag = ConversionHelper.withId(tag, replacement);
            name = replacement;
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
        if (direct != null) {
            return direct;
        }

        StateRename rename = STATE_RENAMES.get(name);
        if (rename != null) {
            return rename.identifier();
        }

        if (name.startsWith("minecraft:light_block_")) {
            return "minecraft:light_block";
        }

        CoralFan coralFan = CORAL_FANS.get(name);
        if (coralFan != null) {
            return coralFan.identifier();
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
        } else if (name.endsWith("_stairs")) {
            return "minecraft:oak_stairs";
        } else if (name.endsWith("_slab")) {
            return "minecraft:oak_slab";
        } else if (name.endsWith("_wall")) {
            return "minecraft:cobblestone_wall";
        } else if (name.endsWith("_trapdoor")) {
            return "minecraft:trapdoor";
        } else if (name.endsWith("_door")) {
            return "minecraft:wooden_door";
        } else if (name.endsWith("_fence_gate")) {
            return "minecraft:fence_gate";
        } else if (name.endsWith("_pressure_plate")) {
            return "minecraft:wooden_pressure_plate";
        } else if (name.endsWith("_button")) {
            return "minecraft:wooden_button";
        } else if (name.endsWith("_lightning_rod")) {
            return "minecraft:lightning_rod";
        } else if (name.endsWith("_golem_statue")) {
            return "minecraft:air";
        } else if (name.contains("copper") && name.endsWith("_chest")) {
            return "minecraft:chest";
        } else if (name.contains("cinnabar") && name.endsWith("_slab")) {
            return "minecraft:mangrove_slab";
        } else if (name.contains("cinnabar") && name.endsWith("_stairs")) {
            return "minecraft:mangrove_stairs";
        } else if (name.contains("cinnabar") && name.endsWith("_wall")) {
            return "minecraft:red_nether_brick_wall";
        } else if (name.contains("cinnabar")) {
            return "minecraft:red_concrete";
        } else if (name.contains("sulfur") && name.endsWith("_slab")) {
            return "minecraft:bamboo_slab";
        } else if (name.contains("sulfur") && name.endsWith("_stairs")) {
            return "minecraft:bamboo_stairs";
        } else if (name.contains("sulfur") && name.endsWith("_wall")) {
            return "minecraft:end_stone_brick_wall";
        } else if (name.contains("sulfur")) {
            return "minecraft:yellow_concrete";
        }

        String wilderness = WildernessBoundConverter.blockFallback(name);
        if (wilderness != null) {
            return resolve(wilderness);
        }

        return null;
    }

    private record StateRename(String identifier, String state, Object value) {
    }

    private record CoralFan(String identifier, boolean hangTypeBit, boolean dead) {
    }
}
