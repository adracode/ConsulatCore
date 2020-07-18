package fr.amisoz.consulatcore.enchantments;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffectType;

public class CEnchantment {
    
    private final int level;
    private final Type enchantment;
    
    CEnchantment(Type enchantment){
        this(enchantment, 1);
    }
    
    public CEnchantment(Type enchantment, int level){
        this.level = level;
        this.enchantment = enchantment;
    }
    
    public int getLevel(){
        return level;
    }
    
    public Type getEnchantment(){
        return enchantment;
    }
    
    public enum Type {
        GLOWING(PotionEffectType.GLOWING, "Surbrillance", (byte)1),
        WATER_BREATHING(PotionEffectType.WATER_BREATHING, "Respiration aquatique", (byte)1),
        CONDUIT_POWER(PotionEffectType.CONDUIT_POWER, "Force de conduit", (byte)1),
        DOLPHIN_GRACE(PotionEffectType.DOLPHINS_GRACE, "Grâce du dophin", (byte)1),
        NIGHT_VISION(PotionEffectType.NIGHT_VISION, "Vision nocturne", (byte)1),
        FIRE_RESISTANCE(PotionEffectType.FIRE_RESISTANCE, "Résistance au feu", (byte)1),
        HEALTH_BOOST(PotionEffectType.HEALTH_BOOST, "Augmentation de vie", (byte)1),
        INVISIBILITY(PotionEffectType.INVISIBILITY, "Invisibilité", (byte)1),
        HASTE(PotionEffectType.FAST_DIGGING, "Vitesse de minage", (byte)2),
        LUCK(PotionEffectType.LUCK, "Chance", (byte)1),
        SLOW_FALLING(PotionEffectType.SLOW_FALLING, "Chute lente", (byte)1),
        SPEED(PotionEffectType.SPEED, "Vitesse", (byte)2),
        JUMP_BOOST(PotionEffectType.JUMP, "Sauts augmentés", (byte)1);
    
        private final PotionEffectType effect;
        private final String display;
        private final byte maxLevel;
    
        Type(PotionEffectType effect, String display, byte maxLevel){
            this.effect = effect;
            this.display = display;
            this.maxLevel = maxLevel;
        }
    
        @SuppressWarnings("DuplicatedCode")
        public boolean canApply(EquipmentSlot armor){
            switch(armor){
                case HEAD:
                    switch(this){
                        case GLOWING:
                        case WATER_BREATHING:
                        case CONDUIT_POWER:
                        case DOLPHIN_GRACE:
                        case NIGHT_VISION:
                        case FIRE_RESISTANCE:
                            return true;
                    }
                    break;
                case CHEST:
                    switch(this){
                        case GLOWING:
                        case HEALTH_BOOST:
                        case INVISIBILITY:
                        case HASTE:
                        case LUCK:
                        case FIRE_RESISTANCE:
                            return true;
                    }
                    break;
                case LEGS:
                    switch(this){
                        case GLOWING:
                        case HEALTH_BOOST:
                        case FIRE_RESISTANCE:
                        case LUCK:
                        case INVISIBILITY:
                        case HASTE:
                            return true;
                    }
                    break;
                case FEET:
                    switch(this){
                        case GLOWING:
                        case FIRE_RESISTANCE:
                        case SLOW_FALLING:
                        case SPEED:
                        case JUMP_BOOST:
                            return true;
                    }
                    break;
                case HAND:
                    return true;
            }
            return false;
        }
    
        public String getDisplay(){
            return display;
        }
    
        public PotionEffectType getEffect(){
            return effect;
        }
    
        public byte getMaxLevel(){
            return maxLevel;
        }
    }

}
