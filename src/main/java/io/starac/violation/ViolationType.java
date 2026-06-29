package io.starac.violation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ViolationType {

    KILLAURA("KillAura", "A", Category.COMBAT, 1.5, 20, "Атака нескольких целей / неестественные углы"),
    REACH("Reach", "B", Category.COMBAT, 2.0, 15, "Превышение дистанции атаки"),
    AUTOCLICKER("AutoClicker", "C", Category.COMBAT, 1.0, 15, "Аномальная стабильность CPS"),
    AIMBOT("Aimbot", "D", Category.COMBAT, 1.8, 20, "Подозрительная точность наведения (GCD/Sine)"),
    VELOCITY("Velocity", "E", Category.COMBAT, 1.5, 15, "Игнорирование отдачи"),
    HITBOX("Hitbox", "F", Category.COMBAT, 2.0, 15, "Попадание вне ванильного хитбокса"),
    CRITICALS("Criticals", "G", Category.COMBAT, 1.0, 12, "Принудительные крит-удары"),

    FLY("Fly", "A", Category.MOVEMENT, 2.5, 15, "Полёт / отсутствие гравитации"),
    SPEED("Speed", "B", Category.MOVEMENT, 1.8, 15, "Превышение ванильной скорости"),
    JESUS("Jesus", "C", Category.MOVEMENT, 2.0, 12, "Хождение по жидкостям"),
    TIMER("Timer", "D", Category.MOVEMENT, 2.5, 15, "Ускорение игрового тика (Blink/Timer)"),
    NOFALL("NoFall", "E", Category.MOVEMENT, 1.2, 12, "Игнорирование урона от падения"),
    STEP("Step", "F", Category.MOVEMENT, 1.0, 10, "Подъём на 2+ блока"),
    PHASE("Phase", "G", Category.MOVEMENT, 3.0, 12, "Проход сквозь блоки (NoClip)"),

    SCAFFOLD("Scaffold", "A", Category.WORLD, 1.8, 18, "Автоматическая постройка под собой"),
    FASTPLACE("FastPlace", "B", Category.WORLD, 1.0, 12, "Слишком быстрая установка блоков"),
    NUKER("Nuker", "C", Category.WORLD, 2.0, 15, "Массовое разрушение блоков"),
    TOWER("Tower", "D", Category.WORLD, 1.2, 12, "Автоматический подъём на столбе"),

    CHEST_STEALER("ChestStealer", "A", Category.INVENTORY, 1.5, 12, "Мгновенное разграбление сундуков"),
    INVENTORY_MOVE("InvMove", "B", Category.INVENTORY, 1.0, 12, "Движение с открытым инвентарём"),

    BAD_PACKETS("BadPackets", "A", Category.PACKET, 2.0, 20, "Невозможные / битые пакеты"),
    PING_SPOOF("PingSpoof", "B", Category.PACKET, 1.5, 15, "Подделка задержки"),

    AI_DETECTED("AI-Detected", "A", Category.AI, 3.0, 10, "Нейросеть подтвердила аномалию");

    public enum Category {
        COMBAT("Combat", "§c"),
        MOVEMENT("Movement", "§b"),
        WORLD("World", "§a"),
        INVENTORY("Inventory", "§e"),
        PACKET("Packet", "§d"),
        AI("AI", "§5");

        private final String displayName;
        private final String color;

        Category(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }

        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
    }

    private final String name;
    private final String code;
    private final Category category;
    private final double baseWeight;
    private final int maxVL;
    private final String description;

    ViolationType(String name, String code, Category category, double baseWeight, int maxVL, String description) {
        this.name = name;
        this.code = code;
        this.category = category;
        this.baseWeight = baseWeight;
        this.maxVL = maxVL;
        this.description = description;
    }

    public String getName() { return name; }
    public String getCode() { return code; }
    public Category getCategory() { return category; }
    public double getBaseWeight() { return baseWeight; }
    public int getMaxVL() { return maxVL; }
    public String getDescription() { return description; }
    public String getFormattedName() {
        return category.getColor() + name + " §7(" + code + ")";
    }

    public String getId() {
        return category.name() + "." + code;
    }

    public static ViolationType fromString(String name) {
        for (ViolationType t : values()) {
            if (t.name.equalsIgnoreCase(name) || t.getId().equalsIgnoreCase(name)) {
                return t;
            }
        }
        return null;
    }

    public static List<ViolationType> byCategory(Category category) {
        return Arrays.stream(values())
                .filter(t -> t.category == category)
                .collect(Collectors.toList());
    }
}