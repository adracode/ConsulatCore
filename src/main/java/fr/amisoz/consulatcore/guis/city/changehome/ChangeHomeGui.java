package fr.amisoz.consulatcore.guis.city.changehome;

import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.zones.ZoneManager;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.event.GuiOpenEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.template.DataRelatGui;
import fr.leconsulat.api.player.ConsulatPlayer;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChangeHomeGui extends DataRelatGui<City> {
    
    private Map<UUID, Location> newHomeByPlayer = new HashMap<>(1);
    
    public ChangeHomeGui(City city){
        super(city, "§4Changement de Home", 5,
                IGui.getItem("§6Changement de home", 22, Material.OAK_SIGN),
                IGui.getItem("§cAnnuler", 20, Material.RED_CONCRETE),
                IGui.getItem("§aConfirmer", 24, Material.GREEN_CONCRETE));
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44);
        setDeco(Material.RED_STAINED_GLASS_PANE, 0, 1, 2, 3, 4, 5, 6, 7, 8);
        setDestroyOnClose(true);
    }
    
    @Override
    public void onOpened(GuiOpenEvent event){
        Location home = getData().getHome();
        Location newHome = event.getPlayer().getPlayer().getLocation();
        setDescriptionPlayer(22, event.getPlayer(),"§cAncien §7home: ",
                "§7x: " + home.getBlockX(), "§7y: " + home.getBlockY(), "§7z: " + home.getBlockZ(), "",
                "§aNouveau §7home: ",
                "§7x: " + newHome.getBlockX(), "§7y: " + newHome.getBlockY(), "§7z: " + newHome.getBlockZ());
        newHomeByPlayer.put(event.getPlayer().getUUID(), newHome);
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        ConsulatPlayer player = event.getPlayer();
        switch(event.getSlot()){
            case 20:
                player.getPlayer().closeInventory();
                player.sendActionBar(Text.SET_HOME_CANCELLED);
                return;
            case 24:
                Location newSpawn = newHomeByPlayer.remove(player.getUUID());
                if(newSpawn == null){
                    player.getPlayer().closeInventory();
                    player.sendMessage(Text.ERROR);
                    return;
                }
                player.sendMessage(Text.YOU_SET_HOME_CITY);
                player.getPlayer().closeInventory();
                ZoneManager.getInstance().setHome(getData(), newSpawn);
                break;
        }
    }
    
}
