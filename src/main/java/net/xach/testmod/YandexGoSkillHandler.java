package net.xach.testmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Mod.EventBusSubscriber(modid = TestMod.MOD_ID)
public class YandexGoSkillHandler {
    private static final Logger LOGGER = Logger.getLogger(TestMod.MOD_ID);

    // Уникальные идентификаторы для модификаторов атрибутов
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("a2b3c4d5-e6f7-4a9b-0c1d-2e3f4a5b6c7d");
    private static final UUID HUNGER_MODIFIER_UUID = UUID.fromString("b3c4d5e6-f7a8-4b9c-1d0e-3f4a5b6c7d8e");

    // Регистрация команды при старте сервера
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getServer().getCommands().getDispatcher();
        dispatcher.register(
                Commands.literal("testmod")
                        .then(Commands.literal("speed")
                                .executes(YandexGoSkillHandler::executeSpeedCommand))
        );
    }

    // Команда для проверки скорости и уровня скилла
    private static int executeSpeedCommand(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            player.getCapability(TestMod.PlayerClassCapability.CAPABILITY).ifPresent(cap -> {
                if (cap.getPlayerClass().equals("yandex.go")) {
                    int speedLevel = cap.getSkillLevel("speed_basic");
                    double speed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
                    player.sendSystemMessage(Component.literal(
                            "Уровень скилла speed_basic: " + speedLevel + ", Скорость: " + String.format("%.3f", speed)
                    ));
                } else {
                    player.sendSystemMessage(Component.literal("Вы не являетесь классом yandex.go"));
                }
            });
        }
        return 1;
    }

    // Обработка нажатия клавиш активации скиллов и открытия меню
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (ModKeyBindings.ACTIVATE_SKILL.consumeClick()) {
                TestMod.NETWORK.send(PacketDistributor.SERVER.noArg(), new SkillActivationPacket());
                LOGGER.info("Skill activation key (K) pressed");
            }
            if (ModKeyBindings.OPEN_ACTIVE_SKILL_MENU.consumeClick()) {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player != null) {
                    LOGGER.info("Open active skill menu key (L) pressed for player: " + minecraft.player.getName().getString());
                    minecraft.player.getCapability(TestMod.PlayerClassCapability.CAPABILITY).ifPresent(cap -> {
                        // Проверяем, есть ли у игрока активные или глобальные навыки
                        List<SkillTreeHandler.Skill> availableSkills = SkillTreeHandler.CLASS_SKILL_TREES.getOrDefault(cap.getPlayerClass(), null)
                                .getAllSkills().stream()
                                .filter(skill -> (skill.getType() == SkillTreeHandler.SkillType.ACTIVE || skill.getType() == SkillTreeHandler.SkillType.GLOBAL) && cap.getSkillLevel(skill.getId()) > 0)
                                .toList();
                        if (!availableSkills.isEmpty()) {
                            minecraft.setScreen(new ActiveSkillSelectionScreen(
                                    new ActiveSkillSelectionMenu(0, minecraft.player.getInventory()),
                                    minecraft.player.getInventory(),
                                    Component.literal("Выбор активного навыка")
                            ));
                            LOGGER.info("Opening active skill selection menu for player: " + minecraft.player.getName().getString() + ", class: " + cap.getPlayerClass());
                        } else {
                            LOGGER.info("No active or global skills available for player class: " + cap.getPlayerClass());
                            minecraft.player.sendSystemMessage(Component.literal("Нет доступных активных навыков для вашего класса."));
                        }
                    });
                } else {
                    LOGGER.warning("No player found when trying to open active skill menu");
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide()) {
            Player player = event.player;
            player.getCapability(TestMod.PlayerClassCapability.CAPABILITY).ifPresent(cap -> {
                if (!cap.getPlayerClass().equals("yandex.go")) return;

                // Пассивные скиллы ветки speedBranch
                int speedLevel = cap.getSkillLevel("speed_basic");
                if (speedLevel > 0) {
                    applySpeedBoost(player, speedLevel);
                }

                int hungerReductionLevel = cap.getSkillLevel("hunger_reduction");
                if (hungerReductionLevel > 0) {
                    applyHungerReduction(player, hungerReductionLevel);
                }

                // Пассивные скиллы ветки inventoryBranch
                int inventorySlotsLevel = cap.getSkillLevel("inventory_slots_basic");
                if (inventorySlotsLevel > 0) {
                    applyInventorySlots(player, inventorySlotsLevel);
                }

                int craftingEfficiencyLevel = cap.getSkillLevel("crafting_efficiency");
                if (craftingEfficiencyLevel > 0 && !cap.hasCraftingHandler()) {
                    applyCraftingEfficiency(player, craftingEfficiencyLevel);
                    cap.setCraftingHandler(true); // Предотвращаем повторную регистрацию
                }

                // Пассивные скиллы ветки carryBranch
                int carryCapacityLevel = cap.getSkillLevel("carry_capacity_basic");
                if (carryCapacityLevel > 0 && !cap.hasTrapHandler()) {
                    applyCarryCapacityTrap(player, carryCapacityLevel);
                    cap.setTrapHandler(true); // Предотвращаем повторную регистрацию
                }

                int shulkerCarryLevel = cap.getSkillLevel("shulker_carry");
                if (shulkerCarryLevel > 0) {
                    applyShulkerCarry(player);
                }
            });
        }
    }

    // Скорость передвижения +5% за уровень
    public static void applySpeedBoost(Player player, int level) {
        AttributeModifier speedModifier = new AttributeModifier(
                SPEED_MODIFIER_UUID,
                "yandex_speed_boost",
                0.05 * level, // Увеличение скорости на 5% за уровень
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
        player.getAttribute(Attributes.MOVEMENT_SPEED)
                .removeModifier(SPEED_MODIFIER_UUID);
        player.getAttribute(Attributes.MOVEMENT_SPEED)
                .addPermanentModifier(speedModifier);
    }

    // Снижение расхода голода на 10% за уровень
    private static void applyHungerReduction(Player player, int level) {
        // Эмулируем снижение расхода голода через периодическое восстановление
        if (player.tickCount % (20 * 10) == 0) { // Каждые 10 секунд
            player.getFoodData().addExhaustion(-0.1f * level); // Уменьшаем истощение
            LOGGER.info("Reduced hunger exhaustion for player: " + player.getName().getString() + ", level: " + level);
        }
    }

    // Активный скилл: Рывок (sprint_boost)
    public static void activateSprintBoost(Player player, TestMod.PlayerClassCapability cap) {
        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED,
                200, // 10 секунд (20 тиков/сек * 10 сек)
                2, // Уровень 3 для значительного ускорения
                false,
                true
        ));
        cap.useSurgeEnergy(20);
        cap.sync((ServerPlayer) player);
        LOGGER.info("Activated Sprint Boost for player: " + player.getName().getString());
    }

    // Глобальный скилл: Всплеск скорости (speed_surge)
    public static void activateSpeedSurge(Player player, TestMod.PlayerClassCapability cap) {
        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED,
                600, // 30 секунд
                3, // Уровень 4 для мощного ускорения
                false,
                true
        ));
        player.addEffect(new MobEffectInstance(
                MobEffects.SATURATION,
                600, // Восстановление голода
                1,
                false,
                true
        ));
        cap.useSurgeEnergy(50);
        cap.sync((ServerPlayer) player);
        LOGGER.info("Activated Speed Surge for player: " + player.getName().getString());
    }

    // Дополнительные слоты инвентаря
    private static void applyInventorySlots(Player player, int level) {
        // Эмулируем дополнительные слоты через бафф хранения (например, увеличение переносимого веса)
        LOGGER.info("Applied inventory slots bonus for player: " + player.getName().getString() + ", level: " + level);
    }

    // Увеличение количества стаков
    private static void applyCraftingEfficiency(Player player, int level) {
        // Увеличиваем выход предметов при крафте
        MinecraftForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void onCrafting(net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent event) {
                if (event.getEntity() instanceof Player eventPlayer && eventPlayer == player && !event.getCrafting().isEmpty()) {
                    ItemStack result = event.getCrafting();
                    int bonusAmount = level; // Дополнительный предмет за уровень
                    ItemStack bonusStack = result.copy();
                    bonusStack.setCount(bonusAmount);
                    player.getInventory().add(bonusStack);
                    LOGGER.info("Added crafting efficiency bonus for player: " + player.getName().getString() + ", bonus: " + bonusAmount);
                }
            }
        });
    }

    // Глобальный скилл: Улыбка Курьера (inventory_surge)
    public static void activateInventorySurge(Player player, TestMod.PlayerClassCapability cap) {
        // Скидка 15% на товары при торговле с жителями
        MinecraftForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void onVillagerTrade(PlayerInteractEvent.EntityInteract event) {
                if (event.getTarget() instanceof Villager villager && event.getEntity() == player) {
                    villager.getOffers().forEach(offer -> {
                        ItemStack costA = offer.getCostA();
                        ItemStack costB = offer.getCostB();
                        if (!costA.isEmpty()) {
                            int newCountA = Math.max(1, (int) (costA.getCount() * 0.85)); // Скидка 15%
                            costA.setCount(newCountA);
                        }
                        if (!costB.isEmpty()) {
                            int newCountB = Math.max(1, (int) (costB.getCount() * 0.85)); // Скидка 15%
                            costB.setCount(newCountB);
                        }
                    });
                    LOGGER.info("Applied 15% trade discount for player: " + player.getName().getString());
                }
            }
        });
        cap.useSurgeEnergy(50);
        cap.sync((ServerPlayer) player);
        LOGGER.info("Activated Inventory Surge for player: " + player.getName().getString());
    }

    // Установка невидимого триггера (ловушки)
    private static void applyCarryCapacityTrap(Player player, int level) {
        // Эмулируем установку ловушки через событие взаимодействия
        MinecraftForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
                if (event.getEntity() == player) {
                    Level levelWorld = event.getLevel();
                    AABB trapArea = new AABB(event.getPos()).inflate(1.0);
                    List<LivingEntity> entities = levelWorld.getEntitiesOfClass(LivingEntity.class, trapArea, entity -> entity != player);
                    for (LivingEntity entity : entities) {
                        entity.addEffect(new MobEffectInstance(
                                MobEffects.MOVEMENT_SLOWDOWN,
                                100, // 5 секунд оглушения
                                5, // Высокий уровень для "оглушения"
                                false,
                                true
                        ));
                    }
                    LOGGER.info("Placed trap by player: " + player.getName().getString());
                }
            }
        });
    }

    // Видимость деревень и торговцев
    private static void applyShulkerCarry(Player player) {
        // Эмулируем отображение деревень и торговцев через эффект свечения
        Level level = player.level();
        AABB searchArea = new AABB(player.blockPosition()).inflate(500);
        List<LivingEntity> villagers = level.getEntitiesOfClass(LivingEntity.class, searchArea,
                entity -> entity instanceof net.minecraft.world.entity.npc.Villager);
        for (LivingEntity villager : villagers) {
            villager.addEffect(new MobEffectInstance(
                    MobEffects.GLOWING,
                    200, // 10 секунд подсветки
                    0,
                    false,
                    true
            ));
        }
        LOGGER.info("Applied Shulker Carry village detection for player: " + player.getName().getString());
    }

    // Глобальный скилл: Граната с перцем
    public static void activateCarrySurge(Player player, TestMod.PlayerClassCapability cap) {
        // Разблокируем рецепт Fire Charge (как заглушка для перцового баллона)
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.server.getRecipeManager().byKey(new ResourceLocation("minecraft", "fire_charge"))
                    .ifPresent(recipe -> serverPlayer.awardRecipes(java.util.Collections.singleton(recipe)));
            ItemStack pepperGrenade = new ItemStack(Items.FIRE_CHARGE, 1);
            player.getInventory().add(pepperGrenade);
            cap.useSurgeEnergy(50);
            cap.sync(serverPlayer);
            LOGGER.info("Activated Carry Surge (Pepper Grenade) and unlocked recipe for player: " + player.getName().getString());
        }
    }
}