package fr.amisoz.consulatcore.shop.player;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

public class ShopItemType {
    
    public static final ShopItemType ALL = new ShopItemType();
    
    private ShopItemType(){
    }
    
    @Override
    public boolean equals(Object o){
        return o.getClass() == ShopItemType.class;
    }
    
    @Override
    public int hashCode(){
        return 1;
    }
    
    public static class MaterialItem extends ShopItemType {
        private Material material;
    
        public MaterialItem(Material material){
            this.material = material;
        }
    
        @Override
        public boolean equals(Object o){
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            MaterialItem that = (MaterialItem)o;
            return material == that.material;
        }
    
        @Override
        public int hashCode(){
            return material.hashCode();
        }
    
        @Override
        public String toString(){
            return material.toString().toLowerCase();
        }
    }
    
    public static class EnchantmentItem extends ShopItemType {
        private Enchantment enchantment;
    
        public EnchantmentItem(Enchantment enchantment){
            this.enchantment = enchantment;
        }
    
        @Override
        public boolean equals(Object o){
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            EnchantmentItem that = (EnchantmentItem)o;
            return enchantment.equals(that.enchantment);
        }
    
        @Override
        public int hashCode(){
            return enchantment.hashCode();
        }
    
        @Override
        public String toString(){
            return enchantment.getKey().getKey().toLowerCase();
        }
    }
    
    public static class PotionItem extends ShopItemType {
        private PotionEffectType effect;
    
        public PotionItem(PotionEffectType effect){
            this.effect = effect;
        }
    
        @Override
        public boolean equals(Object o){
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            PotionItem that = (PotionItem)o;
            return effect.equals(that.effect);
        }
    
        @Override
        public int hashCode(){
            return effect.hashCode();
        }
        
        @Override
        public String toString(){
            return effect.getName().toLowerCase();
        }
    }
    
}
