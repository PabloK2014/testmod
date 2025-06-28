package net.xach.testmod.worldgen;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;
import net.xach.testmod.TestMod;

public class ModBiomeModifiers {
    public static final ResourceKey<BiomeModifier> ADD_MAGIC_TREE = registerKey("add_magic_tree");

    public static void bootstrap(BootstapContext<BiomeModifier> context) {
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);

        context.register(ADD_MAGIC_TREE,
                new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_FOREST), // Генерация в лесных биомах
                        HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.MAGIC_PLACED)),
                        GenerationStep.Decoration.VEGETAL_DECORATION
                ));
    }

    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, new ResourceLocation(TestMod.MOD_ID, name));
    }
}