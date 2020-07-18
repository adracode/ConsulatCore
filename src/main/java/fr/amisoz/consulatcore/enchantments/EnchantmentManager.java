package fr.amisoz.consulatcore.enchantments;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import fr.amisoz.consulatcore.ConsulatCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnchantmentManager implements Listener {
    
    private static final EnchantmentManager instance = new EnchantmentManager();
    
    private final Map<UUID, ItemStack> anvilEventCalled = new HashMap<>();
    
    private EnchantmentManager(){
        ConsulatCore.getInstance().getServer().getPluginManager().registerEvents(this, ConsulatCore.getInstance());
    }
    
    @EventHandler
    public void onItemCombineInAnvil(PrepareAnvilEvent event){
        if(anvilEventCalled.containsKey(event.getView().getPlayer().getUniqueId())){
            event.setResult(anvilEventCalled.get(event.getView().getPlayer().getUniqueId()));
            return;
        }
        ItemStack result = event.getResult();
        ItemStack second = event.getInventory().getItem(1);
        if(CEnchantedItem.isEnchanted(second)){
            ItemStack first = event.getInventory().getItem(0);
            if(first != null && first.getType() != Material.AIR){
                int extraCost = 0;
                if(result == null || result.getType() == Material.AIR){
                    result = first.clone();
                }
                CEnchantedItem resultEnchanted = new CEnchantedItem(result);
                CEnchantedItem secondEnchanted = new CEnchantedItem(second);
                for(CEnchantment secondEnchantment : secondEnchanted.getEnchants()){
                    if(!resultEnchanted.addEnchantment(secondEnchantment.getEnchantment(), secondEnchantment.getLevel())){
                        event.setResult(null);
                        Bukkit.getScheduler().runTask(ConsulatCore.getInstance(), () -> {
                            event.getInventory().setRepairCost(0);
                            ((Player)event.getView().getPlayer()).updateInventory();
                        });
                        return;
                    }
                    extraCost += 10;
                }
                event.setResult(result);
                int finalExtraCost = extraCost;
                Bukkit.getScheduler().runTask(ConsulatCore.getInstance(), () -> {
                    event.getInventory().setRepairCost(event.getInventory().getRepairCost() + finalExtraCost);
                });
            }
        }
        if(result != null && result.getType() != Material.AIR && result.getEnchantments().size() > 1 && result.hasItemFlag(ItemFlag.HIDE_ENCHANTS) && result.containsEnchantment(Enchantment.ARROW_INFINITE) && result.getEnchantmentLevel(Enchantment.ARROW_INFINITE) == 0){
            ItemMeta meta = result.getItemMeta();
            meta.removeEnchant(Enchantment.ARROW_INFINITE);
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
            result.setItemMeta(meta);
        }
        anvilEventCalled.put(event.getView().getPlayer().getUniqueId(), event.getResult());
    }
    
    @EventHandler
    public void onTick(ServerTickEndEvent event){
        if(!anvilEventCalled.isEmpty()){
            anvilEventCalled.clear();
        }
    }
    
    public static EnchantmentManager getInstance(){
        return instance;
    }
}
