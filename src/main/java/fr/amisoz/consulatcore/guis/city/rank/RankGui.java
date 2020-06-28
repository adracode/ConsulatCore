package fr.amisoz.consulatcore.guis.city.rank;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.template.DataRelatGui;
import org.bukkit.Bukkit;
import org.bukkit.Material;

public class RankGui extends DataRelatGui<City> {
    
    private static final byte RANK1_SLOT = 19;
    private static final byte RANK2_SLOT = 21;
    private static final byte RANK3_SLOT = 23;
    private static final byte RANK4_SLOT = 25;
    
    public RankGui(City city){
        super(city, "Grades", 5,
                IGui.getItem("grade1", RANK1_SLOT, Material.BEACON, "", "§aCliquez §7pour changer", "§7le nom du grade"),
                IGui.getItem("grade2", RANK2_SLOT, Material.DIAMOND, "", "§aCliquez §7pour changer", "§7le nom du grade"),
                IGui.getItem("grade3", RANK3_SLOT, Material.GOLD_INGOT, "", "§aCliquez §7pour changer", "§7le nom du grade"),
                IGui.getItem("grade4", RANK4_SLOT, Material.IRON_INGOT, "", "§aCliquez §7pour changer", "§7le nom du grade"));
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 37, 38, 39, 40, 41, 42, 43, 44);
    }
    
    @Override
    public void onCreate(){
        setRank(0);
        setRank(1);
        setRank(2);
        setRank(3);
    }
    
    public void setRank(int index){
        City city = getData();
        switch(index){
            case 0:
                setDisplayName(RANK1_SLOT, "§c" + city.getRankName(0));
                break;
            case 1:
                setDisplayName(RANK2_SLOT, "§b" + city.getRankName(1));
                break;
            case 2:
                setDisplayName(RANK3_SLOT, "§e" + city.getRankName(2));
                break;
            case 3:
                setDisplayName(RANK4_SLOT, "§7" + city.getRankName(3));
                break;
        }
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        switch(event.getSlot()){
            case RANK1_SLOT:
            case RANK2_SLOT:
            case RANK3_SLOT:
            case RANK4_SLOT:
                SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
                City city = getData();
                GuiManager.getInstance().userInput(player.getPlayer(),
                        input -> {
                            if(input.isEmpty()){
                                player.sendMessage("§cLe grade entré n'est pas valide.");
                                return;
                            }
                            city.setRankName(event.getSlot() - 19 - ((event.getSlot() - 19) >> 1), input);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(ConsulatCore.getInstance(), () -> {
                                open(player);
                            });
                        },
                        new String[]{"", "^^^^^^^^^^^^^^", "Entrez le", "nom du grade"}, 0);
                break;
        }
    }
}
