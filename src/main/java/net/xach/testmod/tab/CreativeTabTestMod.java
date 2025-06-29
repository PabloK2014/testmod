package net.xach.testmod.tab;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.xach.testmod.TestMod;
import net.xach.testmod.block.TestModBlocks;
import net.xach.testmod.items.TestModItems;

public class CreativeTabTestMod extends CreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TestMod.MOD_ID);

    protected CreativeTabTestMod(Builder builder) {
        super(builder);
    }

    public static final RegistryObject<CreativeModeTab> MAGIC_TAB = CREATIVE_MODE_TABS.register("magic_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(TestModItems.STRAWBERRY.get()))
                    .title(Component.translatable("TestMod"))
                    .displayItems((pParameters, output) -> {
                        output.accept(TestModItems.STRAWBERRY.get());
                        output.accept(TestModItems.STRAWBERRY_SEEDS.get());
                        output.accept(TestModBlocks.MAGIC_LOG.get());
                        output.accept(TestModBlocks.MAGIC_LEAVES.get());
                        output.accept(TestModBlocks.MAGIC_PLANKS.get());
                        output.accept(TestModBlocks.MAGIC_WOOD.get());
                        output.accept(TestModItems.MAGIC_SAPLING.get());
                        output.accept(TestModItems.STRAWBERRY.get());
                    })
                    .build());
}
