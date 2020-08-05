package fr.amisoz.consulatcore.guis.city;

import fr.amisoz.consulatcore.zones.ZoneManager;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.leconsulat.api.gui.GuiContainer;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.Datable;
import fr.leconsulat.api.gui.gui.template.DataGui;
import org.bukkit.Material;

public class DisbandGui extends DataGui<City> {
    
    public DisbandGui(City city){
        super(city, "§4Confirmer §8(§3" + city.getName() + "§8)", 5,
                IGui.getItem("§6Attention", 22, Material.OAK_SIGN, "§cLa destruction de la ville", "§cest définitive et irréversible.", "§cTous les claims de la ville et", "§cl'argent de la banque seront perdus"),
                IGui.getItem("§cAnnuler la destruction", 20, Material.RED_CONCRETE),
                IGui.getItem("§aConfirmer la destruction", 24, Material.GREEN_CONCRETE));
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44);
        setDeco(Material.RED_STAINED_GLASS_PANE, 0, 1, 2, 3, 4, 5, 6, 7, 8);
        setDestroyOnClose(true);
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        switch(event.getSlot()){
            case 20:
                event.getPlayer().getPlayer().closeInventory();
                return;
            case 24:
                ZoneManager.getInstance().deleteCity(getData());
                event.getPlayer().sendMessage("§aTu as détruit ta ville :(");
                event.getPlayer().getPlayer().closeInventory();
                break;
        }
    }
    
    public static class Container extends GuiContainer<City> {
        
        private static Container instance;
        
        public Container(){
            if(instance != null){
                throw new IllegalStateException();
            }
            instance = this;
            GuiManager.getInstance().addContainer("city-disband", this);
        }
        
        @Override
        public Datable<City> createGui(City city){
            return new DisbandGui(city);
        }
    }
    
    
}
