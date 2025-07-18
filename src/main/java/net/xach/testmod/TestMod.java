package net.xach.testmod;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.Registries; // Добавляем для CREATIVE_MODE_TAB
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.xach.testmod.block.ModBlocks;
import net.xach.testmod.items.ModItems;
import org.apache.logging.log4j.LogManager; // Используем Log4j
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Mod(TestMod.MOD_ID)
public class TestMod {
    public static final String MOD_ID = "testmod";
    private static final Logger LOGGER = LogManager.getLogger(MOD_ID); // Заменили на Log4j
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );


    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final RegistryObject<CreativeModeTab> TESTMOD_TAB = CREATIVE_TABS.register("testmod_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.testmod"))
                    .icon(() -> new ItemStack(ModItems.STRAWBERRY.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.STRAWBERRY.get());
                        output.accept(ModItems.STRAWBERRY_SEEDS.get());
                        LOGGER.info("Added Strawberry to TestMod tab");
                    })
                    .build());

    public TestMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::clientSetup);
        bus.addListener(this::registerCapabilities);
        bus.addListener(this::setup);
        MenuRegistry.register(bus);
        ModBlocks.register(bus);
        ModItems.register(bus);
        CREATIVE_TABS.register(bus);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(WarSkillHandler.class);

        int id = 0;
        NETWORK.registerMessage(id++, ClassSelectionPacket.class, ClassSelectionPacket::toBytes, ClassSelectionPacket::new, ClassSelectionPacket::handle);
        NETWORK.registerMessage(id++, PlayerDataSyncPacket.class, PlayerDataSyncPacket::toBytes, PlayerDataSyncPacket::new, PlayerDataSyncPacket::handle);
        NETWORK.registerMessage(id++, OpenSkillTreePacket.class, OpenSkillTreePacket::toBytes, OpenSkillTreePacket::new, OpenSkillTreePacket::handle);
        NETWORK.registerMessage(id++, ActiveSkillSelectionPacket.class, ActiveSkillSelectionPacket::toBytes, ActiveSkillSelectionPacket::new, ActiveSkillSelectionPacket::handle);
        NETWORK.registerMessage(id++, OpenClassSelectionPacket.class, OpenClassSelectionPacket::toBytes, OpenClassSelectionPacket::new, OpenClassSelectionPacket::handle);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("Registering network packets for channel testmod:main");
            int id = 0;
            NETWORK.registerMessage(id++, SkillActivationPacket.class,
                    SkillActivationPacket::toBytes,
                    SkillActivationPacket::new,
                    SkillActivationPacket::handle);
            NETWORK.registerMessage(id++, ClassSelectionPacket.class,
                    ClassSelectionPacket::toBytes,
                    ClassSelectionPacket::new,
                    ClassSelectionPacket::handle);
            NETWORK.registerMessage(id++, PlayerDataSyncPacket.class,
                    PlayerDataSyncPacket::toBytes,
                    PlayerDataSyncPacket::new,
                    PlayerDataSyncPacket::handle);
            NETWORK.registerMessage(id++, SkillUpgradePacket.class,
                    SkillUpgradePacket::toBytes,
                    SkillUpgradePacket::new,
                    SkillUpgradePacket::handle);
            NETWORK.registerMessage(id++, OpenSkillTreePacket.class,
                    OpenSkillTreePacket::toBytes,
                    OpenSkillTreePacket::new,
                    OpenSkillTreePacket::handle);
            NETWORK.registerMessage(id++, ActiveSkillSelectionPacket.class,
                    ActiveSkillSelectionPacket::toBytes,
                    ActiveSkillSelectionPacket::new,
                    ActiveSkillSelectionPacket::handle);
            LOGGER.info("Network packets registered successfully");
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(MenuRegistry.CLASS_SELECTION.get(), ClassSelectionScreen::new);
            MenuScreens.register(MenuRegistry.SKILL_TREE.get(), SkillTreeScreen::new);
            MenuScreens.register(MenuRegistry.ACTIVE_SKILL_SELECTION.get(), ActiveSkillSelectionScreen::new);
        });
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerClassCapability.class);
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            Player player = event.getEntity();
            player.getCapability(PlayerClassCapability.CAPABILITY).ifPresent(cap -> {
                LOGGER.info("Player joined with class: " + cap.getPlayerClass() + ", level: " + cap.getLevel() + ", exp: " + cap.getExperience() + ", points: " + cap.getSkillPoints() + ", active skill: " + cap.getActiveSkill());
                if (cap.getPlayerClass().isEmpty()) {
                    LOGGER.info("Opening class selection screen for player: " + player.getName().getString());
                    NetworkHooks.openScreen((ServerPlayer) player, new MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return Component.literal("Select Class");
                        }

                        @Override
                        public ClassSelectionMenu createMenu(int id, Inventory inv, Player player) {
                            return new ClassSelectionMenu(id, inv);
                        }
                    });
                } else {
                    cap.sync((ServerPlayer) player);
                }
            });
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.getEntity().level().isClientSide()) {
            Player original = event.getOriginal();
            Player newPlayer = event.getEntity();
            LOGGER.info("Player clone event triggered for player: " + newPlayer.getName().getString());
            original.reviveCaps();
            original.getCapability(PlayerClassCapability.CAPABILITY).ifPresent(oldCap -> {
                newPlayer.getCapability(PlayerClassCapability.CAPABILITY).ifPresent(newCap -> {
                    CompoundTag nbt = oldCap.serializeNBT();
                    LOGGER.info("Cloning player capability: class=" + oldCap.getPlayerClass() + ", level=" + oldCap.getLevel() + ", exp=" + oldCap.getExperience() + ", points=" + oldCap.getSkillPoints() + ", active skill: " + oldCap.getActiveSkill());
                    newCap.deserializeNBT(nbt);
                    newCap.sync((ServerPlayer) newPlayer);
                    LOGGER.info("Cloned to new player: class=" + newCap.getPlayerClass() + ", level=" + newCap.getLevel() + ", exp=" + newCap.getExperience() + ", points=" + newCap.getSkillPoints() + ", active skill: " + newCap.getActiveSkill());
                });
            });
            original.invalidateCaps();
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            player.getCapability(PlayerClassCapability.CAPABILITY).ifPresent(cap -> {
                LOGGER.info("Player respawned with class: " + cap.getPlayerClass() + ", level: " + cap.getLevel() + ", exp: " + cap.getExperience() + ", points: " + cap.getSkillPoints() + ", active skill: " + cap.getActiveSkill());
                cap.sync(player);
            });
        }
    }

    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(MOD_ID, "player_class"), new PlayerClassCapability.Provider());
        }
    }

    public static class PlayerClassCapability implements INBTSerializable<CompoundTag> {
        public static final Capability<PlayerClassCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
        private boolean hasTradeHandler = false;
        private boolean hasCraftingHandler = false;
        private boolean hasTrapHandler = false;
        private String playerClass = "";
        private int level = 1;
        private int experience = 0;
        private int skillPoints = 1;
        private int surgeEnergy = 100;
        private String activeSkill = "";
        private final Map<String, Integer> skillLevels = new HashMap<>();

        public int getSurgeEnergy() {
            return surgeEnergy;
        }

        public boolean hasTradeHandler() {
            return hasTradeHandler;
        }

        public void setTradeHandler(boolean value) {
            hasTradeHandler = value;
        }

        public boolean hasCraftingHandler() {
            return hasCraftingHandler;
        }

        public void setCraftingHandler(boolean value) {
            hasCraftingHandler = value;
        }

        public boolean hasTrapHandler() {
            return hasTrapHandler;
        }

        public void setTrapHandler(boolean value) {
            hasTrapHandler = value;
        }

        public void setSurgeEnergy(int surgeEnergy) {
            this.surgeEnergy = Math.max(0, Math.min(100, surgeEnergy));
            LOGGER.info("Set surge energy to: " + this.surgeEnergy);
        }

        public void useSurgeEnergy(int amount) {
            setSurgeEnergy(surgeEnergy - amount);
        }

        public void addSurgeEnergy(int amount) {
            setSurgeEnergy(surgeEnergy + amount);
        }

        public String getPlayerClass() {
            return playerClass;
        }

        public void setPlayerClass(String playerClass) {
            this.playerClass = playerClass != null ? playerClass : "";
            LOGGER.info("Set player class to: " + this.playerClass + " (bytes: " + this.playerClass.getBytes(StandardCharsets.UTF_8).length + ")");
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
            this.skillPoints = level;
        }

        public int getExperience() {
            return experience;
        }

        public void setExperience(int experience) {
            this.experience = experience;
        }

        public void addExperience(int amount) {
            this.experience += amount;
            while (this.experience >= getExperienceForNextLevel()) {
                this.experience -= getExperienceForNextLevel();
                this.level++;
                this.skillPoints++;
            }
        }

        private int getExperienceForNextLevel() {
            return 100 * level;
        }

        public int getSkillPoints() {
            return skillPoints;
        }

        public void setSkillPoints(int skillPoints) {
            this.skillPoints = skillPoints;
        }

        public void spendSkillPoint() {
            if (skillPoints > 0) {
                skillPoints--;
            }
        }

        public int getSkillLevel(String skillId) {
            return skillLevels.getOrDefault(skillId, 0);
        }

        public void setSkillLevel(String skillId, int level) {
            skillLevels.put(skillId, level);
        }

        public void clearSkillLevels() {
            skillLevels.clear();
        }

        public String getActiveSkill() {
            return activeSkill;
        }

        public void setActiveSkill(String skillId) {
            this.activeSkill = skillId != null ? skillId : "";
            LOGGER.info("Set active skill to: " + this.activeSkill);
        }

        public void sync(ServerPlayer player) {
            NETWORK.sendTo(
                    new PlayerDataSyncPacket(playerClass, level, experience, skillLevels, skillPoints, surgeEnergy, activeSkill),
                    player.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT
            );
            LOGGER.info("Syncing class to client: " + playerClass + ", level: " + level + ", exp: " + experience + ", points: " + skillPoints + ", surgeEnergy: " + surgeEnergy + ", active skill: " + activeSkill);
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            LOGGER.info("Serializing player class: " + playerClass + ", level: " + level + ", exp: " + experience + ", points: " + skillPoints + ", surgeEnergy: " + surgeEnergy + ", active skill: " + activeSkill);
            tag.putString("playerClass", playerClass);
            tag.putInt("level", level);
            tag.putInt("experience", experience);
            tag.putInt("skillPoints", skillPoints);
            tag.putInt("surgeEnergy", surgeEnergy);
            tag.putString("activeSkill", activeSkill);
            CompoundTag skillsTag = new CompoundTag();
            skillLevels.forEach(skillsTag::putInt);
            tag.put("skills", skillsTag);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            playerClass = tag.getString("playerClass");
            level = tag.getInt("level");
            experience = tag.getInt("experience");
            skillPoints = tag.getInt("skillPoints");
            surgeEnergy = tag.getInt("surgeEnergy");
            activeSkill = tag.getString("activeSkill");
            CompoundTag skillsTag = tag.getCompound("skills");
            skillLevels.clear();
            skillsTag.getAllKeys().forEach(key -> skillLevels.put(key, skillsTag.getInt(key)));
            LOGGER.info("Deserialized player class: " + playerClass + ", level: " + level + ", exp: " + experience + ", points: " + skillPoints + ", surgeEnergy: " + surgeEnergy + ", active skill: " + activeSkill);
        }

        public static class Provider implements net.minecraftforge.common.capabilities.ICapabilitySerializable<CompoundTag> {
            private final PlayerClassCapability instance = new PlayerClassCapability();
            private final LazyOptional<PlayerClassCapability> optional = LazyOptional.of(() -> instance);

            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> cap, net.minecraft.core.Direction side) {
                return cap == CAPABILITY ? optional.cast() : LazyOptional.empty();
            }

            @Override
            public CompoundTag serializeNBT() {
                return instance.serializeNBT();
            }

            @Override
            public void deserializeNBT(CompoundTag nbt) {
                instance.deserializeNBT(nbt);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ClassSelectionScreen extends AbstractContainerScreen<ClassSelectionMenu> {
        public ClassSelectionScreen(ClassSelectionMenu menu, Inventory inventory, Component title) {
            super(menu, inventory, title);
        }

        @Override
        protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
            // Пустой фон, можно добавить текстуру
        }

        @Override
        protected void init() {
            super.init();
            int buttonWidth = 100;
            int buttonHeight = 20;
            int centerX = this.width / 2;
            int centerY = this.height / 2;

            this.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                            Component.literal("Воин"),
                            button -> selectClass("war"))
                    .pos(centerX - buttonWidth / 2, centerY - buttonHeight - 30)
                    .size(buttonWidth, buttonHeight)
                    .build());

            this.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                            Component.literal("Повар"),
                            button -> selectClass("cook"))
                    .pos(centerX - buttonWidth / 2, centerY - buttonHeight - 10)
                    .size(buttonWidth, buttonHeight)
                    .build());

            this.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                            Component.literal("Курьер Yandex.Go"),
                            button -> selectClass("yandex.go"))
                    .pos(centerX - buttonWidth / 2, centerY + 10)
                    .size(buttonWidth, buttonHeight)
                    .build());

            this.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                            Component.literal("Пивовар"),
                            button -> selectClass("pivo"))
                    .pos(centerX - buttonWidth / 2, centerY + buttonHeight + 30)
                    .size(buttonWidth, buttonHeight)
                    .build());
        }

        private void selectClass(String className) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.getCapability(PlayerClassCapability.CAPABILITY).ifPresent(cap -> {
                    cap.setPlayerClass(className);
                    this.minecraft.setScreen(null);
                    NETWORK.sendToServer(new ClassSelectionPacket(className));
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
    }
}