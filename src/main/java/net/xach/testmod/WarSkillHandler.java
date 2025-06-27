package net.xach.testmod;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

@Mod.EventBusSubscriber(modid = TestMod.MOD_ID)
public class WarSkillHandler {
    private static final Logger LOGGER = Logger.getLogger(TestMod.MOD_ID);

    // Уникальные идентификаторы для модификаторов атрибутов
    private static final UUID ATTACK_MODIFIER_UUID = UUID.fromString("c4d5e6f7-a8b9-4c0d-1e2f-3a4b5c6d7e8f");
    private static final UUID ATTACK_SPEED_MODIFIER_UUID = UUID.fromString("d5e6f7a8-b9c0-4d1e-2f3a-4b5c6d7e8f9a");
    private static final UUID DEFENSE_MODIFIER_UUID = UUID.fromString("e6f7a8b9-c0d1-4e2f-3a4b-5c6d7e8f9a0b");
    private static final UUID HEALTH_REGEN_MODIFIER_UUID = UUID.fromString("f7a8b9c0-d1e2-4f3a-5b6c-7d8e9f0a1b2c");
    private static final UUID CARRY_MODIFIER_UUID = UUID.fromString("a8b9c0d1-e2f3-4a5b-6c7d-8e9f0a1b2c3d");

    // Регистрация команды для проверки навыков Воина
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        var dispatcher = event.getServer().getCommands().getDispatcher();
        dispatcher.register(
                Commands.literal("war")
                        .then(Commands.literal("skills")
                                .executes(context -> executeSkillInfoCommand(context)))
        );
    }

    private static int executeSkillInfoCommand(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            player.getCapability(TestMod.PlayerClassCapability.CAPABILITY).ifPresent(cap -> {
                if (cap.getPlayerClass().equals("war")) {
                    StringBuilder skillsInfo = new StringBuilder("Навыки Воина:\n");
                    SkillTreeHandler.CLASS_SKILL_TREES.get("war").getAllSkills().forEach(skill -> {
                        int level = cap.getSkillLevel(skill.getId());
                        if (level > 0) {
                            skillsInfo.append(skill.getName()).append(": Уровень ").append(level).append("\n");
                        }
                    });
                    player.sendSystemMessage(Component.literal(skillsInfo.toString()));
                } else {
                    player.sendSystemMessage(Component.literal("Вы не Воин!"));
                }
            });
        }
        return 1;
    }

    // Обработка нажатия клавиши активации навыка
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && ModKeyBindings.ACTIVATE_SKILL.consumeClick()) {
            TestMod.NETWORK.send(PacketDistributor.SERVER.noArg(), new SkillActivationPacket());
        }
    }

    // Основной тик для обработки пассивных и активных навыков
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;

        Player player = event.player;
        player.getCapability(TestMod.PlayerClassCapability.CAPABILITY).ifPresent(cap -> {
            if (!cap.getPlayerClass().equals("war")) return;

            // Ветка Берсерка
            int berserkLevel = cap.getSkillLevel("berserk_way");
            if (berserkLevel > 0 && player.getHealth() <= player.getMaxHealth() * 0.3f) {
                applyBerserkWay(player, berserkLevel);
            }

            int bloodyWoundLevel = cap.getSkillLevel("bloody_wound");
            // Обработка кровотечения через событие атаки (см. ниже)

            // Ветка Защитника
            int ironLevel = cap.getSkillLevel("iron");
            if (ironLevel > 0) {
                applyIronWall(player, ironLevel);
            }

            int fortressLevel = cap.getSkillLevel("fortress");
            if (fortressLevel > 0 && player.tickCount % 20 == 0) { // Проверяем каждую секунду
                if (player.getDeltaMovement().x == 0 && player.getDeltaMovement().z == 0) {
                    applyFortress(player);
                }
            }

            // Ветка Таджика
            int tadjicLevel = cap.getSkillLevel("tadjic");
            if (tadjicLevel > 0) {
                applyTadjicRegen(player, tadjicLevel);
            }

            int carryLevel = cap.getSkillLevel("carry");
            if (carryLevel > 0) {
                applyCarryDamage(player, carryLevel);
            }
        });
    }

    // Путь Берсерка: Увеличение урона и скорости атаки при низком здоровье
    private static void applyBerserkWay(Player player, int level) {
        AttributeModifier attackModifier = new AttributeModifier(
                ATTACK_MODIFIER_UUID,
                "berserk_attack_boost",
                0.1 * level, // +10% урона за уровень
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
        AttributeModifier speedModifier = new AttributeModifier(
                ATTACK_SPEED_MODIFIER_UUID,
                "berserk_speed_boost",
                0.1 * level, // +10% скорости атаки за уровень
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(ATTACK_MODIFIER_UUID);
        player.getAttribute(Attributes.ATTACK_SPEED).removeModifier(ATTACK_SPEED_MODIFIER_UUID);
        player.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(attackModifier);
        player.getAttribute(Attributes.ATTACK_SPEED).addPermanentModifier(speedModifier);
        LOGGER.info("Applied Berserk Way for player: " + player.getName().getString() + ", level: " + level);
    }

    // Кровавая Рана: Шанс нанести кровотечение при атаке
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof Player player && !player.level().isClientSide()) {
            player.getCapability(TestMod.PlayerClassCapability.CAPABILITY).ifPresent(cap -> {
                if (cap.getPlayerClass().equals("war")) {
                    int bloodyWoundLevel = cap.getSkillLevel("bloody_wound");
                    if (bloodyWoundLevel > 0 && new Random().nextFloat() < 0.1 * bloodyWoundLevel) { // 10% шанс за уровень
                        LivingEntity target = event.getEntity();
                        target.addEffect(new MobEffectInstance(
                                MobEffects.POISON, // Эффект яда как аналог кровотечения
                                100, // 5 секунд
                                bloodyWoundLevel - 1,
                                false,
                                true
                        ));
                        LOGGER.info("Applied Bloody Wound to " + target.getName().getString() + " by player: " + player.getName().getString());
                    }
                }
            });
        }
    }

    // Безумный Рывок: Активный навык
    public static void activateMadBoost(Player player, TestMod.PlayerClassCapability cap) {
        if (cap.getSkillLevel("mad_boost") > 0 && cap.getSurgeEnergy() >= 20) {
            AABB area = new AABB(player.blockPosition()).inflate(3.0);
            List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
            for (LivingEntity target : targets) {
                target.hurt(player.level().damageSources().playerAttack(player), 5.0f); // Урон по врагам
            }
            player.setDeltaMovement(player.getLookAngle().scale(2.0)); // Рывок вперед
            player.hurtMarked = true; // Обновляем движение
            cap.useSurgeEnergy(20);
            cap.sync((ServerPlayer) player);
            LOGGER.info("Activated Mad Boost for player: " + player.getName().getString());
        }
    }

    // Жажда Битвы: Восстановление здоровья при убийстве
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            player.getCapability(TestMod.PlayerClassCapability.CAPABILITY).ifPresent(cap -> {
                if (cap.getPlayerClass().equals("war") && cap.getSkillLevel("thirst_battle") > 0) {
                    player.heal(player.getMaxHealth() * 0.1f); // Восстанавливает 10% здоровья
                    cap.sync((ServerPlayer) player);
                    LOGGER.info("Thirst Battle healed player: " + player.getName().getString());
                }
            });
        }
    }

    // Последний Шанс: Неуязвимость при смертельном ударе
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide()) {
            player.getCapability(TestMod.PlayerClassCapability.CAPABILITY).ifPresent(cap -> {
                if (cap.getPlayerClass().equals("war") && cap.getSkillLevel("last_chance") > 0) {
                    if (player.getHealth() - event.getAmount() <= 0) {
                        player.setHealth(1.0f); // Оставить 1 HP
                        player.addEffect(new MobEffectInstance(
                                MobEffects.DAMAGE_RESISTANCE,
                                60, // 3 секунды неуязвимости
                                5, // Высокий уровень сопротивления
                                false,
                                true
                        ));
                        event.setCanceled(true); // Отменяем смертельный урон
                        cap.sync((ServerPlayer) player);
                        LOGGER.info("Last Chance triggered for player: " + player.getName().getString());
                    }
                }
            });
        }
    }

    // Железная Стена: Уменьшение входящего урона
    private static void applyIronWall(Player player, int level) {
        AttributeModifier defenseModifier = new AttributeModifier(
                DEFENSE_MODIFIER_UUID,
                "iron_wall_defense",
                -0.02 * level, // -2% урона за уровень
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
        player.getAttribute(Attributes.ARMOR).removeModifier(DEFENSE_MODIFIER_UUID);
        player.getAttribute(Attributes.ARMOR).addPermanentModifier(defenseModifier);
        LOGGER.info("Applied Iron Wall for player: " + player.getName().getString() + ", level: " + level);
    }

    // Несокрушимость: Сопротивление урону при низком здоровье
    public static void activateIndestructibility(Player player, TestMod.PlayerClassCapability cap) {
        if (cap.getSkillLevel("indestructibility") > 0 && player.getHealth() <= player.getMaxHealth() * 0.3f && cap.getSurgeEnergy() >= 20) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE,
                    100, // 5 секунд
                    cap.getSkillLevel("indestructibility") - 1,
                    false,
                    true
            ));
            cap.useSurgeEnergy(20);
            cap.sync((ServerPlayer) player);
            LOGGER.info("Activated Indestructibility for player: " + player.getName().getString());
        }
    }

    // Крепость: Защита и иммунитет к отбрасыванию при стоянии на месте
    private static void applyFortress(Player player) {
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE,
                20, // 1 секунда (обновляется каждую секунду при стоянии)
                1, // +30% защиты
                false,
                true
        ));
        player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).addPermanentModifier(
                new AttributeModifier(
                        UUID.randomUUID(),
                        "fortress_knockback_resistance",
                        1.0, // Полный иммунитет к отбрасыванию
                        AttributeModifier.Operation.ADDITION
                )
        );
        LOGGER.info("Applied Fortress for player: " + player.getName().getString());
    }

    // Монобровь: Увеличение регенерации здоровья
    private static void applyTadjicRegen(Player player, int level) {
        if (player.tickCount % 20 == 0) { // Каждую секунду
            player.heal(0.02f * level * player.getMaxHealth()); // +2% регенерации за уровень
            LOGGER.info("Applied Tadjic Regen for player: " + player.getName().getString() + ", level: " + level);
        }
    }

    // Армянская сила: Увеличение урона
    private static void applyCarryDamage(Player player, int level) {
        AttributeModifier carryModifier = new AttributeModifier(
                CARRY_MODIFIER_UUID,
                "carry_damage_boost",
                0.02 * level, // +2% урона за уровень
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
        player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(CARRY_MODIFIER_UUID);
        player.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(carryModifier);
        LOGGER.info("Applied Carry Damage for player: " + player.getName().getString() + ", level: " + level);
    }

    // Дагестанская братва: Призыв союзников
    public static void activateDagestan(Player player, TestMod.PlayerClassCapability cap) {
        if (cap.getSkillLevel("dagestan") > 0 && cap.getSurgeEnergy() >= 50) {
            Level level = player.level();
            for (int i = 0; i < 3; i++) { // Призываем 3 союзников
                // Создаем имитацию союзника (например, волка, как заглушку)
                net.minecraft.world.entity.animal.Wolf wolf = new net.minecraft.world.entity.animal.Wolf(
                        net.minecraft.world.entity.EntityType.WOLF, level
                );
                wolf.setPos(player.getX() + new Random().nextInt(3) - 1, player.getY(), player.getZ() + new Random().nextInt(3) - 1);
                wolf.setOwnerUUID(player.getUUID());
                wolf.setTame(true);
                level.addFreshEntity(wolf);
            }
            cap.useSurgeEnergy(50);
            cap.sync((ServerPlayer) player);
            LOGGER.info("Activated Dagestan (summoned allies) for player: " + player.getName().getString());
        }
    }
}