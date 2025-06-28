package net.xach.testmod.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext; // Проверь, что это правильный импорт
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature; // Исправленный импорт
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.tags.BiomeTags;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xach.testmod.TestMod;

public class ModBiomeModifiers {
    public static final ResourceKey<BiomeModifier> ADD_MAGIC_TREE = registerKey("add_magic_tree");

    // Регистрация сериализатора
    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, TestMod.MOD_ID);

    public static final RegistryObject<Codec<ForgeBiomeModifiers.AddFeaturesBiomeModifier>> ADD_FEATURES_CODEC =
            BIOME_MODIFIER_SERIALIZERS.register("add_features",
                    () -> RecordCodecBuilder.create(builder -> builder.group(
                            Biome.LIST_CODEC.fieldOf("biomes").forGetter(ForgeBiomeModifiers.AddFeaturesBiomeModifier::biomes),
                            PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(ForgeBiomeModifiers.AddFeaturesBiomeModifier::features),
                            GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(ForgeBiomeModifiers.AddFeaturesBiomeModifier::step)
                    ).apply(builder, ForgeBiomeModifiers.AddFeaturesBiomeModifier::new)));

    public static void bootstrap(BootstapContext<BiomeModifier> context) {
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);

        TestMod.LOGGER.info("Registering BiomeModifier: add_magic_tree");
        context.register(ADD_MAGIC_TREE,
                new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_FOREST),
                        HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.MAGIC_PLACED)),
                        GenerationStep.Decoration.VEGETAL_DECORATION
                ));
    }

    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, new ResourceLocation(TestMod.MOD_ID, name));
    }
}