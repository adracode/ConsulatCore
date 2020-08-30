package fr.leconsulat.core.enchantments;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class EnchantmentDataType implements PersistentDataType<String, CEnchantment> {
    
    @Override
    public @NotNull Class<String> getPrimitiveType(){
        return String.class;
    }
    
    @Override
    public @NotNull Class<CEnchantment> getComplexType(){
        return CEnchantment.class;
    }
    
    @NotNull
    @Override
    public String toPrimitive(@NotNull CEnchantment enchantment, @NotNull PersistentDataAdapterContext persistentDataAdapterContext){
        return enchantment.getEnchantment().name() + ":" + enchantment.getLevel();
    }
    
    @NotNull
    @Override
    public CEnchantment fromPrimitive(@NotNull String s, @NotNull PersistentDataAdapterContext persistentDataAdapterContext){
        int separator = s.indexOf(':');
        if(separator == -1){
            throw new IllegalArgumentException("Enchantment doesn't has ':'");
        }
        return new CEnchantment(CEnchantment.Type.valueOf(s.substring(0, separator)), Integer.parseInt(s.substring(separator + 1)));
    }
}
