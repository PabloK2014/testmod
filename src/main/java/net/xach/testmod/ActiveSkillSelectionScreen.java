package net.xach.testmod;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;


@OnlyIn(Dist.CLIENT)
public class ActiveSkillSelectionScreen extends AbstractContainerScreen<ActiveSkillSelectionMenu> {

    public ActiveSkillSelectionScreen(ActiveSkillSelectionMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        // Пустой фон
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = 100;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        if (minecraft != null && minecraft.player != null) {
            minecraft.player.getCapability(TestMod.PlayerClassCapability.CAPABILITY).ifPresent(cap -> {
                List<String> availableSkills = new ArrayList<>();
                String playerClass = cap.getPlayerClass();

                // Добавляем навыки в зависимости от класса игрока
                if (playerClass.equals("yandex.go")) {
                    if (cap.getSkillLevel("sprint_boost") > 0) availableSkills.add("sprint_boost");
                    if (cap.getSkillLevel("speed_surge") > 0) availableSkills.add("speed_surge");
                    if (cap.getSkillLevel("inventory_surge") > 0) availableSkills.add("inventory_surge");
                    if (cap.getSkillLevel("carry_surge") > 0) availableSkills.add("carry_surge");
                } else if (playerClass.equals("war")) {
                    if (cap.getSkillLevel("mad_boost") > 0) availableSkills.add("mad_boost");
                    if (cap.getSkillLevel("indestructibility") > 0) availableSkills.add("indestructibility");
                    if (cap.getSkillLevel("dagestan") > 0) availableSkills.add("dagestan");
                }

                // Создаем кнопки для каждого доступного навыка
                for (int i = 0; i < availableSkills.size(); i++) {
                    String skillId = availableSkills.get(i);
                    String skillName = switch (skillId) {
                        // Навыки для yandex.go
                        case "sprint_boost" -> "Рывок";
                        case "speed_surge" -> "Всплеск скорости";
                        case "inventory_surge" -> "Улыбка курьера";
                        case "carry_surge" -> "Перцовая граната";
                        // Навыки для war
                        case "mad_boost" -> "Безумный Рывок";
                        case "indestructibility" -> "Несокрушимость";
                        case "dagestan" -> "Дагестанская братва";
                        default -> skillId;
                    };
                    this.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                                    Component.literal(skillName),
                                    button -> selectActiveSkill(skillId))
                            .pos(centerX - buttonWidth / 2, centerY - buttonHeight * (availableSkills.size() / 2) + i * buttonHeight)
                            .size(buttonWidth, buttonHeight)
                            .build());
                }
            });
        }
    }

    private void selectActiveSkill(String skillId) {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.getCapability(TestMod.PlayerClassCapability.CAPABILITY).ifPresent(cap -> {
                cap.setActiveSkill(skillId);
                TestMod.NETWORK.sendToServer(new ActiveSkillSelectionPacket(skillId));
                minecraft.setScreen(null);
            });
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public static class FoodBagScreen extends AbstractContainerScreen<FoodBagMenu> {
        private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");

        public FoodBagScreen(FoodBagMenu menu, Inventory playerInventory, Component title) {
            super(menu, playerInventory, title);
            this.imageHeight = 168;
            this.inventoryLabelY = this.imageHeight - 94;
        }

        @Override
        protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int x = (this.width - this.imageWidth) / 2;
            int y = (this.height - this.imageHeight) / 2;
            guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            this.renderBackground(guiGraphics);
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            this.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }
}