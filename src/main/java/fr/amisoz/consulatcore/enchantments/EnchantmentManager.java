package fr.amisoz.consulatcore.enchantments;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.player.CPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class EnchantmentManager implements Listener {
    
    private static final EnchantmentManager instance = new EnchantmentManager();
    
    private final Map<UUID, ItemStack> anvilEventCalled = new HashMap<>();
    
    private EnchantmentManager(){
        ConsulatCore.getInstance().getServer().getPluginManager().registerEvents(this, ConsulatCore.getInstance());
    }
    
    public void applyCEnchantment(SurvivalPlayer player, CEnchantment... armorEnchants){
        Player bukkitPlayer = player.getPlayer();
        for(CEnchantment enchant : armorEnchants){
            PotionEffectType effect = enchant.getEnchantment().getEffect();
            PotionEffect currentEffect = bukkitPlayer.getPotionEffect(effect);
            if(currentEffect != null){
                if(currentEffect.getAmplifier() > enchant.getLevel()){
                    continue;
                }
                if(currentEffect.getAmplifier() < enchant.getLevel()){
                    bukkitPlayer.removePotionEffect(currentEffect.getType());
                }
            }
            bukkitPlayer.addPotionEffect(new PotionEffect(enchant.getEnchantment().getEffect(), Integer.MAX_VALUE, enchant.getLevel() - 1, false, false));
        }
    }
    
    @EventHandler
    public void onArmorChange(PlayerArmorChangeEvent event){
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        PlayerArmorChangeEvent.SlotType slot = event.getSlotType();
        CEnchantedItem oldArmor = player.getArmor(slot);
        ItemStack old = CEnchantedItem.isEnchanted(event.getOldItem()) ? event.getOldItem() : null;
        if(old != null && old.getType() == Material.AIR){
            old = null;
        }
        if(oldArmor != null && old == null || oldArmor == null && old != null || oldArmor != null && !oldArmor.getHandle().equals(old)){
            ConsulatAPI.getConsulatAPI().log(Level.WARNING, "Armors doesn't correspond (" + old + "§f / " + oldArmor + ") (can occur at connection)");
            return;
        }
        ItemStack equipped = event.getNewItem();
        if(equipped == null && oldArmor == null){
            ConsulatAPI.getConsulatAPI().log(Level.INFO, "Armor are not enchanted");
            return;
        }
        player.setArmor(slot, event.getNewItem());
        CEnchantedItem newArmor = player.getArmor(slot);
        Player bukkitPlayer = player.getPlayer();
        if(oldArmor != null){
            CEnchantment[] armorEnchants = oldArmor.getEnchants();
            for(CEnchantment enchant : armorEnchants){
                boolean removeEnchant = true;
                for(int i = 0; i < 4; ++i){
                    if(i != slot.ordinal()){
                        CEnchantedItem armorPart = player.getArmor(i);
                        if(armorPart != null && armorPart.isEnchantedWith(enchant.getEnchantment(), enchant.getLevel())){
                            removeEnchant = false;
                            break;
                        }
                    }
                }
                if(removeEnchant){
                    PotionEffectType effect = enchant.getEnchantment().getEffect();
                    PotionEffect currentEffect = bukkitPlayer.getPotionEffect(effect);
                    if(currentEffect == null || currentEffect.getAmplifier() <= enchant.getLevel()){
                        bukkitPlayer.removePotionEffect(enchant.getEnchantment().getEffect());
                    }
                }
            }
        }
        if(newArmor != null){
            applyCEnchantment(player, newArmor.getEnchants());
        }
    }
    
    @EventHandler
    public void applyCEnchantment(EntityPotionEffectEvent event){
        if(!(event.getEntity() instanceof Player)){
            return;
        }
        PotionEffect oldEffect = event.getOldEffect();
        if(oldEffect == null || oldEffect.getAmplifier() == 0){
            return;
        }
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getEntity().getUniqueId());
        if(player == null){
            return;
        }
        for(int i = 0; i < 4; ++i){
            CEnchantedItem armorPart = player.getArmor(i);
            if(armorPart == null){
                continue;
            }
            for(CEnchantment enchantment : armorPart.getEnchants()){
                if(oldEffect.getType().equals(enchantment.getEnchantment().getEffect())){
                    Bukkit.getScheduler().scheduleSyncDelayedTask(ConsulatCore.getInstance(), () -> {
                        player.getPlayer().addPotionEffect(new PotionEffect(enchantment.getEnchantment().getEffect(), Integer.MAX_VALUE, enchantment.getLevel() - 1, false, false));
                    });
                    break;
                }
            }
        }
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
                        cancelAnvil(event);
                        return;
                    }
                    extraCost += 10;
                }
                if(CEnchantedItem.isEnchanted(result) && hasMending(result)){
                    cancelAnvil(event);
                    return;
                }
                event.setResult(result);
                int finalExtraCost = extraCost;
                Bukkit.getScheduler().runTask(ConsulatCore.getInstance(), () -> {
                    event.getInventory().setRepairCost(event.getInventory().getRepairCost() + finalExtraCost);
                });
            }
        }
        if(CEnchantedItem.isEnchanted(result) && hasMending(result)){
            cancelAnvil(event);
            return;
        }
        if(result != null && result.getType() != Material.AIR && result.getEnchantments().size() > 1 && result.containsEnchantment(Enchantment.ARROW_INFINITE) && CEnchantedItem.isEnchanted(result)){
            ItemMeta meta = result.getItemMeta();
            meta.removeEnchant(Enchantment.ARROW_INFINITE);
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
            result.setItemMeta(meta);
        }
        anvilEventCalled.put(event.getView().getPlayer().getUniqueId(), event.getResult());
    }
    
    @EventHandler
    public void onGrindstone(InventoryClickEvent event){
        if(!(event.getWhoClicked().getOpenInventory().getTopInventory() instanceof GrindstoneInventory)){
            return;
        }
        Bukkit.getScheduler().runTask(ConsulatCore.getInstance(), () -> {
            if(!(event.getWhoClicked().getOpenInventory().getTopInventory() instanceof GrindstoneInventory)){
                return;
            }
            GrindstoneInventory inventory = (GrindstoneInventory)event.getWhoClicked().getOpenInventory().getTopInventory();
            ItemStack result = inventory.getItem(2);
            if(CEnchantedItem.isEnchanted(result)){
                new CEnchantedItem(result).removeEnchants();
            }
        });
    }
    
    private boolean hasMending(ItemStack item){
        if(item == null || item.getType() == Material.AIR){
            return false;
        }
        if(item.getType() == Material.ENCHANTED_BOOK){
            EnchantmentStorageMeta book = (EnchantmentStorageMeta)item.getItemMeta();
            return book.hasStoredEnchant(Enchantment.MENDING);
        }
        return item.containsEnchantment(Enchantment.MENDING);
    }
    
    private void cancelAnvil(PrepareAnvilEvent event){
        event.setResult(null);
        Bukkit.getScheduler().runTask(ConsulatCore.getInstance(), () -> {
            event.getInventory().setRepairCost(0);
            ((Player)event.getView().getPlayer()).updateInventory();
        });
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
