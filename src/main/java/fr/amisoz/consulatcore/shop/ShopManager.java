package fr.amisoz.consulatcore.shop;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.guis.shop.ShopGui;
import fr.amisoz.consulatcore.players.SPlayerManager;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.shop.admin.AdminShop;
import fr.amisoz.consulatcore.shop.admin.AdminShopBuy;
import fr.amisoz.consulatcore.shop.admin.AdminShopSell;
import fr.amisoz.consulatcore.shop.admin.custom.ASFly;
import fr.amisoz.consulatcore.shop.admin.custom.ASHome;
import fr.amisoz.consulatcore.shop.admin.custom.ASTouriste;
import fr.amisoz.consulatcore.shop.player.PlayerShop;
import fr.amisoz.consulatcore.shop.player.ShopItemType;
import fr.amisoz.consulatcore.utils.ChestUtils;
import fr.amisoz.consulatcore.utils.CoordinatesUtils;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.events.blocks.PlayerInteractContainerBlockEvent;
import fr.leconsulat.api.events.blocks.PlayerInteractSignEvent;
import fr.leconsulat.api.events.entities.PlayerTurnItemFrameEvent;
import fr.leconsulat.api.nbt.*;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.api.utils.FileUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class ShopManager implements Listener {
    
    private static ShopManager instance;
    
    static{
        ShopManager shopManager = new ShopManager();
        shopManager.register(AdminShopBuy.TYPE, AdminShopBuy::new);
        shopManager.register(AdminShopSell.TYPE, AdminShopSell::new);
        shopManager.register(ASFly.TYPE, ASFly::new);
        shopManager.register(ASHome.TYPE, ASHome::new);
        shopManager.register(ASTouriste.TYPE, ASTouriste::new);
    }
    
    private final Map<String, ShopConstructor> createShop = new HashMap<>();
    
    private Long2ObjectMap<Shop> shops = new Long2ObjectOpenHashMap<>();
    private Map<ShopItemType, Set<PlayerShop>> nonEmptyTypes = new HashMap<>();
    
    private ShopManager(){
        if(instance != null){
            throw new IllegalStateException();
        }
        instance = this;
        ShopGui.Container container = new ShopGui.Container();
        container.getGui(ShopItemType.ALL);
        //Volontairement bloquant
        try {
            ConsulatAPI.getConsulatAPI().log(Level.INFO, "Loading shops...");
            long start = System.currentTimeMillis();
            loadPlayerShops();
            ConsulatAPI.getConsulatAPI().log(Level.INFO, shops.size() + " Shops loaded in " + (System.currentTimeMillis() - start) + " ms");
        } catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    public void register(String type, ShopConstructor constructor){
        this.createShop.put(type, constructor);
    }
    
    public void loadAdminShops(){
        File file = FileUtils.loadFile(ConsulatAPI.getConsulatAPI().getDataFolder(), "admin-shops.dat");
        if(!file.exists()){
            return;
        }
        ConsulatAPI.getConsulatAPI().log(Level.INFO, "Loading admin shops...");
        long start = System.currentTimeMillis();
        int size = 0;
        try {
            NBTInputStream is = new NBTInputStream(file);
            CompoundTag shopsTag = is.read();
            is.close();
            List<CompoundTag> shops = shopsTag.getList("AdminShops", NBTType.COMPOUND);
            for(CompoundTag shopTag : shops){
                AdminShop shop = (AdminShop)createShop.get(shopTag.getString("Type")).construct(shopTag.getLong("Coords"));
                shop.loadNBT(shopTag);
                shop.createGui();
                this.shops.put(shop.getCoords(), shop);
                ++size;
            }
        } catch(IOException e){
            e.printStackTrace();
        }
        ConsulatAPI.getConsulatAPI().log(Level.INFO, size + " admin shops loaded in " + (System.currentTimeMillis() - start) + " ms");
    }
    
    public void saveAdminShops(){
        ListTag<CompoundTag> adminShops = new ListTag<>(NBTType.COMPOUND);
        for(Shop shop : shops.values()){
            if(!(shop instanceof AdminShop)){
                continue;
            }
            adminShops.addTag(((AdminShop)shop).saveNBT());
        }
        if(adminShops.getValue().isEmpty()){
            return;
        }
        try {
            File file = FileUtils.loadFile(ConsulatAPI.getConsulatAPI().getDataFolder(), "admin-shops.dat");
            if(!file.exists()){
                if(!file.createNewFile()){
                    throw new IOException("Couldn't create file.");
                }
            }
            CompoundTag tag = new CompoundTag();
            tag.put("AdminShops", adminShops);
            NBTOutputStream os = new NBTOutputStream(file, tag);
            os.write("AdminShop");
            os.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
    
    private void loadPlayerShops() throws SQLException{
        PreparedStatement shops = ConsulatAPI.getDatabase().prepareStatement("SELECT * FROM shopinfo");
        ResultSet resultShops = shops.executeQuery();
        World world = ConsulatCore.getInstance().getOverworld();
        while(resultShops.next()){
            Location location = new Location(
                    ConsulatCore.getInstance().getOverworld(),
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
                    if(ChestUtils.isDoubleChest(chest)){
                        Block nextChest = ChestUtils.getNextChest(chest.getBlock());
                        if(nextChest != null){
                            ChestUtils.setChestsSingle(chest.getBlock(), nextChest);
                        }
                    }
                    updateShop(old, location);
                } else {
                    ConsulatAPI.getConsulatAPI().log(Level.SEVERE, "Le shop en " + location + " n'est pas valide, il sera supprimé.");
                    ConsulatAPI.getConsulatAPI().logFile("Le shop en " + location + " n'est pas valide, il a été supprimé, owner: " + uuid + ", item vendu: " + resultShops.getString("material"));
                    removeShopDatabase(uuid, location.getBlockX(), location.getBlockY(), location.getBlockZ());
                    continue;
                }
            }
            ItemFrame itemFrame = PlayerShop.getItemFrame(block.getLocation());
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
            PlayerShop shop = new PlayerShop(
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
                    ConsulatAPI.getConsulatAPI().logFile("Le shop " + shop + " a été détruit car il n'a pas de cadre");
                    continue;
                }
            }
            if(shop.getSign() == null){
                removeShop(shop);
                ConsulatAPI.getConsulatAPI().logFile("Le shop " + shop + " a été détruit car il n'a pas de panneau");
            }
            shop.addInGui();
        }
    }
    
    public void addType(PlayerShop shop){
        if(shop.isEmpty()){
            return;
        }
        for(ShopItemType type : shop.getTypes()){
            nonEmptyTypes.computeIfAbsent(type, (k) -> new HashSet<>()).add(shop);
        }
    }
    
    public void removeType(PlayerShop shop){
        for(ShopItemType type : shop.getTypes()){
            Set<PlayerShop> shops = nonEmptyTypes.get(type);
            if(shops.size() == 1 && shops.iterator().next().equals(shop)){
                nonEmptyTypes.remove(type);
            } else {
                shops.remove(shop);
            }
        }
    }
    
    public Set<ShopItemType> getNonEmptyTypes(){
        return nonEmptyTypes.keySet();
    }
    
    public void addShop(SurvivalPlayer player, PlayerShop shop) throws SQLException{
        addShopDatabase(player.getUUID(), shop);
        Bukkit.getScheduler().scheduleSyncDelayedTask(ConsulatCore.getInstance(), () -> {
            player.addShop(shop);
            shops.put(shop.getCoords(), shop);
            shop.addInGui();
        });
    }
    
    public void addAdminShop(AdminShop shop){
        shops.put(shop.getCoords(), shop);
    }
    
    public void removeAdminShop(AdminShop adminShop){
        shops.remove(adminShop.getCoords());
    }
    
    public boolean isShop(Chest chest){
        return getPlayerShop(chest.getLocation()) != null;
    }
    
    @SuppressWarnings("ConstantConditions")
    private boolean isChestEmpty(Chest chest){
        for(ItemStack item : chest.getBlockInventory().getContents()){
            if(item != null){
                return false;
            }
        }
        return true;
    }
    
    @SuppressWarnings("ConstantConditions")
    @EventHandler
    public void onShopCreated(SignChangeEvent event){
        if(!event.getLines()[0].equalsIgnoreCase("[ConsulShop]")){
            return;
        }
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        if(event.getBlock().getType() != Material.OAK_WALL_SIGN){
            player.sendMessage(Text.USE_OAK_WIGN);
            event.getBlock().breakNaturally();
            return;
        }
        Chest chest = getChestFromSign(event.getBlock());
        if(chest == null || ChestUtils.isDoubleChest(chest)){
            event.getBlock().breakNaturally();
            player.sendMessage(Text.SHOP_ONLY_ON_CHEST);
            return;
        }
        if(isShop(chest)){
            event.getBlock().breakNaturally();
            player.sendMessage(Text.ALREADY_SHOP);
            return;
        }
        if(ConsulatCore.getInstance().getOverworld() != player.getPlayer().getWorld()){
            event.getBlock().breakNaturally();
            player.sendMessage(Text.DIMENSION_SHOP);
            return;
        }
        if(!player.canAddNewShop()){
            player.sendMessage(Text.HIT_SHOP_LIMIT);
            event.getBlock().breakNaturally();
            return;
        }
        if(isChestEmpty(chest)){
            player.sendMessage(Text.SHOP_CANT_BE_EMPTY);
            event.getBlock().breakNaturally();
            return;
        }
        String[] lines = event.getLines();
        if(lines[2].length() > 0 || lines[3].length() > 0){
            player.sendMessage(Text.TUTORIAL_SHOP);
            event.getBlock().breakNaturally();
            return;
        }
        double price;
        try {
            price = Double.parseDouble(lines[1]);
        } catch(NumberFormatException e){
            event.getBlock().breakNaturally();
            player.sendMessage(Text.INVALID_MONEY);
            return;
        }
        if(Double.isInfinite(price)){
            event.getBlock().breakNaturally();
            player.sendMessage(Text.INVALID_MONEY);
            return;
        }
        if(price < 1){
            event.getBlock().breakNaturally();
            player.sendMessage(Text.MUST_BE_AT_LEAST(1));
            return;
        }
        ItemStack sold = null;
        for(ItemStack item : chest.getBlockInventory().getContents()){
            if(item != null){
                if(sold == null){
                    if(item.getType() == Material.ENCHANTED_BOOK && ((EnchantmentStorageMeta)item.getItemMeta()).getStoredEnchants().size() != 1){
                        player.sendMessage(Text.ONLY_ONE_ENCHANT_SHOP);
                        event.getBlock().breakNaturally();
                        return;
                    }
                    sold = item;
                } else {
                    if(item.getType() != sold.getType() || !item.getItemMeta().equals(sold.getItemMeta())){
                        player.sendMessage(Text.ITEMS_MUST_BE_EQUALS);
                        event.getBlock().breakNaturally();
                        return;
                    }
                }
            }
        }
        if(sold == null){
            player.sendMessage(Text.SHOP_CANT_BE_EMPTY);
            event.getBlock().breakNaturally();
            return;
        }
        PlayerShop shop = new PlayerShop(player.getUUID(), player.getName(), sold, price, chest.getLocation(), false);
        if(!shop.placeItemFrame()){
            player.sendMessage(Text.REMOVE_ITEM_FRAME);
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
        ItemStack[] content = shop.getInventory().getContents();
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                addShop(player, shop);
                ConsulatAPI.getConsulatAPI().logFile("Shop created: " + shop + ", " + Arrays.toString(content));
            } catch(SQLException e){
                player.sendMessage(Text.ERROR);
                Bukkit.getScheduler().scheduleSyncDelayedTask(ConsulatCore.getInstance(), () -> {
                    event.getBlock().breakNaturally();
                });
                e.printStackTrace();
            }
        });
        player.sendMessage(Text.SHOP_CREATED);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(PlayerTurnItemFrameEvent event){
        Entity frame = event.getEntity();
        Location location = frame.getLocation().clone().add(0, -1, 0);
        if(frame.isInvulnerable() && getPlayerShop(location) == null){
            frame.remove();
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onShopBreak(BlockBreakEvent event){
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        switch(event.getBlock().getType()){
            case CHEST:{
                Shop shop = getShop(event.getBlock().getLocation());
                if(shop instanceof PlayerShop){
                    if(((PlayerShop)shop).getOwner().equals(player.getUUID())){
                        event.setCancelled(true);
                        player.sendMessage(Text.MUST_BREAK_SIGN);
                    } else {
                        event.setCancelled(true);
                        player.sendMessage(Text.SHOP_OWNED_BY(((PlayerShop)shop).getOwnerName()));
                    }
                } else if(shop instanceof AdminShop){
                    if(!player.hasPower(Rank.RESPONSABLE)){
                        event.setCancelled(true);
                        return;
                    }
                    ((Chest)event.getBlock().getState()).getBlockInventory().clear();
                    shops.remove(shop.getCoords());
                } else {
                    return;
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
                PlayerShop shop = getPlayerShop(chest.getLocation());
                if(shop == null){
                    return;
                }
                if(player.getUUID().equals(shop.getOwner()) || player.hasPower(Rank.RESPONSABLE) || player.getRank() == Rank.DEVELOPPEUR){
                    if(!shop.isEmpty()){
                        shop.removeInGui();
                    }
                    ItemStack[] content = shop.getInventory().getContents();
                    Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                        try {
                            removeShop(shop);
                            player.sendMessage(Text.SHOP_REMOVED);
                            ConsulatAPI.getConsulatAPI().logFile("Shop removed: " + shop + ", " + Arrays.toString(content));
                        } catch(SQLException e){
                            player.sendMessage(Text.ERROR);
                            e.printStackTrace();
                        }
                    });
                } else {
                    event.setCancelled(true);
                    player.sendMessage(Text.CANT_BREAK_SHOP);
                }
                break;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBuyingPlayerShop(PlayerInteractSignEvent event){
        if(!(event.getBlock().getBlockData() instanceof Directional)){
            return;
        }
        Sign sign = (Sign)event.getBlock().getState();
        String[] lines = sign.getLines();
        if(!lines[0].contains("§8[§aConsulShop§8]")){
            return;
        }
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        Block block = event.getBlock();
        Chest chest = getChestFromSign(block);
        if(chest == null){
            return;
        }
        PlayerShop shop = getPlayerShop(chest.getLocation());
        if(shop == null){
            return;
        }
        event.setCancelled(true);
        if(shop.isEmpty()){
            player.sendMessage(Text.SHOP_IS_EMPTY);
            return;
        }
        if(shop.isOpen()){
            player.sendMessage(Text.SHOP_IS_NOT_AVAILABLE);
            return;
        }
        if(shop.getOwner().equals(player.getUUID())){
            if(!ConsulatAPI.getConsulatAPI().isDevelopment()){
                player.sendMessage(Text.CANT_BUY_OWN_SHOP);
                return;
            }
        }
        int placeAvailable = player.spaceAvailable(shop.getItem());
        if(placeAvailable <= 0){
            player.sendMessage(Text.NOT_ENOUGH_SPACE_INVENTORY);
            return;
        }
        int amount = Integer.min(shop.getAmount(!player.getPlayer().isSneaking() ? 1 : 64), placeAvailable);
        double price = amount * shop.getPrice();
        if(!player.hasMoney(price)){
            player.sendMessage(Text.NOT_ENOUGH_MONEY);
            return;
        }
        shop.buy(amount);
        player.addItemInInventory(amount, shop.getItem());
        player.removeMoney(price);
        double moneyEarned = price >= 1000 ? price + price / 1000 / 100 * price : price;
        SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(shop.getOwner());
        if(target == null){
            Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                SPlayerManager.getInstance().addMoney(shop.getOwner(), moneyEarned);
            });
        } else {
            target.addMoney(moneyEarned);
            target.sendMessage(Text.SHOP_NOTIFICATION(moneyEarned));
        }
        player.sendMessage(formatShopMessage(shop.getItem(), amount, price, ShopAction.BUY));
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
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onChestClick(PlayerInteractContainerBlockEvent event){
        if(event.getType() != PlayerInteractContainerBlockEvent.Type.CHEST){
            return;
        }
        Shop shop = getShop(event.getBlock().getLocation());
        if(shop == null){
            return;
        }
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        if(shop instanceof PlayerShop){
            PlayerShop playerShop = (PlayerShop)shop;
            if(!playerShop.isOwner(player.getUUID())){
                player.sendMessage(Text.SHOP_OWNED_BY(playerShop.getOwnerName()));
                if(!player.hasPower(Rank.ADMIN)){
                    event.setCancelled(true);
                }
            }
        } else if(shop instanceof AdminShop){
            ((AdminShop)shop).getGui().open(player);
            event.setCancelled(true);
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
        PlayerShop shop = getPlayerShop(chest.getLocation());
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
        PlayerShop shop = getPlayerShop(chest.getLocation());
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
        PlayerShop shop = getPlayerShop(chest.getLocation());
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
        Block otherChest = ChestUtils.getNextChest(chest);
        if(otherChest != null){
            if(!isShop((Chest)otherChest.getState())){
                return;
            }
            ChestUtils.setChestsSingle(chest, otherChest);
            event.getPlayer().sendBlockChange(otherChest.getLocation(), otherChest.getBlockData());
        }
        
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
    
    public void removeShop(PlayerShop shop) throws SQLException{
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
    
    public @Nullable Shop getShop(Location location){
        return shops.get(CoordinatesUtils.convertCoordinates(location));
    }
    
    public @Nullable PlayerShop getPlayerShop(Location location){
        Shop shop = shops.get(CoordinatesUtils.convertCoordinates(location));
        if(shop instanceof PlayerShop){
            return (PlayerShop)shop;
        }
        return null;
    }
    
    public Collection<Shop> getShops(){
        return Collections.unmodifiableCollection(shops.values());
    }
    
    public List<PlayerShop> getPlayerShops(UUID uuid){
        List<PlayerShop> playerShops = new ArrayList<>();
        for(Shop shop : shops.values()){
            PlayerShop playerShop = shop instanceof PlayerShop ? (PlayerShop)shop : null;
            if(playerShop != null && playerShop.getOwner().equals(uuid)){
                playerShops.add(playerShop);
            }
        }
        return playerShops;
    }
    
    public void addShopDatabase(UUID uuid, PlayerShop shop) throws SQLException{
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
    
    public TextComponent formatShopMessage(ItemStack item, int amount, double price, ShopAction action){
        if(item.hasItemMeta()){
            ItemMeta meta = item.getItemMeta();
            if(meta.hasDisplayName()){
                return new TextComponent(Text.PREFIX + "Tu as " + action.message + " §e" + meta.getDisplayName() + " x" + amount + " §6pour §e" + ConsulatCore.formatMoney(price) + ".");
            }
        }
        TextComponent message = new TextComponent(Text.PREFIX + "Tu as " + action.message + " §e");
        message.addExtra(new TranslatableComponent(ConsulatAPI.getNMS().getItem().getItemNameId(item)));
        message.addExtra(" x" + amount + " §6pour §e" + ConsulatCore.formatMoney(price) + ".");
        return message;
    }
    
    public enum ShopAction {
        BUY("acheté"),
        SELL("vendu");
        
        private String message;
        
        ShopAction(String message){
            this.message = message;
        }
        
        public String getMessage(){
            return message;
        }
    }
}
