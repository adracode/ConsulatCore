package fr.amisoz.consulatcore.shop;

import com.destroystokyo.paper.Namespaced;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.commands.economy.ShopCommand;
import fr.amisoz.consulatcore.players.SPlayerManager;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class ShopManager implements Listener {
    
    private static ShopManager instance;
    
    private Map<Long, Shop> shops = new HashMap<>();
    
    public ShopManager(){
        if(instance != null){
            return;
        }
        instance = this;
        //Volontairement bloquant
        try {
            initShops();
        } catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    private void initShops() throws SQLException{
        PreparedStatement shops = ConsulatAPI.getDatabase().prepareStatement("SELECT * FROM shopinfo");
        ResultSet resultShops = shops.executeQuery();
        World world = Bukkit.getWorlds().get(0);
        while(resultShops.next()){
            Location location = new Location(
                    Bukkit.getWorlds().get(0),
                    resultShops.getInt("shop_x"),
                    resultShops.getInt("shop_y"),
                    resultShops.getInt("shop_z")
            );
            String stringUUID = resultShops.getString("owner_uuid");
            if(stringUUID == null){
                ConsulatAPI.getConsulatAPI().log(Level.WARNING, "Player UUID id null at " + location + " in shopinfo table");
                continue;
            }
            UUID uuid = UUID.fromString(stringUUID);
            Block block = world.getBlockAt(location);
            if(!(block.getState() instanceof Chest)){
                if(block.getState() instanceof Sign){
                    Chest chest = getChestFromSign(block);
                    if(chest == null){
                        ConsulatAPI.getConsulatAPI().log(Level.SEVERE, "Le shop en " + location + " n'est pas valide (pas un panneau), il sera supprimé.");
                        ConsulatAPI.getConsulatAPI().logFile("Le shop en " + location + " n'est pas valide, il a été supprimé, owner: " + uuid + ", item vendu: " + resultShops.getString("material"));
                        removeShopDatabase(uuid, location.getBlockX(), location.getBlockY(), location.getBlockZ());
                        continue;
                    }
                    block = getChestFromSign(block).getBlock();
                    Location old = location;
                    location = block.getLocation();
                    if(isDoubleChest(chest)){
                        setChestsSingle(chest.getBlock(), getNextChest(chest.getBlock()));
                    }
                    updateShop(old, location);
                } else {
                    ConsulatAPI.getConsulatAPI().log(Level.SEVERE, "Le shop en " + location + " n'est pas valide, il sera supprimé.");
                    ConsulatAPI.getConsulatAPI().logFile("Le shop en " + location + " n'est pas valide, il a été supprimé, owner: " + uuid + ", item vendu: " + resultShops.getString("material"));
                    removeShopDatabase(uuid, location.getBlockX(), location.getBlockY(), location.getBlockZ());
                    continue;
                }
            }
            ItemFrame itemFrame = Shop.getItemFrame(block.getLocation());
            ItemStack item;
            String stringMaterial = resultShops.getString("material");
            if(stringMaterial == null){
                ConsulatAPI.getConsulatAPI().log(Level.WARNING, "Material id null at " + location + " in shopinfo table");
                continue;
            }
            Material type = Material.valueOf(stringMaterial);
            if(itemFrame != null){
                item = itemFrame.getItem();
                if(item.getType() == Material.AIR){
                    item = new ItemStack(type);
                    itemFrame.setItem(item);
                }
                if(itemFrame.getFacing() != BlockFace.UP){
                    itemFrame.setFacingDirection(BlockFace.UP);
                }
            } else {
                ConsulatAPI.getConsulatAPI().log(Level.WARNING, "Missing item frame for shop " + location + " of " + Bukkit.getOfflinePlayer(uuid).getName());
                item = getFirstItem((Chest)block.getState());
                if(item != null){
                    item.setItemMeta(null);
                } else {
                    item = new ItemStack(type);
                }
                Collection<Entity> entities = location.clone().add(0.5, 1.5, 0.5).getNearbyEntities(0.5, 0.5, 0.5);
                ItemFrame frame = null;
                for(Entity entity : entities){
                    if(entity.getType() == EntityType.ITEM_FRAME){
                        frame = (ItemFrame)entity;
                        if(frame.getFacing() == BlockFace.UP){
                            break;
                        }
                    }
                }
                if(frame != null){
                    frame.setFacingDirection(BlockFace.UP);
                    frame.setItem(item);
                    frame.setInvulnerable(true);
                }
                itemFrame = frame;
            }
            if(item.getType() != type){
                ConsulatAPI.getConsulatAPI().log(Level.SEVERE, "Le shop en " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ() + " est censé" +
                        " avoir un item " + type + " mais à un item de type " + item.getType());
            }
            Shop shop = new Shop(
                    uuid,
                    Bukkit.getOfflinePlayer(uuid).getName(),
                    item,
                    resultShops.getDouble("price"),
                    location,
                    itemFrame == null
            );
            this.shops.put(shop.getCoords(), shop);
            if(itemFrame == null){
                if(!shop.placeItemFrame()){
                    if(shop.getSign() != null){
                        shop.getSign().getBlock().breakNaturally();
                    }
                    removeShop(shop);
                }
            }
        }
    }
    
    public void addShop(SurvivalPlayer player, Shop shop) throws SQLException{
        addShopDatabase(player.getUUID(), shop);
        player.addShop(shop);
        shops.put(shop.getCoords(), shop);
    }
    
    public boolean isShop(Chest chest){
        return getShop(chest.getLocation()) != null;
    }
    
    public void tutorial(SurvivalPlayer player){
        player.sendMessage(Text.PREFIX + "Comment créer un shop:");
        player.sendMessage(Text.PREFIX + "Mets l'item à vendre dans un coffre, place un panneau dessus, et écris ceci sur ton panneau");
        player.sendMessage(Text.PREFIX + " ");
        player.sendMessage(ChatColor.YELLOW + "                   [ConsulShop]");
        player.sendMessage(ChatColor.YELLOW + "     prix à l'unité (une virgule est un point)");
        player.sendMessage(ChatColor.YELLOW + "                       VIDE");
        player.sendMessage(ChatColor.YELLOW + "                       VIDE");
    }
    
    private boolean isChestEmpty(Chest chest){
        for(ItemStack item : chest.getBlockInventory().getContents()){
            if(item != null){
                return false;
            }
        }
        return true;
    }
    
    public boolean isDoubleChest(Chest chest){
        return ((org.bukkit.block.data.type.Chest)chest.getBlock().getBlockData()).getType() != org.bukkit.block.data.type.Chest.Type.SINGLE;
    }
    
    @EventHandler
    public void onShopCreated(SignChangeEvent event){
        if(!event.getLines()[0].equalsIgnoreCase("[ConsulShop]")){
            return;
        }
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        if(event.getBlock().getType() != Material.OAK_WALL_SIGN){
            player.sendMessage("§cUtilise un panneau en bois de chêne.");
            event.getBlock().breakNaturally();
            return;
        }
        Chest chest = getChestFromSign(event.getBlock());
        if(chest == null){
            event.getBlock().breakNaturally();
            player.sendMessage("§cUn shop ne peut être que sur un coffre.");
            return;
        }
        if(isDoubleChest(chest)){
            event.getBlock().breakNaturally();
            player.sendMessage("§cUn shop ne peut être que sur un coffre simple.");
            return;
        }
        if(isShop(chest)){
            event.getBlock().breakNaturally();
            player.sendMessage("§cCe coffre est déjà un shop.");
            return;
        }
        if(Bukkit.getWorlds().get(0) != player.getPlayer().getWorld()){
            event.getBlock().breakNaturally();
            player.sendMessage("§cUn shop ne peut être crée que dans l'overworld.");
            return;
        }
        if(!player.canAddNewShop()){
            player.sendMessage(Text.PREFIX + "§cVous avez atteint votre limite de shops.");
            event.getBlock().breakNaturally();
            return;
        }
        if(isChestEmpty(chest)){
            player.sendMessage("§cLe shop ne doit pas être vide pour être crée.");
            event.getBlock().breakNaturally();
            return;
        }
        String[] lines = event.getLines();
        if(lines[2].length() > 0 || lines[3].length() > 0){
            tutorial(player);
            event.getBlock().breakNaturally();
            return;
        }
        double price;
        try {
            price = Double.parseDouble(lines[1]);
        } catch(NumberFormatException e){
            event.getBlock().breakNaturally();
            player.sendMessage("§cLe prix est incorrecte.");
            return;
        }
        if(price <= 0 || Double.isInfinite(price)){
            event.getBlock().breakNaturally();
            player.sendMessage("§cLe prix est incorrecte.");
            return;
        }
        ItemStack sold = null;
        for(ItemStack item : chest.getBlockInventory().getContents()){
            if(item != null){
                if(sold == null){
                    if(item.getType() == Material.ENCHANTED_BOOK && ((EnchantmentStorageMeta)item.getItemMeta()).getStoredEnchants().size() != 1){
                        player.sendMessage("§cUn livre ne peut être vendu qu'avec un seul enchantement.");
                        event.getBlock().breakNaturally();
                        return;
                    }
                    sold = item;
                } else {
                    if(item.getType() != sold.getType() || !item.getItemMeta().equals(sold.getItemMeta())){
                        player.sendMessage("§cLes items dans ton shop doivent être identiques.");
                        event.getBlock().breakNaturally();
                        return;
                    }
                }
            }
        }
        if(sold == null){
            player.sendMessage("§cLe shop ne doit pas être vide pour être crée.");
            event.getBlock().breakNaturally();
            return;
        }
        Shop shop = new Shop(
                player.getUUID(),
                player.getName(),
                sold,
                price,
                chest.getLocation(),
                false
        );
        if(!shop.placeItemFrame()){
            player.sendMessage("§cMerci de retirer le cadre au dessus du coffre.");
            event.getBlock().breakNaturally();
            return;
        }
        event.setLine(0, "§8[§aConsulShop§8]");
        event.setLine(1, String.valueOf(price));
        if(sold.getType() == Material.ENCHANTED_BOOK){
            Map.Entry<Enchantment, Integer> enchantment = ((EnchantmentStorageMeta)sold.getItemMeta()).getStoredEnchants().entrySet().iterator().next();
            String name = enchantment.getKey().getKey().getKey();
            event.setLine(2, name.substring(0, Math.min(10, name.length())) + " " + enchantment.getValue());
        } else {
            event.setLine(2, sold.getType().toString());
        }
        event.setLine(3, player.getName());
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                addShop(player, shop);
            } catch(SQLException e){
                player.sendMessage("§cUne erreur interne est survenue");
                Bukkit.getScheduler().scheduleSyncDelayedTask(ConsulatCore.getInstance(), () -> {
                    event.getBlock().breakNaturally();
                });
                e.printStackTrace();
            }
        });
        player.sendMessage("§aTon shop a bien été crée!");
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(PlayerInteractEntityEvent event){
        if(event.getRightClicked().getType() == EntityType.ITEM_FRAME){
            Entity frame = event.getRightClicked();
            Location location = frame.getLocation().clone().add(0, -1, 0);
            if(frame.isInvulnerable() && getShop(location) == null){
                frame.remove();
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onShopBreak(BlockBreakEvent event){
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        switch(event.getBlock().getType()){
            case CHEST:{
                Shop shop = getShop(event.getBlock().getLocation());
                if(shop == null){
                    return;
                }
                if(player.getUUID().equals(shop.getOwner())){
                    event.setCancelled(true);
                    player.sendMessage(Text.PREFIX + "§cTu dois casser le panneau pour supprimer ton Shop!");
                } else {
                    event.setCancelled(true);
                    player.sendMessage(Text.PREFIX + "§cCe shop appartient à: §4" + shop.getOwnerName() + "§c.");
                }
                break;
            }
            case OAK_WALL_SIGN:{
                Sign sign = (Sign)event.getBlock().getState();
                String[] lines = sign.getLines();
                if(!lines[0].contains("§8[§aConsulShop§8]")){
                    return;
                }
                Chest chest = getChestFromSign(event.getBlock());
                if(chest == null){
                    return;
                }
                Shop shop = getShop(chest.getLocation());
                if(shop == null){
                    return;
                }
                if(player.getUUID().equals(shop.getOwner()) || player.hasPower(Rank.RESPONSABLE) || player.getRank() == Rank.DEVELOPPEUR){
                    Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                        try {
                            removeShop(shop);
                            player.sendMessage(Text.PREFIX + "Tu viens de détruire un de tes shops!");
                        } catch(SQLException e){
                            player.sendMessage("§cUne erreur interne est survenue");
                            e.printStackTrace();
                        }
                    });
                } else {
                    event.setCancelled(true);
                    player.sendMessage(Text.PREFIX + "Tu ne peux pas casser ce shop !");
                }
                break;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBuyingPlayerShop(PlayerInteractEvent event){
        if(event.getHand() == EquipmentSlot.OFF_HAND){
            return;
        }
        if(event.getClickedBlock() == null){
            return;
        }
        if(event.getClickedBlock().getType() != Material.OAK_WALL_SIGN){
            return;
        }
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK){
            return;
        }
        Sign sign = (Sign)event.getClickedBlock().getState();
        String[] lines = sign.getLines();
        if(!lines[0].contains("§8[§aConsulShop§8]")){
            return;
        }
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        Block block = event.getClickedBlock();
        Chest chest = getChestFromSign(block);
        if(chest == null){
            return;
        }
        Shop shop = getShop(chest.getLocation());
        if(shop == null){
            return;
        }
        event.setCancelled(true);
        if((player.getRank() == Rank.ADMIN || player.getRank() == Rank.DEVELOPPEUR) && player.getPlayer().getInventory().getItemInMainHand().getType() == Material.HEART_OF_THE_SEA){
            ItemMeta meta = player.getPlayer().getInventory().getItemInMainHand().getItemMeta();
            if(meta != null && meta.getDisplayName().equals("Debug tool")){
                player.sendMessage("Shop x = " + shop.getX() + " y = " + shop.getY() + " z = " + shop.getZ());
                player.sendMessage("ItemFrame: " + shop.getItemFrame().getLocation() + ", Facing: " + shop.getItemFrame().getFacing());
                player.sendMessage("Restant: " + shop.getAmount());
                player.sendMessage("Item: " + shop.getItem());
                return;
            }
        }
        if(shop.isEmpty()){
            player.sendMessage(Text.PREFIX + "§cCe shop est actuellement vide.");
            return;
        }
        if(shop.isOpen()){
            player.sendMessage(Text.PREFIX + "§cCe shop n'est pas disponible.");
            return;
        }
        if(shop.getOwner().equals(player.getUUID())){
            if(!ConsulatAPI.getConsulatAPI().isDebug()){
                player.sendMessage(Text.PREFIX + "§cTu ne peux pas acheter à ton propre shop!");
                return;
            }
        }
        int placeAvailable = player.spaceAvailable(shop.getItem());
        if(placeAvailable <= 0){
            player.sendMessage("§cVous n'avez pas assez de place dans votre inventaire.");
            return;
        }
        int amount = Integer.min(shop.getAmount(!player.getPlayer().isSneaking() ? 1 : 64), placeAvailable);
        double price = amount * shop.getPrice();
        if(!player.hasMoney(price)){
            player.sendMessage(Text.PREFIX + "§cTu n'as pas assez d'argent");
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                player.removeMoney(shop.getPrice());
            } catch(SQLException e){
                player.sendMessage("§cUne erreur interne est survenue, la transaction a échoué.");
                e.printStackTrace();
            }
            SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(shop.getOwner());
            try {
                if(target == null){
                    SPlayerManager.getInstance().addMoney(shop.getOwner(), shop.getPrice());
                } else {
                    target.addMoney(shop.getPrice());
                    target.sendMessage(Text.PREFIX + "§aTu as reçu " + price + " € grâce à un de tes shops.");
                }
            } catch(SQLException e){
                player.sendMessage("§cUne erreur interne est survenue, la transaction a échoué");
                e.printStackTrace();
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(ConsulatCore.getInstance(), () -> {
                shop.buy(amount);
                player.addItemInInventory(amount, shop.getItem());
            });
        });
        player.sendMessage(Text.PREFIX + "Tu as acheté §e" + shop.getItemType().toString() + " x " + amount + " §6pour §e" + shop.getPrice() * amount);
        ConsulatAPI.getConsulatAPI().logFile("Achat: " + player + " a acheté au shop " + shop + " " + amount + " items pour un prix de " + price);
    }
    
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event){
        cancelExplosion(event.blockList());
    }
    
    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event){
        cancelExplosion(event.blockList());
    }
    
    @EventHandler
    public void onItemFramePop(HangingBreakEvent event){
        if(event.getEntity().getType() == EntityType.ITEM_FRAME){
            if(event.getEntity().isInvulnerable()){
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onItemFrameDamage(EntityDamageEvent event){
        if(event.getEntity().getType() == EntityType.ITEM_FRAME){
            if(event.getEntity().isInvulnerable()){
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChestClick(PlayerInteractEvent event){
        if(event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.CHEST){
            return;
        }
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK){
            return;
        }
        Shop shop = getShop(event.getClickedBlock().getLocation());
        if(shop == null){
            return;
        }
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        if(!shop.isOwner(player.getUUID())){
            if(player.hasPower(Rank.ADMIN)){
                player.sendMessage(Text.PREFIX + "§cCe shop appartient à: §4" + shop.getOwnerName() + "§c.");
            } else {
                player.sendMessage(Text.PREFIX + "§cTu ne peux pas ouvrir ce shop, il appartient à " + shop.getOwnerName() + "§c.");
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onShopOpen(InventoryOpenEvent event){
        Block chest = null;
        if(event.getInventory().getHolder() != null && event.getInventory().getHolder() instanceof Chest){
            chest = ((Chest)event.getInventory().getHolder()).getBlock();
        }
        if(chest == null){
            return;
        }
        Shop shop = getShop(chest.getLocation());
        if(shop == null){
            return;
        }
        shop.setOpen(true);
    }
    
    @EventHandler
    public void onCloseInventory(InventoryCloseEvent event){
        Block chest = null;
        if(event.getInventory().getHolder() != null && event.getInventory().getHolder() instanceof Chest){
            chest = ((Chest)event.getInventory().getHolder()).getBlock();
        }
        if(chest == null){
            return;
        }
        Shop shop = getShop(chest.getLocation());
        if(shop == null){
            return;
        }
        shop.setOpen(false);
    }
    
    @EventHandler
    public void onClickShop(InventoryClickEvent event){
        Block chest = null;
        if(event.getInventory().getHolder() != null && event.getInventory().getHolder() instanceof Chest){
            chest = ((Chest)event.getInventory().getHolder()).getBlock();
        }
        if(chest == null){
            return;
        }
        Shop shop = getShop(chest.getLocation());
        if(shop == null){
            return;
        }
        if(!shop.isItemAccepted(event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR ?
                event.getCursor() : event.getCurrentItem())){
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlaceChestNextToShop(BlockPlaceEvent event){
        if(event.getBlock().getType() != Material.CHEST){
            return;
        }
        Block chest = event.getBlock();
        org.bukkit.block.data.type.Chest chestData = (org.bukkit.block.data.type.Chest)chest.getBlockData();
        if(chestData.getType() == org.bukkit.block.data.type.Chest.Type.SINGLE){
            return;
        }
        Block otherChest = getNextChest(chest);
        if(!isShop((Chest)otherChest.getState())){
            return;
        }
        setChestsSingle(chest, otherChest);
        event.getPlayer().sendBlockChange(otherChest.getLocation(), otherChest.getBlockData());
    }
    
    public void setChestsSingle(Block chest, Block otherChest){
        org.bukkit.block.data.type.Chest chestData = (org.bukkit.block.data.type.Chest)chest.getBlockData();
        org.bukkit.block.data.type.Chest otherChestData = ((org.bukkit.block.data.type.Chest)otherChest.getBlockData());
        chestData.setType(org.bukkit.block.data.type.Chest.Type.SINGLE);
        otherChestData.setType(org.bukkit.block.data.type.Chest.Type.SINGLE);
        chest.setBlockData(chestData);
        otherChest.setBlockData(otherChestData);
    }
    
    @EventHandler
    public void onItemMove(InventoryMoveItemEvent event){
        Block chest = null;
        if(event.getSource().getHolder() != null && event.getSource().getHolder() instanceof Chest){
            chest = ((Chest)event.getSource().getHolder()).getBlock();
        } else if(event.getDestination().getHolder() != null && event.getDestination().getHolder() instanceof Chest){
            chest = ((Chest)event.getDestination().getHolder()).getBlock();
        }
        if(chest == null){
            return;
        }
        if(isShop((Chest)chest.getState())){
            event.setCancelled(true);
        }
    }
    
    private Block getNextChest(Block chest){
        return chest.getRelative(getNextChest((org.bukkit.block.data.type.Chest)chest.getBlockData()));
    }
    
    private BlockFace getNextChest(org.bukkit.block.data.type.Chest side){
        switch(side.getFacing()){
            case NORTH:
                return side.getType() != org.bukkit.block.data.type.Chest.Type.RIGHT ? BlockFace.EAST : BlockFace.WEST;
            case SOUTH:
                return side.getType() != org.bukkit.block.data.type.Chest.Type.RIGHT ? BlockFace.WEST : BlockFace.EAST;
            case EAST:
                return side.getType() != org.bukkit.block.data.type.Chest.Type.RIGHT ? BlockFace.SOUTH : BlockFace.NORTH;
            case WEST:
                return side.getType() != org.bukkit.block.data.type.Chest.Type.RIGHT ? BlockFace.NORTH : BlockFace.SOUTH;
        }
        return BlockFace.SELF;
    }
    
    private void cancelExplosion(List<Block> blocks){
        for(Iterator<Block> iterator = blocks.iterator(); iterator.hasNext(); ){
            Block block = iterator.next();
            switch(block.getType()){
                case CHEST:
                    if(isShop((Chest)block.getState())){
                        iterator.remove();
                    }
                    break;
                case OAK_WALL_SIGN:
                    if(((Sign)block.getState()).getLine(0).equals("§8[§aConsulShop§8]")){
                        iterator.remove();
                    }
                    break;
            }
        }
    }
    
    public void removeShop(Shop shop) throws SQLException{
        removeShopDatabase(shop.getOwner(), shop.getX(), shop.getY(), shop.getZ());
        this.shops.remove(shop.getCoords());
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(shop.getOwner());
        if(player != null){
            player.removeShop(shop);
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(ConsulatCore.getInstance(), shop::destroyFrame);
    }
    
    public Chest getChestFromSign(Block sign){
        if(!(sign.getState() instanceof Sign)){
            return null;
        }
        Block attachedBlock = sign.getRelative(((Directional)sign.getBlockData()).getFacing().getOppositeFace());
        return attachedBlock.getType() == Material.CHEST && (attachedBlock.getState() instanceof Chest) ? (Chest)attachedBlock.getState() : null;
    }
    
    private ItemStack getFirstItem(Chest chest){
        for(ItemStack item : chest.getBlockInventory()){
            if(item != null){
                return item;
            }
        }
        return null;
    }
    
    public static ShopManager getInstance(){
        return instance;
    }
    
    public Shop getShop(Location location){
        return shops.get(Shop.convertCoordinates(location));
    }
    
    public Collection<Shop> getShops(){
        return shops.values();
    }
    
    public List<Shop> getShops(UUID uuid){
        List<Shop> playerShops = new ArrayList<>(16);
        for(Shop shop : shops.values()){
            if(shop.getOwner().equals(uuid)){
                playerShops.add(shop);
            }
        }
        return playerShops;
    }
    
    public void addShopDatabase(UUID uuid, Shop shop) throws SQLException{
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("INSERT INTO shopinfo (shop_x, shop_y, shop_z, material, price, owner_uuid, isEmpty) VALUES (?, ?, ?, ?, ?, ?, ?)");
        preparedStatement.setInt(1, shop.getX());
        preparedStatement.setInt(2, shop.getY());
        preparedStatement.setInt(3, shop.getZ());
        preparedStatement.setString(4, shop.getItemType().toString());
        preparedStatement.setDouble(5, shop.getPrice());
        preparedStatement.setString(6, uuid.toString());
        preparedStatement.setBoolean(7, false);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
    
    public void removeShopDatabase(UUID uuId, int x, int y, int z) throws SQLException{
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("DELETE FROM shopinfo WHERE owner_uuid = ? AND shop_x = ? AND shop_y = ? AND shop_z = ?");
        preparedStatement.setString(1, uuId.toString());
        preparedStatement.setInt(2, x);
        preparedStatement.setInt(3, y);
        preparedStatement.setInt(4, z);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
    
    //Pas encore modifié
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        Player player = (Player)event.getWhoClicked();
        InventoryView view = event.getView();
        String inventoryName = view.getTitle();
        
        if(inventoryName.equalsIgnoreCase(ChatColor.RED + "Liste des Shops")){
            ItemStack item = event.getCurrentItem();
            if(item == null) return;
            
            ItemMeta meta = item.getItemMeta();
            if(meta == null) return;
            
            List<String> lores = meta.getLore();
            
            String location = null;
            Location teleportLocation = null;
            
            event.setCancelled(true);
            if(item.getType() != Material.PAPER && item.getType() != Material.ARROW){
                if(lores != null){
                    for(String lore : lores){
                        if(lore.contains("Coordonnées")){
                            location = lore;
                            break;
                        }
                    }
                    if(location == null){
                        return;
                    }
                } else {
                    return;
                }
                if(location != null){
                    String[] coordonnees = ChatColor.stripColor(location.replaceAll(" ", "")).split(":");
                    int x = Integer.parseInt(ChatColor.stripColor(coordonnees[2].replace("Y", "")));
                    int y = Integer.parseInt(ChatColor.stripColor(coordonnees[3].replace("Z", "")));
                    int z = Integer.parseInt(ChatColor.stripColor(coordonnees[4]));
                    teleportLocation = new Location(Bukkit.getWorlds().get(0), x, y, z);
                }
                SurvivalPlayer survivalPlayer = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId());
                if(survivalPlayer.hasMoney(10.0)){
                    try {
                        Shop shop = ShopManager.getInstance().getShop(teleportLocation);
                        if(shop != null){
                            Sign sign = shop.getSign();
                            System.out.println(sign);
                            if(sign == null){
                                player.teleport(teleportLocation.clone().add(0, 1, 0));
                            } else {
                                Location block = sign.getLocation().clone().add(0.5, 0, 0.5);
                                if(block.getBlock().getRelative(BlockFace.UP).getType() != Material.AIR){
                                    block.add(0, -1, 0);
                                }
                                player.teleport(block);
                            }
                        } else {
                            player.teleport(teleportLocation.clone().add(0, 1, 0));
                        }
                    } catch(NullPointerException e){
                        player.sendMessage("Erreur lors de la téléportation");
                        return;
                    }
                    player.sendMessage(ChatColor.YELLOW + "Téléportation réussie pour " + ChatColor.RED + "10.0" + ChatColor.YELLOW + "€.");
                    Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                        try {
                            survivalPlayer.removeMoney(10.0);
                        } catch(SQLException e){
                            e.printStackTrace();
                        }
                    });
                }
            } else {
                if(item.getType() == Material.ARROW){
                    if(meta.getDisplayName().contains(ChatColor.stripColor("Page précédente"))){
                        if(getShoplistId(player) >= 1){
                            Inventory inventory = ShopCommand.createShoplistInventory(player, getShoplistId(player) - 1);
                            if(inventory != null){
                                player.openInventory(inventory);
                            }
                        }
                    }
                    
                    if(meta.getDisplayName().contains(ChatColor.stripColor("Page suivante"))){
                        Inventory inventory = ShopCommand.createShoplistInventory(player, getShoplistId(player) + 1);
                        if(inventory != null){
                            player.openInventory(inventory);
                        }
                    }
                }
            }
            
        }
    }
    
    //Pas encore modifié
    public static int getShoplistId(Player player){
        InventoryView inv = player.getOpenInventory();
        if(inv.getTitle().equalsIgnoreCase(ChatColor.RED + "Liste des Shops")){
            ItemStack item = inv.getItem(49);
            ItemMeta meta = Objects.requireNonNull(item).getItemMeta();
            int id = Integer.parseInt(ChatColor.stripColor(Objects.requireNonNull(meta).getDisplayName().replace("Page: ", "")));
            return id;
        }
        return 1;
    }
    
    @EventHandler
    public void onSign(SignChangeEvent event){
        Player player = event.getPlayer();
        if(player.isOp()){
            if(event.getLines()[0].equals("[ConsulatShop]")){
                event.setLine(0, "§8[§aConsulatShop§8]");
            }
            if(event.getLines()[2].equals("X")){
                event.setLine(2, "§cAchat impossible");
            }
            if(event.getLines()[3].equals("X")){
                event.setLine(3, "§cVente impossible");
            }
        }
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        if(event.getHand() != EquipmentSlot.HAND){
            return;
        }
        if(event.getClickedBlock() == null){
            return;
        }
        if(event.getClickedBlock().getType() != Material.OAK_WALL_SIGN){
            return;
        }
        Sign sign = (Sign)event.getClickedBlock().getState();
        String[] lines = sign.getLines();
        if(!lines[0].equals("§8[§aConsulatShop§8]")){
            return;
        }
        if(event.getAction() == Action.LEFT_CLICK_BLOCK){
            SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
            event.setCancelled(true);
            boolean hasBuyedOnce = player.getLimitHome() >= 1;
            if(sign.getLines()[2].contains("Achat impossible")){
                player.sendMessage(Text.PREFIX + "Item non disponible à l'achat.");
                return;
            }
            double buyPrice = Double.parseDouble(sign.getLines()[2]);
            if(sign.getLines()[1].equalsIgnoreCase("home")){
                if(player.hasMoney(buyPrice)){
                    if(hasBuyedOnce){
                        player.sendMessage(Text.PREFIX + "Tu as déjà acheté un home supplémentaire!");
                    } else {
                        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                            try {
                                player.removeMoney(buyPrice);
                                player.incrementLimitHome();
                                player.sendMessage(Text.PREFIX + "Tu as acheté un home supplémentaire pour " + buyPrice + "§.");
                            } catch(SQLException e){
                                e.printStackTrace();
                                player.sendMessage(Text.PREFIX + "Il y a eu une erreur durant l'achat de votre home!");
                            }
                        });
                    }
                } else {
                    player.sendMessage(Text.PREFIX + "§cTu n'as pas assez d'argent");
                }
                return;
            } else if(sign.getLines()[1].equalsIgnoreCase("TOURISTE_GRADE")){
                if(player.hasMoney(buyPrice)){
                    if(player.getRank() != Rank.JOUEUR){
                        player.sendMessage(Text.PREFIX + "Tu ne peux pas acheter ce grade !");
                        return;
                    }
                    Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                        try {
                            player.removeMoney(buyPrice);
                            player.setRank(Rank.TOURISTE);
                            player.sendMessage(Text.PREFIX + "Tu es désormais touriste !");
                        } catch(SQLException e){
                            e.printStackTrace();
                            player.sendMessage(Text.PREFIX + "Il y a eu une erreur durant l'achat de votre grade !");
                        }
                    });
                } else {
                    player.sendMessage(Text.PREFIX + "§cTu n'as pas assez d'argent");
                }
                return;
            } else if(sign.getLines()[1].equalsIgnoreCase("FLY_5")){
                if(player.hasMoney(buyPrice)){
                    if(!player.hasFly()){
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "boutique fly5 " + player.getName());
                    } else {
                        player.sendMessage(ChatColor.RED + "Tu as déjà le fly.");
                    }
                } else {
                    player.sendMessage(Text.PREFIX + "§cTu n'as pas assez d'argent");
                }
            } else {
                Material material;
                byte data = 0;
                if(getShopMaterial(sign.getLines()[1]) != null){
                    ShopEnum shop = getShopMaterial(sign.getLines()[1]);
                    material = Material.getMaterial(shop.getOldMaterialName());
                    data = shop.getMetadata();
                } else {
                    material = Material.getMaterial(sign.getLines()[1]);
                }
                if(material == null){
                    return;
                }
                ItemStack itemStack = new ItemStack(material, 1, data);
                int spaceAvailable = player.spaceAvailable(itemStack);
                if(player.getPlayer().isSneaking()){
                    if(spaceAvailable < 64){
                        player.sendMessage(Text.PREFIX + "§cVous n'avez pas assez de place dans votre inventaire");
                        return;
                    }
                    if(player.hasMoney(buyPrice * 64)){
                        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                            try {
                                player.removeMoney(buyPrice * 64);
                                Bukkit.getScheduler().scheduleSyncDelayedTask(ConsulatCore.getInstance(), () -> {
                                    itemStack.setAmount(64);
                                    player.getPlayer().getInventory().addItem(itemStack);
                                    player.sendMessage(Text.PREFIX + "Tu as acheté §e" + material.name() + " x 64 §6pour §e" + buyPrice * 64);
                                });
                            } catch(SQLException e){
                                player.sendMessage(Text.PREFIX + "§cUne erreur est survenue");
                                e.printStackTrace();
                            }
                        });
                    } else {
                        player.sendMessage(Text.PREFIX + "§cTu n'as pas assez d'argent");
                    }
                } else {
                    if(spaceAvailable == 0){
                        player.sendMessage(Text.PREFIX + "§cVous n'avez pas assez de place dans votre inventaire");
                        return;
                    }
                    if(player.hasMoney(buyPrice)){
                        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                            try {
                                player.removeMoney(buyPrice);
                                player.sendMessage(Text.PREFIX + "Tu as acheté §e" + material.name() + " x 1 §6pour §e" + buyPrice);
                                Bukkit.getScheduler().scheduleSyncDelayedTask(ConsulatCore.getInstance(), () -> {
                                    player.getPlayer().getInventory().addItem(itemStack);
                                });
                            } catch(SQLException e){
                                player.sendMessage(Text.PREFIX + "§cUne erreur est survenue");
                                e.printStackTrace();
                            }
                        });
                    } else {
                        player.sendMessage(Text.PREFIX + "§cTu n'as pas assez d'argent");
                    }
                }
            }
        }
        
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
            SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
            event.setCancelled(true);
            if(sign.getLines()[3].contains("Vente impossible")){
                player.sendMessage(Text.PREFIX + "Item non disponible à la vente.");
                return;
            }
            Material material;
            byte data = 0;
            if(getShopMaterial(sign.getLines()[1]) != null){
                ShopEnum shop = getShopMaterial(sign.getLines()[1]);
                material = Material.getMaterial(shop.getOldMaterialName());
                data = shop.getMetadata();
            } else {
                material = Material.getMaterial(sign.getLines()[1]);
            }
            if(material == null){
                return;
            }
            ItemStack itemStack = new ItemStack(material, 1, data);
            double price = Double.parseDouble(sign.getLines()[3]);
            if(player.getRank() == Rank.TOURISTE){
                price *= 1.12;
            } else if(player.getRank() == Rank.FINANCEUR){
                price *= 1.15;
            } else if(player.hasPower(Rank.MECENE)){
                price *= 1.20;
            }
            final double sellPrice = price;
            if(sign.getLines()[3].contains("Vente impossible")){
                player.sendMessage(Text.PREFIX + "Item non disponible à la vente.");
                return;
            }
            ItemStack itemInHand = player.getPlayer().getInventory().getItemInMainHand();
            if(player.getPlayer().isSneaking()){
                if(itemInHand.getType() == material){
                    if(material == Material.EGG){
                        if(itemInHand.getAmount() == 16){
                            itemStack.setAmount(16);
                            ItemStack save = player.getPlayer().getInventory().getItemInMainHand();
                            player.getPlayer().getInventory().clear(player.getPlayer().getInventory().getHeldItemSlot());
                            player.getPlayer().updateInventory();
                            Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                                try {
                                    player.addMoney(sellPrice * 16);
                                    player.sendMessage(Text.PREFIX + "Tu as vendu §e" + material.name() + " x 16 §6pour §e" + sellPrice * 16);
                                } catch(SQLException e){
                                    player.sendMessage(Text.PREFIX + "§cUne erreur interne est survenue");
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(ConsulatCore.getInstance(), () -> {
                                        player.getPlayer().getInventory().addItem(save);
                                    });
                                    e.printStackTrace();
                                }
                            });
                        }
                    } else {
                        if(itemInHand.getAmount() == 64){
                            itemStack.setAmount(64);
                            ItemStack save = player.getPlayer().getInventory().getItemInMainHand();
                            player.getPlayer().getInventory().clear(player.getPlayer().getInventory().getHeldItemSlot());
                            player.getPlayer().updateInventory();
                            Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                                try {
                                    player.addMoney(sellPrice * 64);
                                    player.sendMessage(Text.PREFIX + "Tu as vendu §e" + material.name() + " x 64 §6pour §e" + sellPrice * 64);
                                } catch(SQLException e){
                                    player.sendMessage(Text.PREFIX + "§cUne erreur interne est survenue");
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(ConsulatCore.getInstance(), () -> {
                                        player.getPlayer().getInventory().addItem(save);
                                    });
                                    e.printStackTrace();
                                }
                            });
                        } else {
                            player.sendMessage(Text.PREFIX + "§cTu n'as pas assez de §4" + itemStack.getType().name() + "§c pour vendre un stack.");
                        }
                    }
                }
            } else {
                if(itemInHand.getType() == material){
                    if(itemInHand.getAmount() > 1){
                        itemInHand.setAmount((itemInHand.getAmount() - 1));
                    } else {
                        player.getPlayer().getInventory().clear(player.getPlayer().getInventory().getHeldItemSlot());
                    }
                    player.getPlayer().updateInventory();
                    Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                        try {
                            player.addMoney(sellPrice);
                            player.sendMessage(Text.PREFIX + "Tu as vendu §e" + material.name() + " x 1 §6pour §e" + sellPrice);
                        } catch(SQLException e){
                            e.printStackTrace();
                        }
                    });
                } else {
                    player.sendMessage(Text.PREFIX + "§cTu n'as pas de " + itemStack.getType().name());
                }
            }
        }
    }
    
    public static ShopEnum getShopMaterial(final String name){
        return Arrays.stream(ShopEnum.values()).filter(shop -> shop.getNewMaterialName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
    
    private void updateShop(Location old, Location loc) throws SQLException{
        PreparedStatement update = ConsulatAPI.getDatabase().prepareStatement("UPDATE shopinfo SET shop_x = ?, shop_y = ?, shop_z = ? WHERE shop_x = ? AND shop_y = ? AND shop_z = ?");
        update.setInt(1, loc.getBlockX());
        update.setInt(2, loc.getBlockY());
        update.setInt(3, loc.getBlockZ());
        update.setInt(4, old.getBlockX());
        update.setInt(5, old.getBlockY());
        update.setInt(6, old.getBlockZ());
        update.executeUpdate();
        update.close();
    }
    
}
