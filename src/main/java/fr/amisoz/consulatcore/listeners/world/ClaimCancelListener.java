package fr.amisoz.consulatcore.listeners.world;

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import fr.amisoz.consulatcore.chunks.CChunk;
import fr.amisoz.consulatcore.chunks.ChunkManager;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.utils.ChestUtils;
import fr.amisoz.consulatcore.utils.CoordinatesUtils;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.amisoz.consulatcore.zones.claims.ClaimManager;
import fr.amisoz.consulatcore.zones.claims.ClaimPermission;
import fr.leconsulat.api.events.blocks.*;
import fr.leconsulat.api.events.entities.PlayerInteractWithEntityEvent;
import fr.leconsulat.api.events.items.PlayerPlaceItemEvent;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.utils.Rollback;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.projectiles.BlockProjectileSource;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

//TODO: On peut mettre du charbon dans un wagon
//on peut pousser les mobs -> impossible à enlever sans enlever toutes les collisions
//on peut jeter des items dans les hoppers -> à interdire ?
public class ClaimCancelListener implements Listener {
    
    private ChunkManager chunkManager = ChunkManager.getInstance();
    private ClaimManager claimManager = ClaimManager.getInstance();
    
    //Bloc est détruit par un joueur
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBreakBlock(BlockBreakEvent event){
        CChunk blockChunk = chunkManager.getChunk(event.getBlock().getChunk());
        if(blockChunk instanceof Claim){
            Claim blockClaim = (Claim)blockChunk;
            if(!blockClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId()),
                    ClaimPermission.BREAK_BLOCK)){
                event.setCancelled(true);
                return;
            } else {
                if(ClaimManager.PROTECTABLE.contains(event.getBlock().getType())){
                    UUID opener = event.getPlayer().getUniqueId();
                    UUID owner = blockClaim.getProtectedContainer(CoordinatesUtils.convertCoordinates(event.getBlock().getLocation()));
                    if(owner != null){
                        if(!owner.equals(opener)){
                            event.getPlayer().sendActionBar("§cCe coffre est privé");
                            event.setCancelled(true);
                            return;
                        } else {
                            blockClaim.freeContainer(event.getBlock());
                        }
                    }
                }
            }
        }
        blockChunk.decrementLimit(event.getBlock().getType());
    }
    
    @EventHandler
    public void onBurn(BlockBurnEvent event){
        if(event.getIgnitingBlock() != null){
            if(!Claim.canInteract(event.getIgnitingBlock().getChunk(), event.getBlock().getChunk())){
                event.setCancelled(true);
                return;
            }
        }
        chunkManager.getChunk(event.getBlock()).decrementLimit(event.getBlock().getType());
    }
    
    /*
    @EventHandler
    public void onCanBuild(BlockCanBuildEvent event){
        Player player = event.getPlayer();
        //Player == null -> Shulker posé par un dispenser
        if(player != null){
            Claim claim = claimManager.getClaim(event.getBlock());
            if(claim != null && !claim.canPlaceBlock((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId()))){
                event.setBuildable(false);
            }
        }
    }
    */
    //BlockCookEvent -> Item cuit dans un four
    //BlockDamageEvent -> Bloc reçoit des dégâts
    
    //Item qui peut être équipé lancé par un dispenser sur une entité
    @EventHandler(ignoreCancelled = true)
    public void onBlockDispenseArmor(BlockDispenseArmorEvent event){
        if(!(event.getTargetEntity() instanceof Player)){
            return;
        }
        Claim claim = claimManager.getClaim(event.getBlock());
        if(claim != null && !claim.canInteractDispenser((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getTargetEntity().getUniqueId()))){
            event.setCancelled(true);
        }
    }
    
    //Item lancé par un dispenser
    @EventHandler
    public void onDispenser(BlockDispenseEvent event){
        Block dispenser = event.getBlock();
        if(!(dispenser.getBlockData() instanceof Dispenser)){
            return;
        }
        Block face = dispenser.getRelative(((Dispenser)dispenser.getBlockData()).getFacing());
        if(!Claim.canInteract(face.getChunk(), dispenser.getChunk())){
            event.setCancelled(true);
        }
    }
    
    //BlockDropItemEvent -> Bloc cassé par un joueur drop un item
    //BlockEvent -> Event générique
    //BlockExpEvent -> Bloc drop de l'xp
    
    //Bloc explose (ex: lit dans le nether)
    @EventHandler(priority = EventPriority.LOW)
    public void onBlockExplode(BlockExplodeEvent event){
        Chunk origin = event.getBlock().getChunk();
        CChunk originChunk = chunkManager.getChunk(origin);
        originChunk.decrementLimit(event.getBlock().getType());
        for(Iterator<Block> iterator = event.blockList().iterator(); iterator.hasNext(); ){
            Block block = iterator.next();
            CChunk blockChunk = chunkManager.getChunk(block);
            if(!Claim.canInteract(originChunk, blockChunk)){
                iterator.remove();
                continue;
            }
            blockChunk.decrementLimit(block.getType());
        }
    }
    
    @EventHandler
    public void onFade(BlockFadeEvent event){
        CChunk chunk = chunkManager.getChunk(event.getBlock());
        if(!chunk.incrementLimit(event.getNewState().getType())){
            event.setCancelled(true);
            return;
        }
        chunk.decrementLimit(event.getBlock().getType());
    }
    
    //Bloc fertilisé (arbre, graines...)
    @EventHandler(ignoreCancelled = true)
    public void onFertilize(BlockFertilizeEvent event){
        Player player = event.getPlayer();
        CChunk chunk = chunkManager.getChunk(event.getBlock());
        if(player != null){
            if(chunk instanceof Claim && !((Claim)chunk).canFertilize((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId()))){
                event.setCancelled(true);
                return;
            }
        }
        Rollback onCancel = new Rollback();
        for(BlockState block : event.getBlocks()){
            CChunk blockChunk = chunkManager.getChunk(block.getLocation());
            if(!blockChunk.incrementLimit(block.getType())){
                event.setCancelled(true);
                onCancel.execute();
                return;
            }
            onCancel.prepare(() -> blockChunk.decrementLimit(block.getType()));
        }
        chunk.decrementLimit(event.getBlock().getType());
    }
    
    @EventHandler
    public void onForm(BlockFormEvent event){
        CChunk chunk = chunkManager.getChunk(event.getBlock());
        if(!chunk.incrementLimit(event.getNewState().getType())){
            event.setCancelled(true);
        }
        chunk.decrementLimit(event.getBlock().getType());
    }
    
    //On suppose que l'eau la lave et les oeufs de dragon ne sont pas limités par chunk parce que bon :Kappa:
    //Lava, water and dragon egg moves
    @EventHandler
    public void onFlow(BlockFromToEvent event){
        if(!Claim.canInteract(event.getBlock().getChunk(), event.getToBlock().getChunk())){
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onGrow(BlockGrowEvent event){
        CChunk chunk = chunkManager.getChunk(event.getBlock());
        if(!chunk.incrementLimit(event.getNewState().getType())){
            event.setCancelled(true);
        }
        chunk.decrementLimit(event.getBlock().getType());
    }
    
    //Bloc prend feu
    @EventHandler(ignoreCancelled = true)
    public void onIgnition(BlockIgniteEvent event){
        if(event.getIgnitingBlock() != null){
            if(!Claim.canInteract(event.getIgnitingBlock().getChunk(), event.getBlock().getChunk())){
                event.setCancelled(true);
                return;
            }
        }
        if(event.getPlayer() != null){
            Player player = event.getPlayer();
            Claim claim = claimManager.getClaim(player.getChunk());
            if(claim != null && !claim.canIgnite((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId()))){
                event.setCancelled(true);
            }
        } else if(event.getIgnitingEntity() != null){
            Chunk entityChunk;
            if(event.getCause() == BlockIgniteEvent.IgniteCause.FIREBALL){
                Location origin = event.getIgnitingEntity().getOrigin();
                if(origin == null){
                    return;
                }
                entityChunk = origin.getChunk();
            } else {
                entityChunk = event.getIgnitingEntity().getChunk();
            }
            if(!Claim.canInteract(entityChunk, event.getBlock().getChunk())){
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPlaceMulti(BlockMultiPlaceEvent event){
        if(event.getReplacedBlockStates().size() == 0){
            return;
        }
        for(BlockState block : event.getReplacedBlockStates()){
            if(!Claim.canInteract(event.getBlock().getChunk(), block.getChunk())){
                event.setCancelled(true);
                break;
            }
        }
        Claim blockClaim = claimManager.getClaim(event.getBlock().getChunk());
        if(blockClaim != null && !blockClaim.canInteract(
                (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId()),
                ClaimPermission.PLACE_BLOCK)){
            event.setCancelled(true);
        }
    }
    
    //BlockPhysicsEvent -> Appelé trop fréquemment pour être utilisé
    //BlockPistonEvent -> Retract ou Push
    
    //Bloc poussé
    @EventHandler(priority = EventPriority.LOW)
    public void onPistonPush(BlockPistonExtendEvent event){
        BlockFace direction = event.getDirection();
        if(!Claim.canInteract(event.getBlock().getChunk(), event.getBlock().getRelative(direction).getChunk())){
            event.setCancelled(true);
            return;
        }
        if(event.getBlocks().size() != 0){
            for(Block block : event.getBlocks()){
                if(!Claim.canInteract(event.getBlock().getChunk(), block.getRelative(direction).getChunk())){
                    event.setCancelled(true);
                    return;
                }
            }
            Rollback onCancel = new Rollback();
            for(Block block : event.getBlocks()){
                Location futureLocation = block.getRelative(direction).getLocation();
                if(futureLocation.getChunk() == block.getChunk()){
                    continue;
                }
                CChunk futureChunk = chunkManager.getChunk(futureLocation);
                CChunk currentChunk = chunkManager.getChunk(block);
                Material type = block.getType();
                currentChunk.decrementLimit(type);
                if(!futureChunk.incrementLimit(type)){
                    event.setCancelled(true);
                    currentChunk.incrementLimit(type);
                    onCancel.execute();
                    return;
                }
                onCancel.prepare(() -> {
                    futureChunk.decrementLimit(type);
                    currentChunk.incrementLimit(type);
                });
            }
        }
        
    }
    
    //Bloc tiré
    @EventHandler(priority = EventPriority.LOW)
    public void onPistonRetract(BlockPistonRetractEvent event){
        if(event.getBlocks().size() != 0){
            for(Block block : event.getBlocks()){
                if(!Claim.canInteract(event.getBlock().getChunk(), block.getChunk())){
                    event.setCancelled(true);
                    return;
                }
            }
            Rollback onCancel = new Rollback();
            BlockFace direction = event.getDirection();
            for(Block block : event.getBlocks()){
                Location futureLocation = block.getRelative(direction).getLocation();
                if(futureLocation.getChunk() == block.getChunk()){
                    continue;
                }
                CChunk futureChunk = chunkManager.getChunk(futureLocation);
                CChunk currentChunk = chunkManager.getChunk(block);
                Material type = block.getType();
                currentChunk.decrementLimit(type);
                if(!futureChunk.incrementLimit(type)){
                    event.setCancelled(true);
                    currentChunk.incrementLimit(type);
                    onCancel.execute();
                    return;
                }
                onCancel.prepare(() -> {
                    futureChunk.decrementLimit(type);
                    currentChunk.incrementLimit(type);
                });
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event){
        CChunk blockChunk = chunkManager.getChunk(event.getBlock().getChunk());
        Claim blockClaim = blockChunk instanceof Claim ? (Claim)blockChunk : null;
        if(blockClaim != null && !blockClaim.canInteract(
                (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId()),
                ClaimPermission.PLACE_BLOCK)){
            event.setCancelled(true);
            return;
        } else if(blockClaim != null){
            if(ClaimManager.PROTECTABLE.contains(event.getBlock().getType())){
                Block placedChest = event.getBlock();
                if(ChestUtils.isDoubleChest((Chest)placedChest.getState())){
                    Block otherChest = ChestUtils.getNextChest(placedChest);
                    if(otherChest != null){
                        if(blockClaim.getProtectedContainer(CoordinatesUtils.convertCoordinates(otherChest.getLocation())) != null){
                            ChestUtils.setChestsSingle(placedChest, otherChest);
                            event.getPlayer().sendBlockChange(otherChest.getLocation(), otherChest.getBlockData());
                        }
                    }
                }
            }
        }
        Block block = event.getBlock();
        if(ChestUtils.isChest(block.getType())){
            Chest chest = (Chest)block.getState();
            if(ChestUtils.isDoubleChest(chest)){
                Block nextChest = ChestUtils.getNextChest(block);
                if(nextChest != null){
                    if(!Claim.canInteract(block.getChunk(), nextChest.getChunk())){
                        ChestUtils.setChestsSingle(block, nextChest);
                        event.getPlayer().sendBlockChange(nextChest.getLocation(), nextChest.getBlockData());
                    }
                }
            }
        }
        if(!blockChunk.incrementLimit(event.getBlockPlaced().getType())){
            event.getPlayer().sendActionBar("§cCe chunk à atteint la limite pour ce bloc.");
            event.setCancelled(true);
        }
    }
    
    //BlockRedstoneEvent -> Changement de redstone sur un bloc
    
    //Dispenser tond un mouton
    @EventHandler
    public void onDispenserShear(BlockShearEntityEvent event){
        if(!Claim.canInteract(event.getBlock().getChunk(), event.getEntity().getChunk())){
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onSpread(BlockSpreadEvent event){
        if(!Claim.canInteract(event.getSource().getChunk(), event.getBlock().getChunk())){
            event.setCancelled(true);
            return;
        }
        CChunk chunk = chunkManager.getChunk(event.getBlock());
        if(!chunk.incrementLimit(event.getNewState().getType())){
            event.setCancelled(true);
            return;
        }
        chunk.decrementLimit(event.getBlock().getType());
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onCauldronChanged(CauldronLevelChangeEvent event){
        switch(event.getReason()){
            case EXTINGUISH:
                if(!(event.getEntity() instanceof Player)){
                    return;
                }
            case BUCKET_FILL:
            case BUCKET_EMPTY:
            case BOTTLE_FILL:
            case BOTTLE_EMPTY:
            case BANNER_WASH:
            case ARMOR_WASH:
                if(event.getEntity() == null){
                    return;
                }
                Claim claim = claimManager.getClaim(event.getBlock().getChunk());
                if(claim != null && claim.canUseCauldron((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getEntity().getUniqueId()))){
                    event.setCancelled(true);
                }
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onEntityFormBlock(EntityBlockFormEvent event){
        CChunk chunk = chunkManager.getChunk(event.getBlock().getChunk());
        if(event.getEntity() instanceof Player){
            if(chunk instanceof Claim && !((Claim)chunk).canFormBlock((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getEntity().getUniqueId()))){
                event.setCancelled(true);
                return;
            }
        }
        if(!chunk.incrementLimit(event.getNewState().getType())){
            event.setCancelled(true);
        }
        chunk.decrementLimit(event.getBlock().getType());
    }
    
    //FluidLevelChangeEvent -> ?
    //LeavesDecayEvent -> Feuille qui dépop
    //MoistureChangeEvent -> Terre labourrée qui change
    //NotePlayEvent -> Une note est jouée
    //SignChangeEvent -> Panneau changée par un joueur
    
    @EventHandler
    public void onSpongeAbsorb(SpongeAbsorbEvent event){
        if(event.getBlocks().size() == 0){
            return;
        }
        event.getBlocks().removeIf(blockState -> !Claim.canInteract(event.getBlock().getChunk(), blockState.getChunk()));
    }
    
    @EventHandler
    public void onLingeringApply(AreaEffectCloudApplyEvent event){
        Claim claim = claimManager.getClaim(event.getEntity().getChunk());
        if(claim == null){
            return;
        }
        for(Iterator<LivingEntity> iterator = event.getAffectedEntities().iterator(); iterator.hasNext(); ){
            Entity entity = iterator.next();
            if(entity instanceof Player){
                if(!claim.canReceivePotion((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(entity.getUniqueId()))){
                    iterator.remove();
                }
            }
        }
    }
    
    //BatToggleSleepEvent
    //CreatureSpawnEvent
    //CreeperPowerEvent
    //EnderDragonChangePhaseEvent
    //EntityAirChangeEvent
    //EntityBreakDoorEvent
    //EntityBreedEvent
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event){
        Entity entity = event.getEntity();
        if(entity instanceof Ravager || entity instanceof Wither){
            event.setCancelled(true);
            return;
        }
        if(entity.getType() == EntityType.FALLING_BLOCK && entity.isDead()){
            Entity fallingBlock = event.getEntity();
            Location from = fallingBlock.getOrigin();
            if(from != null){
                if(!Claim.canInteract(from.getChunk(), fallingBlock.getChunk())){
                    event.setCancelled(true);
                    return;
                }
            }
        }
        CChunk chunk = chunkManager.getChunk(event.getBlock());
        if(!chunk.incrementLimit(event.getTo())){
            event.setCancelled(true);
            return;
        }
        chunk.decrementLimit(event.getBlock().getType());
    }
    
    //EntityCombustByBlockEvent
    //EntityCombustByEntityEvent
    //EntityCombustEvent
    //EntityDamageByBlockEvent
    
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event){
        if(event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ||
                event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION){
            event.setCancelled(true);
            return;
        }
        Entity entity = event.getEntity();
        Claim entityClaim = claimManager.getClaim(entity.getLocation().getChunk());
        //Le mob est dans un chunk libre
        if(entityClaim == null){
            return;
        }
        //Un joueur tape un mob
        if(entity.getType() != EntityType.PLAYER && event.getDamager() instanceof Player){
            Player player = (Player)event.getDamager();
            if(!entityClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId()))){
                event.setCancelled(true);
                return;
            }
        }
        if(!(event.getDamager() instanceof Projectile)){
            return;
        }
        //Si l'entité qui tape est un projectile (flèche, trident...)
        Projectile projectile = (Projectile)event.getDamager();
        //Si un bloc lance le projectile (dispenser)
        if(projectile.getShooter() instanceof BlockProjectileSource){
            if(!Claim.canInteract(
                    ((BlockProjectileSource)projectile.getShooter()).getBlock().getChunk(),
                    entity.getChunk())){
                event.setCancelled(true);
                removeProjectile(projectile);
                return;
            }
        }
        if(!(projectile.getShooter() instanceof Player)){
            return;
        }
        //Si un joueur lance le projectile
        if(!entityClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(
                ((Player)projectile.getShooter()).getUniqueId()))){
            event.setCancelled(true);
            removeProjectile(projectile);
        }
    }
    
    private void removeProjectile(Projectile projectile){
        if(projectile.getType() == EntityType.ARROW || projectile.getType() == EntityType.SPECTRAL_ARROW){
            projectile.remove();
            return;
        }
    }
    
    //EntityDamageEvent
    //EntityDeathEvent
    //EntityDropItemEvent
    
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityExplode(EntityExplodeEvent event){
        Location locOrigin = event.getEntity().getOrigin();
        if(locOrigin == null){
            event.setCancelled(true);
            return;
        }
        CChunk originChunk = chunkManager.getChunk(locOrigin.getChunk());
        for(Iterator<Block> iterator = event.blockList().iterator(); iterator.hasNext(); ){
            Block block = iterator.next();
            CChunk blockChunk = chunkManager.getChunk(block);
            if(!Claim.canInteract(originChunk, blockChunk)){
                iterator.remove();
                continue;
            }
            blockChunk.decrementLimit(block.getType());
        }
    }
    
    //EntityInteractEvent
    
    //https://hub.spigotmc.org/jira/browse/SPIGOT-5243?jql=labels%20%3D%20Arrow
    //@EventHandler
    public void onPickupArrow(PlayerPickupArrowEvent event){
        /*Claim arrowClaim = claimManager.getClaim(event.getArrow().getChunk());
        if(arrowClaim != null && !arrowClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId()))){
            event.setCancelled(true);
        }*/
    }
    //EntityPortalEnterEvent
    //EntityPortalEvent
    //EntityPortalExitEvent
    //EntityPoseChangeEvent
    //EntityPotionEffectEvent
    //EntityRegainHealthEvent
    //EntityResurrectEvent
    //EntityShootBowEvent
    //EntitySpawnEvent
    //EntityTameEvent
    //EntityTargetEvent
    //EntityTargetLivingEntityEvent
    //EntityTeleportEvent
    //EntityToggleGlideEvent
    //EntityToggleSwimEvent
    //EntityTransformEvent
    
    @EventHandler
    public void onUnleash(EntityUnleashEvent event){
        if(!(event instanceof PlayerUnleashEntityEvent)){
            return;
        }
        PlayerUnleashEntityEvent entityEvent = (PlayerUnleashEntityEvent)event;
        Claim entityLeashed = claimManager.getClaim(entityEvent.getPlayer().getChunk());
        if(entityLeashed != null && !entityLeashed.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(entityEvent.getPlayer().getUniqueId()))){
            entityEvent.setCancelled(true);
        }
    }
    
    //ExpBottleEvent
    //ExplosionPrimeEvent
    //FireworkExplodeEvent
    //FoodLevelChangeEvent
    //HorseJumpEvent
    //ItemDespawnEvent
    //ItemMergeEvent
    //ItemSpawnEvent
    
    @EventHandler
    public void onProjectileHit(LingeringPotionSplashEvent event){
        Projectile projectile = event.getEntity();
        if(projectile.getShooter() instanceof BlockProjectileSource){
            if(!Claim.canInteract(((BlockProjectileSource)projectile.getShooter()).getBlock().getChunk(), event.getEntity().getChunk())){
                event.setCancelled(true);
            }
        } else if(projectile.getShooter() instanceof Player){ //Si un joueur lance le projectile
            Claim hitClaim = claimManager.getClaim(event.getEntity().getChunk());
            if(hitClaim != null && !hitClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(
                    ((Player)projectile.getShooter()).getUniqueId()))){
                event.setCancelled(true);
            }
        }
    }
    
    //PigZapEvent
    //PigZombieAngerEvent
    //PlayerDeathEvent
    
    @EventHandler
    public void onPlayerLeashEntity(PlayerLeashEntityEvent event){
        Claim entityClaim = claimManager.getClaim(event.getEntity().getChunk());
        if(entityClaim != null && !entityClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId()))){
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onProjectileHit(PotionSplashEvent event){
        Projectile projectile = event.getEntity();
        if(projectile.getShooter() instanceof BlockProjectileSource){
            if(!Claim.canInteract(((BlockProjectileSource)projectile.getShooter()).getBlock().getChunk(), event.getEntity().getChunk())){
                event.setCancelled(true);
            }
        } else if(projectile.getShooter() instanceof Player){ //Si un joueur lance le projectile
            Claim hitClaim = claimManager.getClaim(event.getEntity().getChunk());
            if(hitClaim != null && !hitClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(
                    ((Player)projectile.getShooter()).getUniqueId()))){
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onProjectileCollide(ProjectileCollideEvent event){
        Projectile projectile = event.getEntity();
        if(!(projectile.getShooter() instanceof Player)){
            return;
        }
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(((Player)projectile.getShooter()).getUniqueId());
        Claim entityClaim = claimManager.getClaim(event.getCollidedWith().getChunk());
        if(entityClaim != null && !entityClaim.canInteract(player)){
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event){
        if(!(event.getEntity().getShooter() instanceof Player)){
            return;
        }
        Player player = (Player)event.getEntity().getShooter();
        Claim claim = claimManager.getClaim(player.getChunk());
        if(claim != null && !claim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId()))){
            event.setCancelled(true);
        }
    }
    
    //SheepDyeWoolEvent
    //SheepRegrowWoolEvent
    //SlimeSplitEvent
    //SpawnerSpawnEvent
    //VillagerAcquireTradeEvent
    //VillagerCareerChangeEvent
    //VillagerReplenishTradeEvent
    
    @EventHandler
    public void onHangingEntityDamageByEntity(HangingBreakByEntityEvent event){
        Entity remover = event.getRemover();
        //En cas d'explosion, remover = null donc on ne peut pas savoir d'où vient l'explosion
        if(event.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION){
            event.setCancelled(true);
            return;
        }
        if(remover instanceof Player){
            Claim entityClaim = claimManager.getClaim(event.getEntity().getChunk());
            if(entityClaim == null){
                return;
            }
            Player player = (Player)remover;
            if(!entityClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId()))){
                event.setCancelled(true);
            }
        } else if(remover instanceof Projectile){
            Projectile projectile = (Projectile)remover;
            if(projectile.getShooter() instanceof Player){
                Claim entityClaim = claimManager.getClaim(event.getEntity().getChunk());
                if(entityClaim == null){
                    return;
                }
                if(!entityClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(
                        ((Player)projectile.getShooter()).getUniqueId()))){
                    event.setCancelled(true);
                }
            } else if(projectile.getShooter() instanceof BlockProjectileSource){
                if(!Claim.canInteract(((BlockProjectileSource)projectile.getShooter()).getBlock().getChunk(), event.getEntity().getChunk())){
                    event.setCancelled(true);
                }
            }
        }
    }
    
    //HangingBreakEvent
    //HangingEvent
    
    @EventHandler
    public void onPlaceHanging(HangingPlaceEvent event){
        if(event.getPlayer() == null){
            return;
        }
        Hanging hanging = event.getEntity();
        if(hanging instanceof Painting){
            Location hangingPosition = hanging.getLocation().clone();
            int xOffset;
            int zOffset;
            switch(hanging.getFacing()){
                case EAST:
                    xOffset = 0;
                    zOffset = -1;
                    break;
                case NORTH:
                    xOffset = -1;
                    zOffset = 0;
                    break;
                case WEST:
                    xOffset = 0;
                    zOffset = 1;
                    break;
                case SOUTH:
                    xOffset = 1;
                    zOffset = 0;
                    break;
                default:
                    //Impossible
                    return;
            }
            SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
            int width = ((Painting)hanging).getArt().getBlockWidth();
            if(width == 4){
                hangingPosition.add(-xOffset, 0, -zOffset);
            }
            for(int i = 0; i < width; ++i){
                Claim hangingClaim = claimManager.getClaim(hangingPosition.getChunk());
                if(hangingClaim != null && !hangingClaim.canInteract(player)){
                    event.setCancelled(true);
                    return;
                }
                hangingPosition.add(xOffset, 0, zOffset);
            }
        } else {
            Claim hangingClaim = claimManager.getClaim(hanging.getChunk());
            if(hangingClaim != null && !hangingClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId()))){
                event.setCancelled(true);
            }
        }
    }
    
    //BrewEvent
    //BrewingStandFuelEvent
    //CraftItemEvent
    //FurnaceBurnEvent
    //FurnaceExtractEvent
    //FurnaceSmeltEvent
    //InventoryClickEvent
    //InventoryCloseEvent
    //InventoryCreativeEvent
    //InventoryDragEvent
    //InventoryInteractEvent
    
    @EventHandler
    public void onInventoryMove(InventoryMoveItemEvent event){
        Chunk sourceChunk;
        if(event.getSource().getHolder() instanceof BlockInventoryHolder){
            Block blockSource = ((BlockInventoryHolder)event.getSource().getHolder()).getBlock();
            sourceChunk = blockSource.getChunk();
            if(ClaimManager.PROTECTABLE.contains(blockSource.getType())){
                Claim claim = claimManager.getClaim(blockSource.getChunk());
                if(claim != null && claim.getProtectedContainer(CoordinatesUtils.convertCoordinates(blockSource.getLocation())) != null){
                    event.setCancelled(true);
                    return;
                }
            }
        } else if(event.getSource().getHolder() instanceof Entity){
            sourceChunk = ((Entity)event.getSource().getHolder()).getChunk();
        } else {
            return;
        }
        Chunk destinationChunk;
        if(event.getDestination().getHolder() instanceof BlockInventoryHolder){
            Block blockDestination = ((BlockInventoryHolder)event.getDestination().getHolder()).getBlock();
            destinationChunk = blockDestination.getChunk();
            if(ClaimManager.PROTECTABLE.contains(blockDestination.getType())){
                Claim claim = claimManager.getClaim(blockDestination.getChunk());
                if(claim != null && claim.getProtectedContainer(CoordinatesUtils.convertCoordinates(blockDestination.getLocation())) != null){
                    event.setCancelled(true);
                    return;
                }
            }
        } else if(event.getDestination().getHolder() instanceof Entity){
            destinationChunk = ((Entity)event.getDestination().getHolder()).getChunk();
        } else {
            return;
        }
        if(!Claim.canInteract(sourceChunk, destinationChunk)){
            event.setCancelled(true);
        }
    }
    
    //InventoryOpenEvent
    //InventoryPickupItemEvent
    //PrepareAnvilEvent
    //PrepareItemCraftEvent
    //TradeSelectEvent
    
    //AsyncPlayerChatEvent
    //AsyncPlayerPreLoginEvent
    //PlayerAdvancementDoneEvent
    //PlayerAnimationEvent
    
    @EventHandler
    public void onInteractArmorStand(PlayerArmorStandManipulateEvent event){
        Claim standClaim = claimManager.getClaim(event.getRightClicked().getChunk());
        if(standClaim != null && !standClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId()))){
            event.setCancelled(true);
        }
    }
    
    //PlayerAttemptPickupItemEvent
    //PlayerBedEnterEvent -> Déjà traitée par l'interaction avec le lit
    //PlayerBedLeaveEvent
    
    @EventHandler
    public void onEmptyBucket(PlayerBucketEmptyEvent event){
        if(event.getBucket() == Material.LAVA_BUCKET){
            List<Player> nearbyPlayers = (List<Player>)event.getBlockClicked().getLocation().getNearbyPlayers(3, 2);
            if(nearbyPlayers.size() > 1 ||
                    (nearbyPlayers.size() == 1 && !nearbyPlayers.get(0).getUniqueId().equals(event.getPlayer().getUniqueId()))){
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cUn autre joueur est à proximité.");
                return;
            }
        }
        onEmptyBucket((PlayerBucketEvent)event);
    }
    
    private void onEmptyBucket(PlayerBucketEvent event){
        Player player = event.getPlayer();
        Claim claim = claimManager.getClaim(event.getBlock().getChunk());
        if(claim != null && !claim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId()))){
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onEmptyBucket(PlayerBucketFillEvent event){
        onEmptyBucket((PlayerBucketEvent)event);
    }
    
    //PlayerChangedMainHandEvent
    //PlayerChangedWorldEvent
    //PlayerChannelEvent
    //PlayerCommandPreprocessEvent
    //PlayerCommandSendEvent
    //PlayerDropItemEvent
    //PlayerEditBookEvent
    //PlayerEggThrowEvent
    //PlayerExpChangeEvent
    //PlayerFishEvent
    //PlayerGameModeChangeEvent

    /*PlayerInteractAtEntityEvent
      PlayerInteractEntityEvent*/
    
    //PlayerInteractEvent
    //PlayerItemBreakEvent
    //PlayerItemConsumeEvent
    //PlayerItemDamageEvent
    //PlayerItemHeldEvent
    //PlayerItemMendEvent
    //PlayerJoinEvent
    //PlayerKickEvent
    //PlayerLevelChangeEvent
    //PlayerLocaleChangeEvent
    //PlayerLoginEvent
    //PlayerMoveEvent
    //PlayerPickupArrowEvent
    //PlayerPortalEvent
    //PlayerQuitEvent
    //PlayerRecipeDiscoverEvent
    //PlayerRegisterChannelEvent
    //PlayerResourcePackStatusEvent
    //PlayerRespawnEvent
    //PlayerRiptideEvent
    
    @EventHandler
    public void onShears(PlayerShearEntityEvent event){
        Claim entityShearedClaim = claimManager.getClaim(event.getEntity().getChunk());
        if(entityShearedClaim != null && !entityShearedClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId()))){
            event.setCancelled(true);
        }
    }
    
    //PlayerStatisticIncrementEvent
    //PlayerSwapHandItemsEvent
    
    @EventHandler
    public void onTakeBook(PlayerTakeLecternBookEvent event){
        Claim bookClaim = claimManager.getClaim(event.getLectern().getChunk());
        if(bookClaim != null && !bookClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId()))){
            event.setCancelled(true);
        }
    }
    
    //PlayerTeleportEvent
    //PlayerToggleFlightEvent
    //PlayerToggleSneakEvent
    //PlayerToggleSprintEvent
    //PlayerUnleashEntity
    //PlayerUnregisterChannelEvent
    //PlayerVelocityEvent
    
    //RaidEvent
    //RaidFinishEvent
    //RaidSpawnWaveEvent
    //RaidStopEvent
    
    @EventHandler
    public void onTriggerRaid(RaidTriggerEvent event){
        Claim centerOfRaidClaim = claimManager.getClaim(event.getRaid().getLocation().getChunk());
        if(centerOfRaidClaim != null && !centerOfRaidClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId()))){
            event.setCancelled(true);
        }
    }
    
    //VehicleBlockCollisionEvent
    //VehicleCollisionEvent
    //VehicleCreateEvent
    
    @EventHandler
    public void onVehicleDamaged(VehicleDamageEvent event){
        if(event.getAttacker() == null){
            return;
        }
        Claim claim = claimManager.getClaim(event.getVehicle().getLocation().getChunk());
        if(claim == null){
            return;
        }
        if(event.getAttacker() instanceof Player){
            if(!claim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getAttacker().getUniqueId()))){
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
        if(!claim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(
                ((Player)projectile.getShooter()).getUniqueId()))){
            event.setCancelled(true);
        }
    }
    
    //VehicleDestroyEvent
    
    @EventHandler
    public void onEnterVehicle(VehicleEnterEvent event){
        if(!(event.getEntered() instanceof Player)){
            return;
        }
        Claim vehicleClaim = claimManager.getClaim(event.getVehicle().getChunk());
        if(vehicleClaim != null && !vehicleClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getEntered().getUniqueId()))){
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onCollision(VehicleEntityCollisionEvent event){
        if(!(event.getEntity() instanceof Player)){
            return;
        }
        Claim collisionClaim = claimManager.getClaim(event.getVehicle().getChunk());
        if(collisionClaim != null && !collisionClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getEntity().getUniqueId()))){
            event.setCancelled(true);
            event.setCollisionCancelled(true);
        }
    }
    
    //VehicleEvent
    //VehicleExitEvent
    //VehicleMoveEvent
    //VehicleUpdateEvent
    
    @EventHandler
    public void onStructureGrow(StructureGrowEvent event){
        if(event.getBlocks().size() == 0){
            return;
        }
        Location source = event.getLocation();
        event.getBlocks().removeIf(blockState -> !Claim.canInteract(source.getChunk(), blockState.getChunk()));
    }
    
    @EventHandler
    public void onBell(PlayerInteractBellEvent event){
        onBlockInteract(event);
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onBlockInteract(PlayerInteractBlockEvent event){
        Claim blockClaim = claimManager.getClaim(event.getBlock().getChunk());
        if(blockClaim != null && !blockClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId()))){
            event.setCancelled(true);
        }
    }
    
    @EventHandler()
    public void onDoor(PlayerInteractDoorEvent event){
        Claim blockClaim = claimManager.getClaim(event.getBlock().getChunk());
        if(blockClaim != null && !blockClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId()),
                ClaimPermission.INTERACT_DOOR)){
            event.setCancelled(true);
        } else {
            event.setCancelled(false);
        }
    }
    
    @EventHandler()
    public void onFenceGate(PlayerInteractFenceGateEvent event){
        Claim blockClaim = claimManager.getClaim(event.getBlock().getChunk());
        if(blockClaim != null && !blockClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId()),
                ClaimPermission.INTERACT_DOOR)){
            event.setCancelled(true);
        } else {
            event.setCancelled(false);
        }
    }
    
    @EventHandler()
    public void onTrapdoor(PlayerInteractTrapdoorEvent event){
        Claim blockClaim = claimManager.getClaim(event.getBlock().getChunk());
        if(blockClaim != null && !blockClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId()),
                ClaimPermission.INTERACT_DOOR)){
            event.setCancelled(true);
        } else {
            event.setCancelled(false);
        }
    }
    
    @EventHandler()
    public void onOpenGui(PlayerInteractGuiBlockEvent event){
        event.setCancelled(false);
        switch(event.getType()){
            case ANVIL:
            case BEACON:
                Claim blockClaim = claimManager.getClaim(event.getBlock().getChunk());
                if(blockClaim != null && !blockClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId()),
                        ClaimPermission.OPEN_CONTAINER)){
                    event.setCancelled(true);
                }
                break;
        }
    }
    
    @EventHandler()
    public void onOpenContainer(PlayerInteractContainerBlockEvent event){
        event.setCancelled(false);
        Claim blockClaim = claimManager.getClaim(event.getBlock().getChunk());
        if(blockClaim == null){
            return;
        }
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        if(!blockClaim.canInteract(player, ClaimPermission.OPEN_CONTAINER)){
            if(!player.isInModeration() || player.getPlayer().getGameMode() != GameMode.SPECTATOR){
                event.setCancelled(true);
            }
        } else if(ClaimManager.PROTECTABLE.contains(event.getBlock().getType())){
            if(!(blockClaim.getOwner() instanceof City) || player.getUUID().equals(blockClaim.getOwnerUUID())){
                return;
            }
            UUID opener = event.getPlayer().getUniqueId();
            UUID owner = blockClaim.getProtectedContainer(CoordinatesUtils.convertCoordinates(event.getBlock().getLocation()));
            if(owner == null){
                return;
            }
            if(!owner.equals(opener)){
                event.getPlayer().sendActionBar("§cCe coffre est privé");
                event.setCancelled(true);
            }
        }
    }
    
    
    @EventHandler()
    public void onInteractLever(PlayerInteractLeverEvent event){
        Claim blockClaim = claimManager.getClaim(event.getBlock().getChunk());
        if(blockClaim != null && !blockClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId()),
                ClaimPermission.INTERACT_REDSTONE)){
            event.setCancelled(true);
        } else {
            event.setCancelled(false);
        }
    }
    
    @EventHandler()
    public void onInteractButton(PlayerInteractButtonEvent event){
        Claim blockClaim = claimManager.getClaim(event.getBlock().getChunk());
        if(blockClaim != null && !blockClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId()),
                ClaimPermission.INTERACT_REDSTONE)){
            event.setCancelled(true);
        } else {
            event.setCancelled(false);
        }
    }
    
    @EventHandler()
    public void onInteractPlate(PlayerInteractPlateEvent event){
        Claim blockClaim = claimManager.getClaim(event.getBlock().getChunk());
        if(blockClaim != null && !blockClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId()),
                ClaimPermission.INTERACT_REDSTONE)){
            event.setCancelled(true);
        } else {
            event.setCancelled(false);
        }
    }
    
    @EventHandler()
    public void onInteractTripwire(PlayerInteractTripwireEvent event){
        Claim blockClaim = claimManager.getClaim(event.getBlock().getChunk());
        if(blockClaim != null && !blockClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId()),
                ClaimPermission.INTERACT_REDSTONE)){
            event.setCancelled(true);
        } else {
            event.setCancelled(false);
        }
    }
    
    @EventHandler
    public void onLecter(PlayerInteractLecternEvent event){
        event.setCancelled(false);
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractWithEntityEvent event){
        Claim entityClaim = claimManager.getClaim(event.getEntity().getChunk());
        if(entityClaim != null && !entityClaim.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId()))){
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerPlaceItem(PlayerPlaceItemEvent event){
        Claim clicked = claimManager.getClaim(event.getClickedLocation().getChunk());
        if(clicked != null && !clicked.canInteract((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId()))){
            event.setCancelled(true);
        }
    }
}
