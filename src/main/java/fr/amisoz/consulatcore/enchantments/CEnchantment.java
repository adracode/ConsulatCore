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
    public int hashCode(){
        return Objects.hash(level, enchantment);
    }
    
    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }
        if(!(o instanceof CEnchantment)){
            return false;
        }
        CEnchantment that = (CEnchantment)o;
        return level == that.level &&
                enchantment == that.enchantment;
    }
    
    public enum Type {
        RADIOACTIVE(PotionEffectType.GLOWING, "Radioactif", false, (byte)1),
        DIVING(PotionEffectType.WATER_BREATHING, "Plongée", false, (byte)1),
        AQUA_FORCE(PotionEffectType.CONDUIT_POWER, "Force aquatique", false, (byte)1),
        DOLPHIN_FIN(PotionEffectType.DOLPHINS_GRACE, "Nageoire du dauphin", false, (byte)1),
        INFRARED_VISION(PotionEffectType.NIGHT_VISION, "Vision infrarouge", false, (byte)1),
        FIRE_ENDURANCE(PotionEffectType.FIRE_RESISTANCE, "Endurance au feu", false, (byte)1),
        INCREASED_HEALTH(PotionEffectType.HEALTH_BOOST, "Augmentation de vie", false, (byte)1),
        STEALTH(PotionEffectType.INVISIBILITY, "Furtivité", false, (byte)1),
        MINER_HAND(PotionEffectType.FAST_DIGGING, "Main du mineur", false, (byte)2),
        FISHER_LUCK(PotionEffectType.LUCK, "Chance du pêcheur", false, (byte)1),
        MOON_FALL(PotionEffectType.SLOW_FALLING, "Chute lunaire", false, (byte)1),
        CHEETAH_STRENGTH(PotionEffectType.SPEED, "Force du guépard", false, (byte)2),
        MOON_JUMP(PotionEffectType.JUMP, "Sauts lunaire", false, (byte)1);
        
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
                        case RADIOACTIVE:
                        case DIVING:
                        case AQUA_FORCE:
                        case DOLPHIN_FIN:
                        case INFRARED_VISION:
                        case FIRE_ENDURANCE:
                            return true;
                    }
                    break;
                case CHEST:
                    switch(this){
                        case RADIOACTIVE:
                        case INCREASED_HEALTH:
                        case STEALTH:
                        case MINER_HAND:
                        case FISHER_LUCK:
                        case FIRE_ENDURANCE:
                            return true;
                    }
                    break;
                case LEGS:
                    switch(this){
                        case RADIOACTIVE:
                        case INCREASED_HEALTH:
                        case FIRE_ENDURANCE:
                        case FISHER_LUCK:
                        case STEALTH:
                        case MINER_HAND:
                            return true;
                    }
                    break;
                case FEET:
                    switch(this){
                        case RADIOACTIVE:
                        case FIRE_ENDURANCE:
                        case MOON_FALL:
                        case CHEETAH_STRENGTH:
                        case MOON_JUMP:
                            return true;
                    }
                    break;
                case HAND:
                    return true;
            }
            return false;
        }
        
        public boolean canCombine(){
            return canCombine;
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
