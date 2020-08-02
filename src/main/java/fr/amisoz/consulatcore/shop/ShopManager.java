package fr.amisoz.consulatcore.shop;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.guis.shop.ShopGui;
import fr.amisoz.consulatcore.players.SPlayerManager;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.shop.admin.AdminShop;
import fr.amisoz.consulatcore.shop.player.PlayerShop;
import fr.amisoz.consulatcore.shop.player.ShopItemType;
import fr.amisoz.consulatcore.utils.ChestUtils;
import fr.amisoz.consulatcore.utils.CoordinatesUtils;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.events.blocks.PlayerInteractContainerBlockEvent;
import fr.leconsulat.api.events.blocks.PlayerInteractSignEvent;
import fr.leconsulat.api.nbt.*;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.api.utils.FileUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
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
        new ShopManager();
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
                    ConsulatAPI.getConsulatAPI().logFile("Le shop " + shop + " a été détruit par il n'a pas de cadre");
                    continue;
                }
            }
            if(shop.getSign() == null){
                removeShop(shop);
                ConsulatAPI.getConsulatAPI().logFile("Le shop " + shop + " a été détruit par il n'a pas de panneau");
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
    
    public boolean isShop(Chest chest){
        return getPlayerShop(chest.getLocation()) != null;
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
    
    @SuppressWarnings("ConstantConditions")
    private boolean isChestEmpty(Chest chest){
        for(ItemStack item : chest.getBlockInventory().getContents()){
            if(item != null){
                return false;
            }
        }
        return true;
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
        if(ChestUtils.isDoubleChest(chest)){
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
            player.sendMessage("§cLe prix est incorrect.");
            return;
        }
        if(Double.isInfinite(price)){
            event.getBlock().breakNaturally();
            player.sendMessage("§cLe prix est incorrect.");
            return;
        }
        if(price < 1){
            event.getBlock().breakNaturally();
            player.sendMessage("§cLe prix doit être d'au moins 1€.");
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
        PlayerShop shop = new PlayerShop(
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
        ItemStack[] content = shop.getInventory().getContents();
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                addShop(player, shop);
                ConsulatAPI.getConsulatAPI().logFile("Shop created: " + shop + ", " + Arrays.toString(content));
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
            if(frame.isInvulnerable() && getPlayerShop(location) == null){
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
                if(shop instanceof PlayerShop){
                    if(((PlayerShop)shop).getOwner().equals(player.getUUID())){
                        event.setCancelled(true);
                        player.sendMessage(Text.PREFIX + "§cTu dois casser le panneau pour supprimer ton Shop!");
                    } else {
                        event.setCancelled(true);
                        player.sendMessage(Text.PREFIX + "§cCe shop appartient à: §4" + ((PlayerShop)shop).getOwnerName() + "§c.");
                    }
                } else if(shop instanceof AdminShop){
                    if(!player.hasPower(Rank.RESPONSABLE)){
                        event.setCancelled(true);
                        return;
                    }
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
                            player.sendMessage(Text.PREFIX + "Tu viens de détruire un de tes shops!");
                            ConsulatAPI.getConsulatAPI().logFile("Shop removed: " + shop + ", " + Arrays.toString(content));
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
        shop.buy(amount);
        player.addItemInInventory(amount, shop.getItem());
        player.removeMoney(price);
        SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(shop.getOwner());
        if(target == null){
            Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                SPlayerManager.getInstance().addMoney(shop.getOwner(), price);
            });
        } else {
            double percentAdd = 0;
            if(price >= 1000){
                percentAdd = (price / 1000) / 100 * price;
            }
            target.addMoney(price + percentAdd);
            target.sendMessage(Text.PREFIX + "§aTu as reçu " + (price + percentAdd) + " € grâce à un de tes shops.");
        }
        player.sendMessage(Text.PREFIX + "Tu as acheté §e" + shop.getItemType().toString() + " x " + amount + " §6pour §e" + price);
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
                if(player.hasPower(Rank.ADMIN)){
                    player.sendMessage(Text.PREFIX + "§cCe shop appartient à: §4" + playerShop.getOwnerName() + "§c.");
                } else {
                    player.sendMessage(Text.PREFIX + "§cTu ne peux pas ouvrir ce shop, il appartient à " + playerShop.getOwnerName() + "§c.");
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
                    player.removeMoney(buyPrice);
                    player.setRank(Rank.TOURISTE);
                    player.sendMessage(Text.PREFIX + "Tu es désormais touriste !");
                } else {
                    player.sendMessage(Text.PREFIX + "§cTu n'as pas assez d'argent");
                }
                return;
            } else if(sign.getLines()[1].equalsIgnoreCase("FLY_5")){
                if(player.hasMoney(buyPrice)){
                    if(!player.hasFly()){
                        player.removeMoney(buyPrice);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "boutique fly5 " + player.getName());
                    } else {
                        player.sendMessage(ChatColor.RED + "Tu as déjà le fly.");
                    }
                } else {
                    player.sendMessage(Text.PREFIX + "§cTu n'as pas assez d'argent");
                }
            }
        }
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
