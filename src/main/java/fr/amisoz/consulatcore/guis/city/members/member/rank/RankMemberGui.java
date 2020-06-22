package fr.amisoz.consulatcore.guis.city.members.member.rank;

import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.cities.CityPlayer;
import fr.amisoz.consulatcore.zones.cities.CityRank;
import fr.leconsulat.api.gui.Gui;
import fr.leconsulat.api.gui.GuiListener;
import fr.leconsulat.api.gui.PagedGui;
import fr.leconsulat.api.gui.events.GuiClickEvent;
import fr.leconsulat.api.gui.events.PagedGuiCreateEvent;
import org.bukkit.Material;

import java.util.UUID;

//TODO: changer le nom lors d'une update
public class RankMemberGui extends GuiListener<UUID> {
    
    private static final byte CURRENT_SLOT = 4;
    private static final byte RANK1_SLOT = 20;
    private static final byte RANK2_SLOT = 22;
    private static final byte RANK3_SLOT = 24;
    
    private static final String[] GIVE_RANK_DESCRIPTION = new String[]{"", "§aCliquez §7pour attribuer", "§7ce grade au joueur"};
    
    public RankMemberGui(){
        super(5);
        setTemplate("Grade",
                getItem("§eGrade actuel", CURRENT_SLOT, Material.NAME_TAG),
                getItem("<grade1>", RANK1_SLOT, Material.DIAMOND, GIVE_RANK_DESCRIPTION),
                getItem("<grade2>", RANK2_SLOT, Material.GOLD_INGOT, GIVE_RANK_DESCRIPTION),
                getItem("<grade3>", RANK3_SLOT, Material.IRON_INGOT, GIVE_RANK_DESCRIPTION)
        ).setDeco(Material.BLACK_STAINED_GLASS_PANE, 0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44);
        setDestroyOnClose(true);
    }
    
    private int slotByRank(CityRank rank){
        switch(rank.getId()){
            case 0:
                return RANK1_SLOT;
            case 1:
                return RANK2_SLOT;
            case 2:
                return RANK3_SLOT;
        }
        return -1;
    }
    
    @Override
    public void onPageCreate(PagedGuiCreateEvent<UUID> event){
        PagedGui<UUID> current = event.getPagedGui();
        City city = getCity(event.getGui());
        CityRank rank = city.getCityPlayer(event.getData()).getRank();
        int slot = slotByRank(rank);
        current.setGlowing(slot, true);
        current.setDescription(slot);
        current.setDescription(CURRENT_SLOT, "§a" + rank.getRankName());
        current.setDisplayName(RANK1_SLOT, "§b" + city.getRankName(0));
        current.setDisplayName(RANK2_SLOT, "§e" + city.getRankName(1));
        current.setDisplayName(RANK3_SLOT, "§7" + city.getRankName(2));
    }
    
    public void setRank(Gui<UUID> current, CityRank rank){
        City city = getCity(current);
        UUID uuid = current.getData();
        CityPlayer cityPlayer = city.getCityPlayer(uuid);
        CityRank currentRank = cityPlayer.getRank();
        int slot = slotByRank(currentRank);
        current.getPage().setGlowing(slot, false);
        current.getPage().setDescription(slot, GIVE_RANK_DESCRIPTION);
        current.getPage().setGlowing(slotByRank(rank), true);
        current.getPage().setDescription(CURRENT_SLOT, "§a" + rank.getRankName());
        current.getPage().setDescription(slotByRank(rank));
        cityPlayer.setRank(rank);
    }
    
    @Override
    public void onClick(GuiClickEvent<UUID> event){
        City city = getCity(event.getGui());
        CityRank rank = null;
        switch(event.getSlot()){
            case RANK1_SLOT:
                rank = city.getRank(0);
                break;
            case RANK2_SLOT:
                rank = city.getRank(1);
                break;
            case RANK3_SLOT:
                rank = city.getRank(2);
                break;
        }
        if(rank != null){
            setRank(event.getGui(), rank);
        }
    }
    
    private City getCity(Gui<UUID> gui){
        return (City)gui.getFather().getFather().getData();
    }
    
}
