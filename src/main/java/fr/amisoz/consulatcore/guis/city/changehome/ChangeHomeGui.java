package fr.amisoz.consulatcore.guis.city.changehome;

import fr.amisoz.consulatcore.zones.ZoneManager;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.event.GuiOpenEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.template.DataRelatGui;
import org.bukkit.Location;
import org.bukkit.Material;

public class ChangeHomeGui extends DataRelatGui<City> {
    
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
    public void onOpen(GuiOpenEvent event){
        Location home = getData().getHome();
        Location newHome = event.getPlayer().getPlayer().getLocation();
        setDescription(22, "§cAncien §7home: ",
                "§7x: " + home.getBlockX(), "§7y: " + home.getBlockY(), "§7z: " + home.getBlockZ(), "",
                "§aNouveau §7home: ",
                "§7x: " + newHome.getBlockX(), "§7y: " + newHome.getBlockY(), "§7z: " + newHome.getBlockZ());
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        switch(event.getSlot()){
            case 20:
                event.getPlayer().getPlayer().closeInventory();
                event.getPlayer().sendMessage("§cTu as annulé le déplacement du home");
                return;
            case 24:
                ZoneManager.getInstance().setHome(getData(), event.getPlayer().getPlayer().getLocation());
                event.getPlayer().sendMessage("§aTu as déplacé le home de ta ville.");
                event.getPlayer().getPlayer().closeInventory();
                break;
        }
    }
    
}
