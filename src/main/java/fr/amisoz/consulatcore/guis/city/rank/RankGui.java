package fr.amisoz.consulatcore.guis.city.rank;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.leconsulat.api.gui.Gui;
import fr.leconsulat.api.gui.GuiListener;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.PagedGui;
import fr.leconsulat.api.gui.events.GuiClickEvent;
import fr.leconsulat.api.gui.events.PagedGuiCreateEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;

public class RankGui extends GuiListener<City> {
    
    private static final byte RANK1_SLOT = 20;
    private static final byte RANK2_SLOT = 22;
    private static final byte RANK3_SLOT = 24;
    
    public RankGui(){
        super(5);
        setTemplate("Grades",
                getItem("grade1", RANK1_SLOT, Material.DIAMOND),
                getItem("grade2", RANK2_SLOT, Material.GOLD_INGOT),
                getItem("grade3", RANK3_SLOT, Material.IRON_INGOT)
        ).setDeco(Material.BLACK_STAINED_GLASS_PANE, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44);
    }
    
    @Override
    public void onPageCreate(PagedGuiCreateEvent<City> event){
        PagedGui<City> current = event.getPagedGui();
        setRank(current, 0);
        setRank(current, 1);
        setRank(current, 2);
    }
    
    public void setRank(PagedGui<City> gui, int index){
        City city = gui.getGui().getData();
        switch(index){
            case 0:
                gui.setDisplayName(RANK1_SLOT, "§b" + city.getRank(0));
            case 1:
                gui.setDisplayName(RANK2_SLOT, "§e" + city.getRank(1));
            case 2:
                gui.setDisplayName(RANK3_SLOT, "§7" + city.getRank(2));
        }
    }
    
    @Override
    public void onClick(GuiClickEvent<City> event){
        switch(event.getSlot()){
            case RANK1_SLOT:
            case RANK2_SLOT:
            case RANK3_SLOT:
                SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
                PagedGui<City> current = event.getPagedGui();
                City city = event.getData();
                GuiManager.getInstance().userInput(player.getPlayer(),
                        input -> {
                            if(input.isEmpty()){
                                player.sendMessage("§cLe grade entré n'est pas valide.");
                                return;
                            }
                            city.setRank(event.getSlot() - 20 - ((event.getSlot() - 20) >> 1), input);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(ConsulatCore.getInstance(), () -> {
                                current.open(player);
                            });
                        },
                        new String[]{"", "^^^^^^^^^^^^^^", "Entrez le", "nom du grade"}, 0);
                break;
        }
    }
    
    public void setRanks(Gui<City> gui){
        if(gui == null){
            return;
        }
        setRank(gui.getPage(), 0);
        setRank(gui.getPage(), 1);
        setRank(gui.getPage(), 2);
    }
}
