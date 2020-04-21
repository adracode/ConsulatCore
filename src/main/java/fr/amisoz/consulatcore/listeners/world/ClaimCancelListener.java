package fr.amisoz.consulatcore.listeners.world;

import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.claims.Claim;
import fr.amisoz.consulatcore.claims.ClaimManager;
import fr.amisoz.consulatcore.players.SPlayerManager;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.player.CPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

public class ClaimCancelListener implements Listener {
    
    //TODO: Regarder si le projectile ne provient pas d'un dispenser ?
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event){
        Entity entity = event.getEntity();
        Claim claim = ClaimManager.getInstance().getClaim(entity.getLocation().getChunk());
        if(claim == null){
            return;
        }
        if(entity instanceof ItemFrame && event.getDamager() instanceof Player){
            if(!claim.isAllowed((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getDamager().getUniqueId()))){
                event.setCancelled(true);
            }
        }
        if(!(event.getDamager() instanceof Projectile)){
            return;
        }
        Projectile projectile = (Projectile)event.getDamager();
        if(!(projectile.getShooter() instanceof Player)){
            return;
        }
        if(!claim.isAllowed((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(
                ((Player)projectile.getShooter()).getUniqueId()))){
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void ravagerBlockDestroy(EntityChangeBlockEvent event){
        if(event.getEntity() instanceof Ravager){
            event.setCancelled(true);
        }
    }
    
    //Il semble que les interactions avec l'ItemFrame envoient
    //Deux events: PlayerInteractEntityEvent et PlayerInteractAtEntityEvent
    //contrairement aux armors stands qui envoient seulement PlayerInteractAtEntityEvent
    @EventHandler(priority = EventPriority.LOW)
    public void onInteractEntity(PlayerInteractEntityEvent event){
        cancelEvent(event);
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event){
        cancelEvent(event);
    }
    
    private void cancelEvent(PlayerInteractEntityEvent event){
        Player player = event.getPlayer();
        if(player.getWorld() != Bukkit.getWorlds().get(0)){
            return;
        }
        Entity entity = event.getRightClicked();
        if(entity.getType() == EntityType.ARMOR_STAND ||
                entity.getType() == EntityType.ITEM_FRAME){
            Claim claim = ClaimManager.getInstance().getClaim(player.getLocation().getChunk());
            if(claim != null && !claim.isAllowed((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId()))){
                event.setCancelled(true);
                player.sendMessage(Text.PREFIX + "§cAction impossible.");
            }
        }
    }
    
    @EventHandler
    public void onVehicleDamaged(VehicleDamageEvent event){
        if(event.getAttacker() == null){
            return;
        }
        Claim claim = ClaimManager.getInstance().getClaim(event.getVehicle().getLocation().getChunk());
        if(claim == null){
            return;
        }
        if(event.getAttacker() instanceof Player){
            if(!claim.isAllowed((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getAttacker().getUniqueId()))){
                event.setCancelled(true);
            }
        }
        if(!(event.getAttacker() instanceof Projectile)){
            return;
        }
        Projectile projectile = (Projectile)event.getAttacker();
        if(!(projectile.getShooter() instanceof Player)){
            return;
        }
        if(!claim.isAllowed((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(
                ((Player)projectile.getShooter()).getUniqueId()))){
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamaged(EntityDamageByEntityEvent event){
        if(event.getEntity().getWorld() != Bukkit.getWorlds().get(0)){
            return;
        }
        EntityType type = event.getEntityType();
        if(type == EntityType.PRIMED_TNT || type == EntityType.MINECART_TNT){
            event.setCancelled(true);
            return;
        }
        if(type != EntityType.PLAYER){
            if(event.getDamager() instanceof Player){
                Player player = (Player)event.getDamager();
                Claim claim = ClaimManager.getInstance().getClaim(event.getEntity().getLocation().getChunk());
                if(claim != null && !claim.isAllowed((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId()))){
                    event.setCancelled(true);
                    player.sendMessage(Text.PREFIX + "§cAction impossible.");
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        if(player.getWorld() != Bukkit.getWorlds().get(0)){
            return;
        }
        Claim claim = ClaimManager.getInstance().getClaim(event.getBlock().getChunk());
        if(claim != null && !claim.isAllowed((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId()))){
            event.setCancelled(true);
            player.sendMessage(Text.PREFIX + "§cAction impossible.");
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onPlaceBucket(PlayerBucketEmptyEvent event){
        Player player = event.getPlayer();
        if(player.getWorld() != Bukkit.getWorlds().get(0)){
            return;
        }
        Claim claim = ClaimManager.getInstance().getClaim(event.getBlock().getChunk());
        if(claim != null && !claim.isAllowed((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId()))){
            event.setCancelled(true);
            player.sendMessage(Text.PREFIX + "§cAction impossible.");
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityExplode(EntityExplodeEvent event){
        Entity entity = event.getEntity();
        if(entity.getWorld() != Bukkit.getWorlds().get(0)){
            return;
        }
        if(ClaimManager.getInstance().isClaimed(entity.getLocation().getChunk())){
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onBlockExplode(BlockExplodeEvent event){
        Block block = event.getBlock();
        if(block.getWorld() != Bukkit.getWorlds().get(0)){
            return;
        }
        if(ClaimManager.getInstance().isClaimed(block.getChunk())){
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        if(player.getWorld() != Bukkit.getWorlds().get(0)){
            return;
        }
        Claim claim = ClaimManager.getInstance().getClaim(event.getBlock().getChunk());
        if(claim != null && !claim.isAllowed((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId()))){
            event.setCancelled(true);
            player.sendMessage(Text.PREFIX + "§cAction impossible.");
        }
    }
    
    //TODO: Faire un canceler spécifique pour régler le bug du manger
    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if(player.getWorld() != Bukkit.getWorlds().get(0)){
            return;
        }
        if(event.getClickedBlock() == null){
            return;
        }
        Claim claim = ClaimManager.getInstance().getClaim(event.getClickedBlock().getChunk());
        if(claim != null && !claim.isAllowed((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId()))){
            if(event.getClickedBlock().getType() != Material.OAK_WALL_SIGN){
                event.setCancelled(true);
                player.sendMessage(Text.PREFIX + "§cAction impossible.");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onMinecartCollision(VehicleEntityCollisionEvent event){
        if(event.getVehicle().getType() != (EntityType.MINECART_HOPPER)){
            return;
        }
        if(!(event.getEntity() instanceof Player)){
            return;
        }
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getEntity().getUniqueId());
        Claim claim = player.getClaimLocation();
        if(claim != null && !claim.isAllowed(player)){
            event.setCancelled(true);
            event.setCollisionCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onPistonPush(BlockPistonExtendEvent event){
        if(event.getBlocks().size() == 0){
            return;
        }
        ClaimManager claimManager = ClaimManager.getInstance();
        BlockFace direction = event.getDirection();
        Claim claimPiston = claimManager.getClaim(event.getBlock().getChunk());
        for(Block block : event.getBlocks()){
            if(!isBlockMovableByPiston(claimPiston, claimManager.getClaim(block.getRelative(direction).getChunk()))){
                event.setCancelled(true);
                break;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onPistonRetract(BlockPistonRetractEvent event){
        if(event.getBlocks().size() == 0){
            return;
        }
        ClaimManager claimManager = ClaimManager.getInstance();
        Claim claimPiston = claimManager.getClaim(event.getBlock().getChunk());
        for(Block block : event.getBlocks()){
            if(!isBlockMovableByPiston(claimPiston, claimManager.getClaim(block.getChunk()))){
                event.setCancelled(true);
                break;
            }
        }
    }
    
    private boolean isBlockMovableByPiston(Claim pistonChunk, Claim chunk){
        //Un piston peut interagir avec un bloc non claim
        if(pistonChunk != null && chunk == null) return true;
        //Un piston non claim ne peut interagit avec un bloc claim
        if(pistonChunk == null && chunk != null) return false;
        //Si le piston et le bloc sont non claim, l'interaction est autorisé
        if(chunk == null) return true;
        //L'interaction n'est possible que si les deux claims appartiennent au même joueur
        return pistonChunk.isOwner(chunk.getOwner());
    }
    
}
