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
        GLOWING(PotionEffectType.GLOWING, "Surbrillance"),
        WATER_BREATHING(PotionEffectType.WATER_BREATHING, "Respiration aquatique"),
        CONDUIT_POWER(PotionEffectType.CONDUIT_POWER, "Force de conduit"),
        DOLPHIN_GRACE(PotionEffectType.DOLPHINS_GRACE, "Grâce du dophin"),
        NIGHT_VISION(PotionEffectType.NIGHT_VISION, "Vision nocturne"),
        FIRE_RESISTANCE(PotionEffectType.FIRE_RESISTANCE, "Résistance au feu"),
        HEALTH_BOOST(PotionEffectType.HEALTH_BOOST, "Augmentation de vie"),
        INVISIBILITY(PotionEffectType.INVISIBILITY, "Invisibilité"),
        HASTE(PotionEffectType.FAST_DIGGING, "Vitesse de minage"),
        LUCK(PotionEffectType.LUCK, "Chance"),
        SLOW_FALLING(PotionEffectType.SLOW_FALLING, "Chute lente"),
        SPEED(PotionEffectType.SPEED, "Vitesse"),
        JUMP_BOOST(PotionEffectType.JUMP, "Sauts augmentés");
    
        private final PotionEffectType effect;
        private final String display;
    
        Type(PotionEffectType effect, String display){
            this.effect = effect;
            this.display = display;
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
    }

}
