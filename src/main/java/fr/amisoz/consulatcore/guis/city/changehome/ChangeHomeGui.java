package fr.amisoz.consulatcore.guis.city.changehome;

import fr.amisoz.consulatcore.zones.ZoneManager;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.leconsulat.api.gui.GuiListener;
import fr.leconsulat.api.gui.events.GuiClickEvent;
import fr.leconsulat.api.gui.events.GuiOpenEvent;
import org.bukkit.Location;
import org.bukkit.Material;

public class ChangeHomeGui extends GuiListener<City> {
    
    public ChangeHomeGui(){
        super(5);
        setTemplate("§4Changement de Home",
                getItem("§6Changement de home", 22, Material.OAK_SIGN),
                getItem("§cAnnuler", 20, Material.RED_CONCRETE),
                getItem("§aConfirmer", 24, Material.GREEN_CONCRETE))
                .setDeco(Material.BLACK_STAINED_GLASS_PANE, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44)
                .setDeco(Material.RED_STAINED_GLASS_PANE, 0, 1, 2, 3, 4, 5, 6, 7, 8);
        setCreateOnOpen(true);
        setDestroyOnClose(true);
    }
    
    @Override
    public void onOpen(GuiOpenEvent<City> event){
        Location home = event.getData().getHome();
        Location newHome = event.getPlayer().getPlayer().getLocation();
        event.getPagedGui().setDescription(22, "§cAncien §7home: ",
                "§7x: " + home.getBlockX(), "§7y: " + home.getBlockY(), "§7z: " + home.getBlockZ(), "",
                "§aNouveau §7home: ",
                "§7x: " + newHome.getBlockX(), "§7y: " + newHome.getBlockY(), "§7z: " + newHome.getBlockZ());
    }
 
    @Override
    public void onClick(GuiClickEvent<City> event){
        switch(event.getSlot()){
            case 20:
                event.getPlayer().getPlayer().closeInventory();
                event.getPlayer().sendMessage("§cTu as annulé le déplacement du home");
                return;
            case 24:
                ZoneManager.getInstance().setHome(event.getData(), event.getPlayer().getPlayer().getLocation());
                event.getPlayer().sendMessage("§aTu as déplacé le home de ta ville.");
                event.getPlayer().getPlayer().closeInventory();
                break;
        }
    }
    
}
