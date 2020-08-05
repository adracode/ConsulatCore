package fr.amisoz.consulatcore.enchantments;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CEnchantment {
    
    private final int level;
    private final @NotNull Type enchantment;
    
    public CEnchantment(Type enchantment){
        this(enchantment, 1);
    }
    
    public CEnchantment(@NotNull Type enchantment, int level){
        this.enchantment = Objects.requireNonNull(enchantment);
        this.level = level;
    }
    
    public int getLevel(){
        return level;
    }
    
    @NotNull
    public Type getEnchantment(){
        return enchantment;
    }
    
    @Override
    public boolean equals(Object o){
        if(this == o) {
            return true;
        }
        if(!(o instanceof CEnchantment)){
            return false;
        }
        CEnchantment that = (CEnchantment)o;
        return level == that.level &&
                enchantment == that.enchantment;
    }
    
    @Override
    public int hashCode(){
        return Objects.hash(level, enchantment);
    }
    
    public enum Type {
        GLOWING(PotionEffectType.GLOWING, "Surbrillance", false, (byte)1),
        WATER_BREATHING(PotionEffectType.WATER_BREATHING, "Respiration aquatique", false, (byte)1),
        CONDUIT_POWER(PotionEffectType.CONDUIT_POWER, "Force de conduit", false, (byte)1),
        DOLPHIN_GRACE(PotionEffectType.DOLPHINS_GRACE, "Grâce du dophin", false, (byte)1),
        NIGHT_VISION(PotionEffectType.NIGHT_VISION, "Vision nocturne", false, (byte)1),
        FIRE_RESISTANCE(PotionEffectType.FIRE_RESISTANCE, "Résistance au feu", false, (byte)1),
        HEALTH_BOOST(PotionEffectType.HEALTH_BOOST, "Augmentation de vie", false, (byte)1),
        INVISIBILITY(PotionEffectType.INVISIBILITY, "Invisibilité", false, (byte)1),
        HASTE(PotionEffectType.FAST_DIGGING, "Vitesse de minage", false, (byte)2),
        LUCK(PotionEffectType.LUCK, "Chance", false, (byte)1),
        SLOW_FALLING(PotionEffectType.SLOW_FALLING, "Chute lente", false, (byte)1),
        SPEED(PotionEffectType.SPEED, "Vitesse", false, (byte)2),
        JUMP_BOOST(PotionEffectType.JUMP, "Sauts augmentés", false, (byte)1);
    
        private final PotionEffectType effect;
        private final String display;
        private final boolean canCombine;
        private final byte maxLevel;
    
        Type(PotionEffectType effect, String display, boolean canCombine, byte maxLevel){
            this.effect = effect;
            this.display = display;
            this.canCombine = canCombine;
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
    
        public boolean canCombine(){
            return canCombine;
        }
    }
    
    

}
