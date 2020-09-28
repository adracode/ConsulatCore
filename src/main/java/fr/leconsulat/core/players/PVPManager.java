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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;

import java.util.*;

public class PVPManager implements Listener {
    
    private static PVPManager instance;
    private PVPGui pvpGui = new PVPGui();
    private Random random = new Random();
    private final Set<PotionEffectType> debuff = new HashSet<>(Arrays.asList(
            PotionEffectType.BLINDNESS,
            PotionEffectType.CONFUSION,
            PotionEffectType.HARM,
            PotionEffectType.HUNGER,
            PotionEffectType.LEVITATION,
            PotionEffectType.POISON,
            PotionEffectType.SLOW,
            PotionEffectType.SLOW_DIGGING,
            PotionEffectType.SLOW_FALLING,
            PotionEffectType.UNLUCK,
            PotionEffectType.WEAKNESS,
            PotionEffectType.WITHER
    ));
    
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
        if(event.getEntityType() != EntityType.PLAYER){
            return;
        }
        event.setCancelled(true);
        SurvivalPlayer damaged = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getEntity().getUniqueId());
        if(damaged == null || !damaged.isPvp()){
            return;
        }
        SurvivalPlayer damager;
        if(event.getDamager().getType() == EntityType.PLAYER){
            damager = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getDamager().getUniqueId());
        } else if(event.getDamager() instanceof Projectile){
            Projectile projectile = (Projectile)event.getDamager();
            ProjectileSource source = projectile.getShooter();
            //Si un bloc lance le projectile (dispenser)
            if(source instanceof BlockProjectileSource){
                event.setCancelled(false);
                return;
            }
            if(!(source instanceof Player)){
                event.setCancelled(false);
                return;
            } else {
                damager = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(((Player)source).getUniqueId());
            }
        } else {
            event.setCancelled(false);
            return;
        }
        if(damager == null || !damager.isPvp() || damaged.equals(damager)){
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
        setCombat(damaged, damager);
    }
    
    private void setCombat(SurvivalPlayer damaged, SurvivalPlayer damager){
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
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLingeringApply(AreaEffectCloudApplyEvent event){
        if(isDebuff(event.getEntity().getBasePotionData().getType().getEffectType())){
            checkPveDamage(event.getEntity().getSource(), event.getAffectedEntities());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileHit(PotionSplashEvent event){
        boolean debuff = false;
        for(PotionEffect effect : event.getPotion().getEffects()){
            if(isDebuff(effect.getType())){
                debuff = true;
            }
        }
        if(debuff){
            checkPveDamage(event.getEntity().getShooter(), event.getAffectedEntities());
        }
    }
    
    private boolean isDebuff(PotionEffectType type){
       return debuff.contains(type);
    }
    
    private void checkPveDamage(ProjectileSource shooter, Collection<LivingEntity> affectedEntities){
        if(shooter instanceof Player){
            SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(((Player)shooter).getUniqueId());
            boolean cancel = player == null || !player.isPvp();
            for(Iterator<LivingEntity> iterator = affectedEntities.iterator(); iterator.hasNext(); ){
                LivingEntity entity = iterator.next();
                if(entity.getType() == EntityType.PLAYER){
                    if(cancel){
                        if(player != null && player.getUUID() == entity.getUniqueId()){
                            continue;
                        }
                        iterator.remove();
                    } else {
                        SurvivalPlayer affected = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(entity.getUniqueId());
                        if(affected.equals(player)){
                            continue;
                        }
                        if(affected.isPvp()){
                            setCombat(affected, player);
                        } else {
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onIgnition(BlockIgniteEvent event){
        if(event.getPlayer() != null){
            checkPveDamage(event, (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId()), event.getBlock().getLocation());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEmptyBucket(PlayerBucketEmptyEvent event){
        if(event.getBucket() == Material.LAVA_BUCKET){
            checkPveDamage(event, (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId()), event.getBlockClicked().getLocation());
        }
    }
    
    private void checkPveDamage(Cancellable cancellable, SurvivalPlayer pveDamager, Location location){
        List<Player> nearbyPlayers = (List<Player>)location.getNearbyPlayers(3, 2);
        if(nearbyPlayers.size() > 1 || (nearbyPlayers.size() == 1 && !nearbyPlayers.get(0).getUniqueId().equals(pveDamager.getUUID()))){
            boolean cancel = !pveDamager.isPvp();
            if(!cancel){
                for(Player player : nearbyPlayers){
                    if(!((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId())).isPvp()){
                        cancel = true;
                        break;
                    }
                }
            }
            if(cancel){
                cancellable.setCancelled(true);
                pveDamager.sendActionBar(Text.ANOTHER_PLAYER_NEAR);
            }
        }
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
