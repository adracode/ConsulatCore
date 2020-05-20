package fr.amisoz.consulatcore.shop;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.gui.Gui;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.GuiListener;
import fr.leconsulat.api.gui.events.GuiClickEvent;
import fr.leconsulat.api.gui.events.GuiCloseEvent;
import fr.leconsulat.api.gui.events.GuiCreateEvent;
import fr.leconsulat.api.gui.events.GuiOpenEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class ShopGui extends GuiListener {
    
    private Map<ShopItemType, AtomicInteger> lastPages = new HashMap<>();
    private Map<ShopItemType, AtomicInteger> nextSlots = new HashMap<>();

    public ShopGui(){
        super(null, ShopItemType.class);
        int lines = 6;
        lastPages.put(ShopItemType.ALL, new AtomicInteger(0));
        nextSlots.put(ShopItemType.ALL, new AtomicInteger(0));
        addGui(null, this, "§4Shops §c(0)", lines,
                getItem("§ePage précédente", (lines - 1) * 9, Material.ARROW),
                getItem("§ePage suivante", lines * 9 - 1, Material.ARROW)
        );
        addGui(ShopItemType.ALL, this, "§4Shops §c(1)", lines,
                getItem("§ePage précédente", (lines - 1) * 9, Material.ARROW),
                getItem("§ePage suivante", lines * 9 - 1, Material.ARROW)
        );
        setCreateOnOpen(false);
    }
    
    public void addShop(Shop shop, ShopItemType key){
        if(shop.isEmpty()){
            return;
        }
        if(getGui(key) == null){
            lastPages.put(key, new AtomicInteger(0));
            nextSlots.put(key, new AtomicInteger(0));
            create(key);
        }
        AtomicInteger nextSlot = nextSlots.get(key);
        AtomicInteger lastPage = lastPages.get(key);
        if(nextSlot.get() >= 45){
            lastPage.incrementAndGet();
            nextSlot.set(0);
        }
        Gui gui = getGui(key, lastPage.get());
        GuiItem item = new GuiItem(shop.getItem(), nextSlot.getAndIncrement());
        item.setDescription("§eVendu par: §c" + shop.getOwnerName(),
                "§ePrix unitaire: §c" + shop.getPrice() + "§e€.",
                "§eCoordonnées: X: §c" + shop.getX() + "§e Y: §c" + shop.getY() + "§e Z: §c" + shop.getZ(),
                "§eTéléportation pour: §c10§e€.");
        item.setAttachedObject(shop);
        gui.setItem(item);
    }
    
    public void removeShop(Shop shop, ShopItemType key){
        AtomicInteger nextSlot = nextSlots.get(key);
        AtomicInteger lastPage = lastPages.get(key);
        for(Gui gui : getGuis().get(key)){
            for(int i = 0; i < (gui.getLines() - 1) * 9; ++i){
                GuiItem item = gui.getItem(i);
                if(item != null){
                    if(shop.equals(item.getAttachedObject())){
                        gui.removeItem(i);
                        if(i != nextSlot.get() - 1 || gui.getPage() != lastPage.get()){
                            Gui lastGui = getGui(key, lastPage.get());
                            lastGui.moveItem(nextSlot.get() - 1, gui, i);
                        }
                        if(nextSlot.get() <= 1){
                            if(lastPage.get() > 0){
                                lastPage.decrementAndGet();
                                nextSlot.set((gui.getLines() - 1) * 9);
                            } else {
                                nextSlot.set(0);
                            }
                        } else {
                            nextSlot.decrementAndGet();
                        }
                        return;
                    }
                }
            }
        }
    }
    
    @Override
    public void onCreate(GuiCreateEvent event){
        if(event.getKey() == null){
            return;
        }
        Gui gui = event.getGui();
        Object key = event.getKey();
        if(key.equals(ShopItemType.ALL)){
            gui.setName("§4Shops §8(§3" + (lastPages.get(event.getKey()).get() + 1) + "§8)");
        } else {
            gui.setName("§4Shops §8(§3" + key.toString() + "§8) (§3" + (lastPages.get(event.getKey()).get() + 1) + "§8)");
        }
    }
    
    @Override
    public void onOpen(GuiOpenEvent event){
    
    }
    
    @Override
    public void onClose(GuiCloseEvent event){
    
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        switch(event.getSlot()){
            case 45:
                if(event.getGui().getPage() <= 0){
                    return;
                }
                open(event.getPlayer(), event.getGui().getKey(), event.getGui().getPage() - 1);
                break;
            case 53:
                if(event.getGui().getPage() == lastPages.get(event.getKey()).get()){
                    return;
                }
                open(event.getPlayer(), event.getGui().getKey(), event.getGui().getPage() + 1);
                break;
            default:
                Shop shop = (Shop)event.getGui().getItem(event.getSlot()).getAttachedObject();
                SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
                player.getPlayer().closeInventory();
                if(player.hasMoney(10.0)){
                    try {
                        if(shop != null){
                            Sign sign = shop.getSign();
                            Location shopLocation = shop.getLocation();
                            if(sign == null){
                                player.getPlayer().teleport(shopLocation.clone().add(0, 1, 0));
                            } else {
                                Location block = sign.getLocation().clone().add(0.5, 0, 0.5);
                                block.setDirection(block.toBlockLocation().subtract(shopLocation).multiply(-1).toVector());
                                if(block.getBlock().getRelative(BlockFace.UP).getType() != Material.AIR){
                                    block.add(0, -1, 0);
                                }
                                player.getPlayer().teleport(block);
                            }
                        } else {
                            player.sendMessage(Text.PREFIX + "§cCe shop n'a pas été trouvé");
                            ConsulatAPI.getConsulatAPI().log(Level.WARNING, "Shop not found in list: " + event.getGui().getItem(event.getSlot()));
                            ConsulatAPI.getConsulatAPI().logFile("Shop not found in list: " + event.getGui().getItem(event.getSlot()));
                        }
                    } catch(NullPointerException e){
                        player.sendMessage("Erreur lors de la téléportation");
                        return;
                    }
                    player.sendMessage(ChatColor.YELLOW + "Téléportation réussie pour " + ChatColor.RED + "10.0" + ChatColor.YELLOW + "€.");
                    Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                        try {
                            player.removeMoney(10.0);
                        } catch(SQLException e){
                            e.printStackTrace();
                        }
                    });
                } else {
                    player.sendMessage(Text.PREFIX + "§cVous n'avez pas assez d'argent.");
                }
        }
    }
}
