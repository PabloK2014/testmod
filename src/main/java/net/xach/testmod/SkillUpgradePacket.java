package net.xach.testmod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;
import java.util.logging.Logger;

public class SkillUpgradePacket {
    private static final Logger LOGGER = Logger.getLogger(TestMod.MOD_ID);
    private final String skillId;

    public SkillUpgradePacket(String skillId) {
        this.skillId = skillId;
        LOGGER.info("Created SkillUpgradePacket for skill: " + skillId);
    }

    public SkillUpgradePacket(FriendlyByteBuf buf) {
        this.skillId = buf.readUtf(32767);
        LOGGER.info("Decoded SkillUpgradePacket for skill: " + skillId);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(skillId, 32767);
        LOGGER.info("Encoded SkillUpgradePacket for skill: " + skillId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(TestMod.PlayerClassCapability.CAPABILITY).ifPresent(cap -> {
                    if (cap.getSkillPoints() > 0) {
                        int currentLevel = cap.getSkillLevel(skillId);
                        cap.setSkillLevel(skillId, currentLevel + 1);
                        cap.spendSkillPoint();
                        cap.sync(player);
                        LOGGER.info("Upgraded skill " + skillId + " to level " + (currentLevel + 1) + " for player: " + player.getName().getString());
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}