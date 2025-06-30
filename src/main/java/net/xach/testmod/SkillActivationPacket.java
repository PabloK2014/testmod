package net.xach.testmod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SkillActivationPacket {

    public SkillActivationPacket() {
    }

    public SkillActivationPacket(FriendlyByteBuf buf) {
        // Пакет не содержит данных
    }

    public void toBytes(FriendlyByteBuf buf) {
        // Пакет не содержит данных
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                player.getCapability(TestMod.PlayerClassCapability.CAPABILITY).ifPresent(cap -> {
                    String activeSkill = cap.getActiveSkill();
                    String playerClass = cap.getPlayerClass();

                    System.out.println("Skill activation packet received from player: " + player.getName().getString() +
                            ", Active skill: " + activeSkill + ", Class: " + playerClass);

                    if (activeSkill.isEmpty()) {
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Активный навык не выбран! Используйте клавишу L для выбора."));
                        return;
                    }

                    // Обработка навыков для класса "war"
                    if (playerClass.equals("war")) {
                        switch (activeSkill) {
                            case "mad_boost":
                                WarSkillHandler.activateMadBoost(player, cap);
                                break;
                            case "indestructibility":
                                WarSkillHandler.activateIndestructibility(player, cap);
                                break;
                            case "dagestan":
                                WarSkillHandler.activateDagestan(player, cap);
                                break;
                            default:
                                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Неизвестный навык: " + activeSkill));
                        }
                    }
                    // Обработка навыков для класса "yandex.go"
                    else if (playerClass.equals("yandex.go")) {
                        switch (activeSkill) {
                            case "sprint_boost":
                                YandexGoSkillHandler.activateSprintBoost(player, cap);
                                break;
                            case "speed_surge":
                                YandexGoSkillHandler.activateSpeedSurge(player, cap);
                                break;
                            case "inventory_surge":
                                YandexGoSkillHandler.activateInventorySurge(player, cap);
                                break;
                            case "carry_surge":
                                YandexGoSkillHandler.activateCarrySurge(player, cap);
                                break;
                            default:
                                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Неизвестный навык: " + activeSkill));
                        }
                    }
                    // Обработка навыков для класса "miner"
                    else if (playerClass.equals("miner")) {
                        switch (activeSkill) {
                            case "ore_highlight":
                                MinerSkillHandler.activateOreHighlight(player, cap);
                                break;
                            case "vein_miner":
                                MinerSkillHandler.activateVeinMiner(player, cap);
                                break;
                            case "night_vision":
                                MinerSkillHandler.activateNightVision(player, cap);
                                break;
                            default:
                                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Неизвестный навык: " + activeSkill));
                        }
                    }
                    // Добавьте обработку для других классов по необходимости
                    else {
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Активация навыков для класса " + playerClass + " пока не реализована."));
                    }
                });
            }
        });
        return true;
    }
}
