package net.xach.testmod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillTreeHandler {

    public static final Map<String, SkillTree> CLASS_SKILL_TREES = new HashMap<>();

    static {
        initializeSkillTrees();
    }

    public static void initializeSkillTrees() {
        // Класс Воин
        SkillTree warTree = new SkillTree();

        // Ветка "Берсерк"
        List<Skill> berserkBranch = Arrays.asList(
                new Skill("berserk_way", "Путь Берсерка", "Увеличивает урон при низком здоровье", SkillType.PASSIVE, 1, 3, null),
                new Skill("bloody_wound", "Кровавая Рана", "Шанс нанести кровотечение", SkillType.PASSIVE, 5, 3, "berserk_way"),
                new Skill("mad_boost", "Безумный Рывок", "Рывок с уроном по области", SkillType.ACTIVE, 10, 1, "bloody_wound"),
                new Skill("thirst_battle", "Жажда Битвы", "Восстановление здоровья при убийстве", SkillType.PASSIVE, 15, 1, "mad_boost"),
                new Skill("last_chance", "Последний Шанс", "Неуязвимость при смертельном ударе", SkillType.PASSIVE, 20, 1, "thirst_battle")
        );

        // Ветка "Защитник"
        List<Skill> defenderBranch = Arrays.asList(
                new Skill("iron", "Железная Стена", "Уменьшает входящий урон", SkillType.PASSIVE, 1, 3, null),
                new Skill("indestructibility", "Несокрушимость", "Активное сопротивление урону", SkillType.ACTIVE, 5, 1, "iron"),
                new Skill("fortress", "Крепость", "Защита при стоянии на месте", SkillType.PASSIVE, 10, 1, "indestructibility")
        );

        // Ветка "Таджик"
        List<Skill> tadjicBranch = Arrays.asList(
                new Skill("tadjic", "Монобровь", "Увеличивает регенерацию здоровья", SkillType.PASSIVE, 1, 5, null),
                new Skill("carry", "Армянская сила", "Увеличивает урон", SkillType.PASSIVE, 5, 5, "tadjic"),
                new Skill("dagestan", "Дагестанская братва", "Призывает союзников", SkillType.ACTIVE, 15, 1, "carry")
        );

        warTree.addBranch(berserkBranch);
        warTree.addBranch(defenderBranch);
        warTree.addBranch(tadjicBranch);
        CLASS_SKILL_TREES.put("war", warTree);

        // Класс Курьер Yandex.Go
        SkillTree yandexTree = new SkillTree();

        // Ветка "Скорость"
        List<Skill> speedBranch = Arrays.asList(
                new Skill("sprint_boost", "Рывок", "Временное увеличение скорости", SkillType.ACTIVE, 1, 3, null),
                new Skill("speed_surge", "Всплеск скорости", "Мощное ускорение", SkillType.ACTIVE, 10, 1, "sprint_boost")
        );

        // Ветка "Инвентарь"
        List<Skill> inventoryBranch = Arrays.asList(
                new Skill("inventory_surge", "Улыбка курьера", "Увеличивает вместимость", SkillType.ACTIVE, 5, 1, null)
        );

        // Ветка "Грузоподъёмность"
        List<Skill> carryBranch = Arrays.asList(
                new Skill("carry_surge", "Перцовая граната", "Оглушает врагов", SkillType.ACTIVE, 8, 1, null)
        );

        yandexTree.addBranch(speedBranch);
        yandexTree.addBranch(inventoryBranch);
        yandexTree.addBranch(carryBranch);
        CLASS_SKILL_TREES.put("yandex.go", yandexTree);

        // Класс Шахтёр
        SkillTree minerTree = new SkillTree();

        // Ветка "Добыча"
        List<Skill> miningBranch = Arrays.asList(
                new Skill("dig_area", "Широкое копание", "Ломает блоки в области", SkillType.PASSIVE, 1, 3, null),
                new Skill("ore_double", "Удвоение руды", "Шанс получить двойную руду", SkillType.PASSIVE, 5, 5, "dig_area"),
                new Skill("ore_highlight", "Подсветка руды", "Подсвечивает руду через стены", SkillType.ACTIVE, 10, 1, "ore_double"),
                new Skill("vein_miner", "Жилокопатель", "Добывает всю рудную жилу", SkillType.ACTIVE, 15, 1, "ore_highlight")
        );

        // Ветка "Исследование"
        List<Skill> explorationBranch = Arrays.asList(
                new Skill("night_vision", "Ночное зрение", "Активирует ночное зрение", SkillType.ACTIVE, 1, 1, null),
                new Skill("ore_drop_chance", "Случайная находка", "Шанс найти редкие предметы", SkillType.PASSIVE, 5, 3, "night_vision"),
                new Skill("deep_miner", "Глубинный шахтёр", "Бонус к добыче на глубине", SkillType.PASSIVE, 10, 3, "ore_drop_chance")
        );

        // Ветка "Выживание"
        List<Skill> survivalBranch = Arrays.asList(
                new Skill("blast_resistance", "Устойчивость к взрывам", "Уменьшает урон от взрывов на 15% за уровень", SkillType.PASSIVE, 3, 1, null),
                new Skill("torch_range", "Свет факела", "Увеличивает радиус освещения факелов на 2 блока за уровень", SkillType.PASSIVE, 3, 2, "blast_resistance"),
                new Skill("deep_miner", "Глубинный шахтёр", "Увеличивает добычу ресурсов в глубоких шахтах на 25%", SkillType.GLOBAL, 1, 5, "torch_range")
        );

        minerTree.addBranch(miningBranch);
        minerTree.addBranch(explorationBranch);
        minerTree.addBranch(survivalBranch);
        CLASS_SKILL_TREES.put("miner", minerTree);

        // Класс Повар
        SkillTree cookTree = new SkillTree();

        // Ветка "Гурман"
        List<Skill> gourmetBranch = Arrays.asList(
                new Skill("food_efficiency", "Эффективность еды", "Еда даёт больше сытости", SkillType.PASSIVE, 1, 5, null),
                new Skill("cooking_speed", "Скорость готовки", "Быстрее готовит еду", SkillType.PASSIVE, 5, 3, "food_efficiency")
        );

        cookTree.addBranch(gourmetBranch);
        CLASS_SKILL_TREES.put("cook", cookTree);

        // Класс Пивовар
        SkillTree pivoTree = new SkillTree();

        // Ветка "Мастер ферментации"
        List<Skill> fermentationBranch = Arrays.asList(
                new Skill("brewing_efficiency", "Эффективность варки", "Быстрее варит зелья", SkillType.PASSIVE, 1, 5, null),
                new Skill("potion_power", "Сила зелий", "Зелья действуют дольше", SkillType.PASSIVE, 5, 3, "brewing_efficiency")
        );

        pivoTree.addBranch(fermentationBranch);
        CLASS_SKILL_TREES.put("pivo", pivoTree);
    }

    public static SkillTree getSkillTree(String className) {
        return CLASS_SKILL_TREES.get(className);
    }

    public enum SkillType {
        ACTIVE,
        PASSIVE,
        GLOBAL
    }

    public static class Skill {
        private final String id;
        private final String name;
        private final String description;
        private final SkillType type;
        private final int requiredLevel;
        private final int maxLevel;
        private final String parentId;

        public Skill(String id, String name, String description, SkillType type, int requiredLevel, int maxLevel, String parentId) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.type = type;
            this.requiredLevel = requiredLevel;
            this.maxLevel = maxLevel;
            this.parentId = parentId;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public SkillType getType() { return type; }
        public int getRequiredLevel() { return requiredLevel; }
        public int getMaxLevel() { return maxLevel; }
        public String getParentId() { return parentId; }
    }

    public static class SkillTree {
        private final List<List<Skill>> branches = new ArrayList<>();

        public void addBranch(List<Skill> branch) {
            this.branches.add(branch);
        }

        public List<List<Skill>> getBranches() {
            return branches;
        }

        public List<Skill> getAllSkills() {
            List<Skill> allSkills = new ArrayList<>();
            for (List<Skill> branch : branches) {
                allSkills.addAll(branch);
            }
            return allSkills;
        }
    }
}
