package net.xach.testmod.worldgen.tree;

import net.xach.testmod.TestMod;
import net.xach.testmod.worldgen.ModConfiguredFeatures;
import net.minecraft.world.level.block.grower.TreeGrower;

import java.util.Optional;

public class ModTreeGrowers {
    public static final TreeGrower WALNUT = new TreeGrower(TestMod.MOD_ID + ":magic",
            Optional.empty(), Optional.of(ModConfiguredFeatures.MAGIC_KEY), Optional.empty());
}

