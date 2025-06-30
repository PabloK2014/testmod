package net.xach.testmod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class SkillActivationPacket {


    public SkillActivationPacket() {
    }

    public SkillActivationPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {

                return;
            }

            player.getCapability(TestMod.PlayerClassCapability.CAPABILITY).ifPresent(cap -> {
                String activeSkill = cap.getActiveSkill();
                String playerClass = cap.getPlayerClass();


                if (activeSkill.isEmpty()) {

                    player.sendSystemMessage(Component.literal("Выберите активный навык в меню (клавиша L)."));
                    return;
                }

                if (playerClass.equals("yandex.go")) {
                    switch (activeSkill) {
                        case "sprint_boost":
                            if (cap.getSkillLevel("sprint_boost") > 0 && cap.getSurgeEnergy() >= 20) {
                                YandexGoSkillHandler.activateSprintBoost(player, cap);
                            } else {
                                player.sendSystemMessage(Component.literal("Недостаточно энергии Surge (" + cap.getSurgeEnergy() + "/20) или навык не изучен."));

                            }
                            break;
                        case "speed_surge":
                            if (cap.getSkillLevel("speed_surge") > 0 && cap.getSurgeEnergy() >= 50) {
                                YandexGoSkillHandler.activateSpeedSurge(player, cap);
                            } else {
                                player.sendSystemMessage(Component.literal("Недостаточно энергии Surge (" + cap.getSurgeEnergy() + "/50) или навык не изучен."));

                            }
                            break;
                        case "inventory_surge":
                            if (cap.getSkillLevel("inventory_surge") > 0 && cap.getSurgeEnergy() >= 50 && !cap.hasTradeHandler()) {
                                YandexGoSkillHandler.activateInventorySurge(player, cap);
                                cap.setTradeHandler(true);
                            } else {
                                player.sendSystemMessage(Component.literal("Недостаточно энергии Surge (" + cap.getSurgeEnergy() + "/50), навык не изучен или уже активирован."));

                            }
                            break;
                        case "carry_surge":
                            if (cap.getSkillLevel("carry_surge") > 0 && cap.getSurgeEnergy() >= 50) {
                                YandexGoSkillHandler.activateCarrySurge(player, cap);
                            } else {
                                player.sendSystemMessage(Component.literal("Недостаточно энергии Surge (" + cap.getSurgeEnergy() + "/50) или навык не изучен."));

                            }
                            break;
                        default:

                            player.sendSystemMessage(Component.literal("Неизвестный активный навык: " + activeSkill));
                    }
                } else if (playerClass.equals("war")) {
                    switch (activeSkill) {
                        case "mad_boost":
                            if (cap.getSkillLevel("mad_boost") > 0 && cap.getSurgeEnergy() >= 20) {
                                WarSkillHandler.activateMadBoost(player, cap);
                            } else {
                                player.sendSystemMessage(Component.literal("Недостаточно энергии Surge (" + cap.getSurgeEnergy() + "/20) или навык не изучен."));

                            }
                            break;
                        case "indestructibility":
                            if (cap.getSkillLevel("indestructibility") > 0 && cap.getSurgeEnergy() >= 20 && player.getHealth() <= player.getMaxHealth() * 0.3f) {
                                WarSkillHandler.activateIndestructibility(player, cap);
                            } else {
                                player.sendSystemMessage(Component.literal("Недостаточно энергии Surge (" + cap.getSurgeEnergy() + "/20), здоровья или навык не изучен."));

                            }
                            break;
                        case "dagestan":
                            if (cap.getSkillLevel("dagestan") > 0 && cap.getSurgeEnergy() >= 50) {
                                WarSkillHandler.activateDagestan(player, cap);
                            } else {
                                player.sendSystemMessage(Component.literal("Недостаточно энергии Surge (" + cap.getSurgeEnergy() + "/50) или навык не изучен."));

                            }
                            break;
                        default:

                            player.sendSystemMessage(Component.literal("Неизвестный активный навык: " + activeSkill));
                    }
                } else {

                    player.sendSystemMessage(Component.literal("Ваш класс не поддерживает активные навыки."));
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}