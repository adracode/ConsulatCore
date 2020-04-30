package fr.amisoz.consulatcore.shop;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.gui.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;

public class ShopGui extends GuiListener {
    
    private int lastGui = 0;
    private int nextSlot = 0;
    
    public ShopGui(){
        super(null, Integer.class);
        addGui(null, this, "§eListe des §cshops", 6,
                getItem("§ePage précédente", 45, Material.ARROW),
                getItem("§ePage suivante", 53, Material.ARROW),
                getItem("§ePage: §c", 49, Material.PAPER)
        );
        setCreateOnOpen(false);
    }
    
    public void addShop(Shop shop){
        Gui gui = getGui(lastGui);
        if(nextSlot >= 44 || gui == null){
            ++lastGui;
            nextSlot = 0;
            if(gui == null){
                gui = create(lastGui);
            }
        }
        GuiItem item = new GuiItem(shop.getItem(), nextSlot++);
        item.setDescription("§eVendu par: §c" + shop.getOwnerName(),
                "§ePrix unitaire: §c" + shop.getPrice() + "§e€.",
                "§eCoordonnées: X: §c" + shop.getX() + "§e Y: §c" + shop.getY() + "§e Z: §c" + shop.getY(),
                "§eTéléportation pour: §c10§e€.");
        item.setAttachedObject(shop);
        gui.setItem(item);
    }
    
    public void removeShop(Shop shop){
        for(Map.Entry<Object, Gui> guis : getGuis().entrySet()){
            Gui gui = guis.getValue();
            for(int i = 0; i < 45; ++i){
                GuiItem item = gui.getItem(i);
                if(item != null){
                    if(shop.equals(item.getAttachedObject())){
                        gui.removeItem(i);
                        if(i != nextSlot - 1 && (int)gui.getKey() != lastGui){
                            Gui lastGui = getGui(this.lastGui);
                            lastGui.moveItem(nextSlot - 1, gui, i);
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
        gui.setDisplayName(49, "§ePage: §c" + lastGui);
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
            case 49:
                break;
            default:
                Shop shop = (Shop)event.getGui().getItem(event.getSlot()).getAttachedObject();
                SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
                if(player.hasMoney(10.0)){
                    try {
                        if(shop != null){
                            Sign sign = shop.getSign();
                            if(sign == null){
                                player.getPlayer().teleport(shop.getLocation().clone().add(0, 1, 0));
                            } else {
                                Location block = sign.getLocation().clone().add(0.5, 0, 0.5);
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
                    player.getPlayer().closeInventory();
                }
        }
    }
}
