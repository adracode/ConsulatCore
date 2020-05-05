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
import java.util.Map;
import java.util.logging.Level;

public class ShopGui extends GuiListener {
    
    private int lastGui = 0;
    private int nextSlot = 0;
    
    public ShopGui(){
        super(null, Integer.class);
        int lines = 6;
        addGui(null, this, "§eListe des shops §c(0)", lines,
                getItem("§ePage précédente", (lines - 1) * 9, Material.ARROW),
                getItem("§ePage suivante", lines * 9 - 1, Material.ARROW)
        );
        setCreateOnOpen(false);
    }
    
    public void addShop(Shop shop){
        if(shop.isEmpty()){
            return;
        }
        Gui gui = getGui(lastGui);
        if(gui == null || nextSlot >= (gui.getLines() - 1) * 9){
            ++lastGui;
            nextSlot = 0;
            gui = create(lastGui);
        }
        GuiItem item = new GuiItem(shop.getItem(), nextSlot++);
        item.setDescription("§eVendu par: §c" + shop.getOwnerName(),
                "§ePrix unitaire: §c" + shop.getPrice() + "§e€.",
                "§eCoordonnées: X: §c" + shop.getX() + "§e Y: §c" + shop.getY() + "§e Z: §c" + shop.getZ(),
                "§eTéléportation pour: §c10§e€.");
        item.setAttachedObject(shop);
        gui.setItem(item);
    }
    
    public void removeShop(Shop shop){
        for(Map.Entry<Object, Gui> guis : getGuis().entrySet()){
            Gui gui = guis.getValue();
            for(int i = 0; i < (gui.getLines() - 1) * 9; ++i){
                GuiItem item = gui.getItem(i);
                if(item != null){
                    if(shop.equals(item.getAttachedObject())){
                        gui.removeItem(i);
                        if(i != nextSlot - 1 || (int)gui.getKey() != lastGui){
                            Gui lastGui = getGui(this.lastGui);
                            lastGui.moveItem(nextSlot - 1, gui, i);
                        }
                        if(nextSlot <= 1){
                            if(lastGui > 1){
                                --lastGui;
                                nextSlot = (gui.getLines() - 1) * 9;
                            } else {
                                nextSlot = 0;
                            }
                        } else {
                            --nextSlot;
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
        lastGui = (int)event.getKey();
        gui.setName("§eListe des shops §c(" + lastGui + ")");
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
                if((int)event.getGui().getKey() <= 1){
                    return;
                }
                open(event.getPlayer(), (int)event.getGui().getKey() - 1);
                break;
            case 53:
                if((int)event.getGui().getKey() == lastGui){
                    return;
                }
                open(event.getPlayer(), (int)event.getGui().getKey() + 1);
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
