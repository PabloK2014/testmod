package net.xach.testmod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;
import java.util.logging.Logger;

public class ActiveSkillSelectionPacket {
    private static final Logger LOGGER = Logger.getLogger(TestMod.MOD_ID);
    private final String skillId;

    public ActiveSkillSelectionPacket(String skillId) {
        this.skillId = skillId;
    }

    public ActiveSkillSelectionPacket(FriendlyByteBuf buf) {
        this.skillId = buf.readUtf(32767);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(skillId, 32767);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(TestMod.PlayerClassCapability.CAPABILITY).ifPresent(cap -> {
                    // Проверяем, что навык существует и принадлежит классу игрока
                    boolean isValidSkill = SkillTreeHandler.CLASS_SKILL_TREES.getOrDefault(cap.getPlayerClass(), null)
                            .getAllSkills().stream()
                            .anyMatch(skill -> skill.getId().equals(skillId) && (skill.getType() == SkillTreeHandler.SkillType.ACTIVE || skill.getType() == SkillTreeHandler.SkillType.GLOBAL));
                    if (isValidSkill) {
                        cap.setActiveSkill(skillId);
                        cap.sync(player);
                        LOGGER.info("Set active skill to " + skillId + " for player: " + player.getName().getString());
                    } else {
                        LOGGER.warning("Invalid active skill selected: " + skillId + " for class: " + cap.getPlayerClass());
                        player.sendSystemMessage(Component.literal("Недопустимый навык: " + skillId));
                    }
                });
            } else {
                LOGGER.warning("No player found for active skill selection");
            }
        });
        ctx.get().setPacketHandled(true);
    }
}