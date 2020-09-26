package fr.leconsulat.core.players;

import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.events.ConsulatPlayerLeaveEvent;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.MainPage;
import fr.leconsulat.api.gui.gui.module.api.Pageable;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.events.SurvivalPlayerLoadedEvent;
import fr.leconsulat.core.guis.pvp.PVPGui;
import fr.leconsulat.core.guis.shop.ShopGui;
import fr.leconsulat.core.zones.cities.City;
import fr.leconsulat.core.zones.claims.Claim;
import fr.leconsulat.core.zones.claims.ClaimManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Random;
import java.util.UUID;

public class PVPManager implements Listener {
    
    private static PVPManager instance;
    private PVPGui pvpGui = new PVPGui();
    private Random random = new Random();
    
    public PVPManager(){
        if(instance != null){
            throw new IllegalStateException();
        }
        instance = this;
        Bukkit.getServer().getPluginManager().registerEvents(this, ConsulatAPI.getConsulatAPI());
        Bukkit.getScheduler().runTaskTimer(ConsulatAPI.getConsulatAPI(), () -> {
            for(GuiItem item : pvpGui){
                SurvivalPlayer player = (SurvivalPlayer)item.getAttachedObject();
                if(player != null && player.isInCombat()){
                    player.sendActionBar(Text.IN_COMBAT(player.getCombatCooldown() / 1_000));
                }
            }
        }, 20L, 20L);
    }
    
    private String getDeathMessage(String damaged, String damager){
        switch(random.nextInt(3)){
            case 0:
                return damaged + " est mort... " + damager + " est le coupable !";
            case 1:
                return "Il semble que " + damaged + " soit mort a cause de " + damager;
            case 2:
                return "Aie ! " + damager + " a explosé " + damaged;
        }
        return "";
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCombat(EntityDamageByEntityEvent event){
        if(event.getEntityType() != EntityType.PLAYER || event.getDamager().getType() != EntityType.PLAYER){
            return;
        }
        event.setCancelled(true);
        SurvivalPlayer damaged = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getEntity().getUniqueId());
        if(damaged == null){
            return;
        }
        SurvivalPlayer damager = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getDamager().getUniqueId());
        if(damager == null || !damaged.isPvp() || !damager.isPvp() || damaged.equals(damager)){
            return;
        }
        Claim damagedClaim = ClaimManager.getInstance().getClaim(damaged.getPlayer().getChunk());
        if(damagedClaim != null && damagedClaim.getOwner() instanceof City){
            if(((City)damagedClaim.getOwner()).isNoDamage()){
                return;
            }
        }
        Claim damagerClaim = ClaimManager.getInstance().getClaim(damager.getPlayer().getChunk());
        if(damagerClaim != null && damagerClaim.getOwner() instanceof City){
            if(((City)damagerClaim.getOwner()).isNoDamage()){
                return;
            }
        }
        event.setCancelled(false);
        if(!damaged.isInCombat()){
            nowInCombat(damaged);
        }
        damaged.setLastHit();
        if(!damager.isInCombat()){
            nowInCombat(damager);
        }
        damager.setLastHit();
        damaged.setLastDamager(damager.getUUID());
    }
    
    private void nowInCombat(SurvivalPlayer player){
        player.sendMessage(Text.NOW_IN_COMBAT);
        if(player.isFlying()){
            player.disableFly();
        }
        IGui currentlyOpen = player.getCurrentlyOpen();
        if(currentlyOpen instanceof Pageable){
            MainPage mainPage = ((Pageable)currentlyOpen).getMainPage();
            if(mainPage.getGui() instanceof PVPGui || mainPage.getGui() instanceof ShopGui){
                player.getPlayer().closeInventory();
            }
        }
    }
    
    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getEntity().getUniqueId());
        if(player.isInCombat() && player.getLastDamager() != null){
            player.setPvp(false);
            player.sendMessage(Text.DEAD_IN_COMBAT);
            event.setDeathMessage(getDeathMessage(player.getName(), Bukkit.getOfflinePlayer(player.getLastDamager()).getName()));
            if(ConsulatAPI.getConsulatAPI().isDevelopment()){
                event.getDrops().add(getHead(player.getUUID()));
            } else if(random.nextInt(100) < 16){
                event.getDrops().add(getHead(player.getUUID()));
            }
            player.setLastDamager(null);
        }
    }
    
    private ItemStack getHead(UUID uuid){
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta)head.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
        head.setItemMeta(meta);
        return head;
    }
    
    @EventHandler
    public void onJoin(SurvivalPlayerLoadedEvent event){
        SurvivalPlayer player = event.getPlayer();
        if(player.isPvp()){
            pvpGui.addPlayer(player);
        }
    }
    
    @EventHandler
    public void onQuit(ConsulatPlayerLeaveEvent event){
        SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
        if(player.isPvp()){
            pvpGui.removePlayer(player);
            if(player.isInCombat()){
                player.getPlayer().setHealth(0);
            }
        }
    }
    
    public PVPGui getPvpGui(){
        return pvpGui;
    }
    
    public static PVPManager getInstance(){
        return instance;
    }
}
