package net.xach.testmod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;
import java.util.logging.Logger;

public class OpenSkillTreePacket {
    private static final Logger LOGGER = Logger.getLogger(TestMod.MOD_ID);

    public OpenSkillTreePacket() {
        LOGGER.info("Created OpenSkillTreePacket");
    }

    public OpenSkillTreePacket(FriendlyByteBuf buf) {
        LOGGER.info("Decoded OpenSkillTreePacket");
    }

    public void toBytes(FriendlyByteBuf buf) {
        LOGGER.info("Encoded OpenSkillTreePacket");
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                LOGGER.info("Opening skill tree for player: " + player.getName().getString());
                NetworkHooks.openScreen(player, new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return Component.literal("Skill Tree");
                    }

                    @Override
                    public SkillTreeMenu createMenu(int id, Inventory inv, Player player) {
                        return new SkillTreeMenu(id, inv);
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}