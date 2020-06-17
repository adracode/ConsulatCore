package fr.amisoz.consulatcore.guis.city;

import fr.amisoz.consulatcore.zones.ZoneManager;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.leconsulat.api.gui.GuiContainer;
import fr.leconsulat.api.gui.events.GuiClickEvent;
import fr.leconsulat.api.gui.events.PagedGuiCreateEvent;
import org.bukkit.Material;

public class DisbandGui extends GuiContainer<City> {
    
    public DisbandGui(){
        super(5);
        setTemplate("§4Confirmer",
                getItem("§6Attention", 22, Material.OAK_SIGN, "§cLa destruction de la ville", "§cest définitive et irréversible.", "§cVous perdrez tous les claims de la ville", "§cet l'argent de la banque"),
                getItem("§cAnnuler la destruction", 20, Material.RED_CONCRETE),
                getItem("§aConfirmer la destruction", 24, Material.GREEN_CONCRETE)
        ).setDeco(Material.BLACK_STAINED_GLASS_PANE, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44)
                .setDeco(Material.RED_STAINED_GLASS_PANE, 0, 1, 2, 3, 4, 5, 6, 7, 8);
        setCreateOnOpen(true);
        setDestroyOnClose(true);
    }
    
    @Override
    public void onPageCreate(PagedGuiCreateEvent<City> event){
        event.getPagedGui().setName("§4Confirmer §8(§3" + event.getData().getName() + "§8)");
    }
    
    @Override
    public void onClick(GuiClickEvent<City> event){
        switch(event.getSlot()){
            case 20:
                event.getPlayer().getPlayer().closeInventory();
                return;
            case 24:
                ZoneManager.getInstance().deleteCity(event.getData());
                event.getPlayer().sendMessage("§aTu as détruit ta ville :(");
                event.getPlayer().getPlayer().closeInventory();
                break;
        }
    }
}
