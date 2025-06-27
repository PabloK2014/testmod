package net.xach.testmod.datagen;


import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.registries.RegistryObject;
import net.xach.testmod.block.ModBlocks;
import net.xach.testmod.block.StrawberryCrop;
import net.xach.testmod.items.ModItems;


import java.util.Map;
import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
    public ModBlockLootTableProvider() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    protected ModBlockLootTableProvider(HolderLookup.Provider pRegistries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), (Map<ResourceLocation, LootTable.Builder>) pRegistries);
    }



    @Override
    protected void generate() {

        LootItemCondition.Builder lootItemConditionBuilder = LootItemBlockStatePropertyCondition.hasBlockStateProperties(ModBlocks.STRAWBERRY_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StrawberryCrop.AGE, StrawberryCrop.MAX_AGE));

        this.add(ModBlocks.STRAWBERRY_CROP.get(), this.createCropDrops(ModBlocks.STRAWBERRY_CROP.get(),
                ModItems.STRAWBERRY.get(), ModItems.STRAWBERRY_SEEDS.get(), lootItemConditionBuilder));

        this.dropSelf(ModBlocks.MAGIC_LOG.get());
        this.dropSelf(ModBlocks.MAGIC_WOOD.get());
        this.dropSelf(ModBlocks.STRIPPED_MAGIC_LOG.get());
        this.dropSelf(ModBlocks.STRIPPED_MAGIC_WOOD.get());
        this.dropSelf(ModBlocks.MAGIC_PLANKS.get());
        this.dropSelf(ModBlocks.MAGIC_SAPLING.get());

        this.add(ModBlocks.MAGIC_LEAVES.get(), block ->
                createLeavesDrops(block, ModBlocks.MAGIC_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));
    }





    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}