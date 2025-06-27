package net.xach.testmod;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkHooks;

import java.util.*;

public class SkillTreeHandler {
    public static final Map<String, SkillTree> CLASS_SKILL_TREES = new HashMap<>();

    static {
        // Ветка для класса "yandex.go"
        List<Skill> speedBranch = Arrays.asList(
                new Skill("speed_basic", "Базовая скорость", SkillType.PASSIVE, 3, 1, null, 0, 0, "Увеличивает скорость передвижения на 5% за уровень"),
                new Skill("hunger_reduction", "Снижение голода", SkillType.PASSIVE, 3, 2, "speed_basic", 0, 1, "Снижает расход голода на 10% за уровень"),
                new Skill("sprint_boost", "Рывок", SkillType.ACTIVE, 1, 3, "hunger_reduction", 0, 2, "Активирует временное ускорение на 10 секунд"),
                new Skill("speed_surge", "Всплеск скорости", SkillType.GLOBAL, 1, 5, "sprint_boost", 0, 3, "Мощное ускорение на 30 секунд с восстановлением голода")
        );

        List<Skill> inventoryBranch = Arrays.asList(
                new Skill("inventory_slots_basic", "Базовые слоты", SkillType.PASSIVE, 3, 1, null, 1, 0, "Добавляет 1 слот инвентаря за уровень"),
                new Skill("crafting_efficiency", "Стаки", SkillType.PASSIVE, 4, 10, "inventory_slots_basic", 1, 1, "увеличивает кол-во стаков в инвенатре (на 16 за уровень)"),
                new Skill("inventory_surge", "Улыбка Курьера", SkillType.GLOBAL, 1, 4, "crafting_efficiency", 1, 2, "Скидка в 15% на все товары")
        );

        List<Skill> carryBranch = Arrays.asList(
                new Skill("carry_capacity_basic", "Ловушка", SkillType.PASSIVE, 3, 1, null, 2, 0, "На земле можно установить невидимый «триггер», который оглушает врагов"),
                new Skill("shulker_carry", "Карта в голове", SkillType.PASSIVE, 1, 2, "carry_capacity_basic", 2, 1, "Видит расположение всех деревень и торговцев"),
                new Skill("carry_surge", "Граната с перцем", SkillType.GLOBAL, 1, 4, "shulker_carry", 2, 2, "Открывает крафт перцового баллона")
        );

        SkillTree yandexTree = new SkillTree(Arrays.asList(speedBranch, inventoryBranch, carryBranch));
        CLASS_SKILL_TREES.put("yandex.go", yandexTree);

        // Ветка для класса "war"
        List<Skill> berserkBranch = Arrays.asList(
                new Skill("berserk_way", "Путь берсерка", SkillType.PASSIVE, 10, 1, null, 0, 0, "При низком уровне здоровья увеличивается урон и скорость атаки"),
                new Skill("bloody_wound", "Кровавая Рана", SkillType.PASSIVE, 5, 2, "berserk_way", 0, 1, "Каждая атака имеет шанс вызвать кровотечение у врага, нанося урон со временем"),
                new Skill("mad_boost", "Безумный Рывок", SkillType.ACTIVE, 1, 3, "bloody_wound", 0, 2, "Рывок вперёд с уроном по всем врагам на пути (перезарядка 10 сек)"),
                new Skill("thirst_battle", "Жажда Битвы", SkillType.GLOBAL, 1, 5, "mad_boost", 0, 3, "Убийства восстанавливают часть здоровья"),
                new Skill("last_chance", "Последний шанс", SkillType.PASSIVE, 1, 5, "thirst_battle", 0, 4, "При получении смертельного удара остаёшься с 1 HP и получаешь неуязвимость на 3 сек (перезарядка 2 мин)")
        );

        List<Skill> defenderBranch = Arrays.asList(
                new Skill("iron", "Железная Стена", SkillType.PASSIVE, 10, 1, null, 1, 0, "Уменьшает входящий урон на 2% за уровень"),
                new Skill("indestructibility", "Несокрушимость", SkillType.ACTIVE, 2, 2, "iron", 1, 1, "При падении ниже 30% HP получаешь сопротивление к урону на 5 сек (перезарядка 45 сек)"),
                new Skill("fortress", "Крепость", SkillType.PASSIVE, 1, 4, "indestructibility", 1, 2, "При стоянии на месте более 3 сек получаешь +30% защиты и иммунитет к отбрасыванию")
        );

        List<Skill> tadjicBranch = Arrays.asList(
                new Skill("tadjic", "Монобровь", SkillType.PASSIVE, 25, 1, null, 2, 0, "Увеличивает регенерацию здоровья на 2% за уровень"),
                new Skill("carry", "Армянская сила", SkillType.PASSIVE, 20, 2, "tadjic", 2, 1, "Увеличивает урон на 2% за уровень"),
                new Skill("dagestan", "Дагестанская братва", SkillType.GLOBAL, 1, 4, "carry", 2, 2, "Призывает братву на кипиш (45 секунд)")
        );

        SkillTree warTree = new SkillTree(Arrays.asList(berserkBranch, defenderBranch, tadjicBranch));
        CLASS_SKILL_TREES.put("war", warTree);

        // Ветка для класса "pivo" (Пивовар)
        List<Skill> fermentationBranch = Arrays.asList(
                new Skill("strong_brew", "Крепкий настой", SkillType.PASSIVE, 5, 1, null, 0, 0, "Зелья дают +30% к длительности и силе эффектов"),
                new Skill("double_distillation", "Двойная перегонка", SkillType.PASSIVE, 3, 2, "strong_brew", 0, 1, "Шанс 25% создать 2 зелья вместо 1 при варке"),
                new Skill("reactive_cocktail", "Реактивный коктейль", SkillType.ACTIVE, 1, 3, "double_distillation", 0, 2, "При распитии зелья есть шанс получить взрывной эффект (оглушает врагов вокруг)"),
                new Skill("bacchus_madness", "Безумие Бахуса", SkillType.GLOBAL, 1, 5, "reactive_cocktail", 0, 3, "Все враги в радиусе 8 блоков получают тошноту и слабость")
        );

        List<Skill> drunkenMasterBranch = Arrays.asList(
                new Skill("dizzy_strike", "Головокружение", SkillType.PASSIVE, 5, 1, null, 1, 0, "При ударе по врагу добавляет дебафф 'Деориентация' (меняет управление на обратное, 15% шанс)"),
                new Skill("sweeping_strike", "Размашистый удар", SkillType.PASSIVE, 3, 2, "dizzy_strike", 1, 1, "Атаки по врагам имеют шанс 15% ударить ещё одного случайного противника рядом"),
                new Skill("last_sip", "Последний глоток", SkillType.ACTIVE, 1, 4, "sweeping_strike", 1, 2, "При падении ниже 10% HP автоматически выпивается случайное зелье из инвентаря")
        );

        List<Skill> tavernPartyBranch = Arrays.asList(
                new Skill("toast_health", "Тост за здоровье", SkillType.PASSIVE, 3, 1, null, 2, 0, "При распитии зелья исцеляются все союзники в радиусе 5 блоков"),
                new Skill("fun_aura", "Аура веселья", SkillType.PASSIVE, 3, 2, "toast_health", 2, 1, "Все игроки рядом получают +15% к скорости и урону, если на владельце есть эффект зелья"),
                new Skill("spirit_invulnerability", "Неуязвимость духа", SkillType.ACTIVE, 1, 3, "fun_aura", 2, 2, "Союзники в радиусе 8 блоков получают регенерацию на 5 сек (раз в 30 сек)"),
                new Skill("dance_till_drop", "Танец до упаду", SkillType.GLOBAL, 1, 5, "spirit_invulnerability", 2, 3, "Активирует массовый эффект невидимости для всех рядом на 10 сек (перезарядка 2 мин)")
        );

        SkillTree pivoTree = new SkillTree(Arrays.asList(fermentationBranch, drunkenMasterBranch, tavernPartyBranch));
        CLASS_SKILL_TREES.put("pivo", pivoTree);

        // Ветка для класса "cook" (Повар)
        List<Skill> gourmetBranch = Arrays.asList(
                new Skill("fresh_product", "Свежий продукт", SkillType.PASSIVE, 5, 1, null, 0, 0, "Еда восстанавливает на 2% сытости больше за уровень"),
                new Skill("fast_cooking", "Быстрое приготовление", SkillType.PASSIVE, 5, 2, "fresh_product", 0, 1, "Время готовки в печи уменьшено на 5% за уровень"),
                new Skill("hearty_meal", "Сытный обед", SkillType.PASSIVE, 3, 3, "fast_cooking", 0, 2, "Эффект сытости длится в 2 раза дольше"),
                new Skill("chef_master", "Шеф-повар", SkillType.PASSIVE, 1, 4, "hearty_meal", 0, 3, "Приготовленная еда иногда даёт случайный позитивный эффект (например, регенерацию)")
        );

        List<Skill> fieryKitchenBranch = Arrays.asList(
                new Skill("smoke_screen", "Дымовая завеса", SkillType.ACTIVE, 1, 1, null, 1, 0, "При ударе ниже 30% HP даёт невидимость на 3 сек"),
                new Skill("flambe", "Фламбе", SkillType.PASSIVE, 1, 3, "smoke_screen", 1, 1, "При убийстве подожжённого врага происходит взрыв (урон в радиусе 3 блоков)")
        );

        List<Skill> criticBranch = Arrays.asList(
                new Skill("ready", "Готово!", SkillType.PASSIVE, 3, 1, null, 2, 0, "Приготовление еды даёт временный бафф 'Вдохновление' (+10% к урону)"),
                new Skill("quick_snack", "Быстрое перекус", SkillType.PASSIVE, 1, 2, "ready", 2, 1, "Можно есть на бегу без замедления"),
                new Skill("feast_world", "Пир на весь мир", SkillType.PASSIVE, 1, 3, "quick_snack", 2, 2, "Разделение еды с союзниками восстанавливает им +2 сердца"),
                new Skill("banquet", "Банкет", SkillType.GLOBAL, 1, 5, "feast_world", 2, 3, "При активации все союзники в радиусе 10 блоков получают регенерацию и сопротивление на 15 сек")
        );

        SkillTree cookTree = new SkillTree(Arrays.asList(gourmetBranch, fieryKitchenBranch, criticBranch));
        CLASS_SKILL_TREES.put("cook", cookTree);
    }

    public static void openSkillTree(ServerPlayer player) {
        NetworkHooks.openScreen(player, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("Дерево навыков");
            }

            @Override
            public SkillTreeMenu createMenu(int id, Inventory inv, Player player) {
                return new SkillTreeMenu(id, inv);
            }
        });
    }

    public static class SkillTree {
        private final List<List<Skill>> branches;

        public SkillTree(List<List<Skill>> branches) {
            this.branches = branches;
        }

        public List<List<Skill>> getBranches() {
            return branches;
        }

        public List<Skill> getAllSkills() {
            List<Skill> allSkills = new ArrayList<>();
            branches.forEach(allSkills::addAll);
            return allSkills;
        }
    }

    public static class Skill {
        private final String id;
        private final String name;
        private final SkillType type;
        private final int maxLevel;
        private final int requiredLevel;
        private final String parentId;
        private final int branch;
        private final int position;
        private final String description;

        public Skill(String id, String name, SkillType type, int maxLevel, int requiredLevel, String parentId, int branch, int position, String description) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.maxLevel = maxLevel;
            this.requiredLevel = requiredLevel;
            this.parentId = parentId;
            this.branch = branch;
            this.position = position;
            this.description = description;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public SkillType getType() { return type; }
        public int getMaxLevel() { return maxLevel; }
        public int getRequiredLevel() { return requiredLevel; }
        public String getParentId() { return parentId; }
        public int getBranch() { return branch; }
        public int getPosition() { return position; }
        public String getDescription() { return description; }
    }

    public enum SkillType {
        PASSIVE, ACTIVE, GLOBAL
    }
}