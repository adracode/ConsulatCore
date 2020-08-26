package fr.amisoz.consulatcore.guis.city.ranks.rank;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.CityPermission;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.cities.CityRank;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.Datable;
import fr.leconsulat.api.gui.gui.template.DataRelatGui;
import org.bukkit.Bukkit;
import org.bukkit.Material;

public class RankGui extends DataRelatGui<CityRank> {
    
    private static final byte INFO_SLOT = 4;
    private static final byte RENAME_SLOT = 13;
    private static final byte MEMBER_SLOT = 29;
    private static final byte CLAIM_SLOT = 30;
    private static final byte ACCESS_SLOT = 31;
    private static final byte BANK_SLOT = 32;
    private static final byte HOME_SLOT = 33;
    
    public RankGui(CityRank rank){
        super(rank, rank.getColor() + rank.getRankName(), 6,
                IGui.getItem("§eGrade", INFO_SLOT, Material.PAPER, "",
                        "§7Les grades définissent des", "§7permissions par défaut aux", "§7membres", "",
                        "§7Les permissions des membres", "§7peuvent être modifiées", "§7par la suite", "",
                        "§7§oSi les permissions par", "§7§odéfaut d'un grade sont modifiées,", "§7§oles permissions des membres", "§7§one sont pas modifiées"),
                IGui.getItem("§eChanger le nom", RENAME_SLOT, Material.OAK_SIGN, "", "§7Changer le nom", "§7du grade"),
                IGui.getItem("§eMembres", MEMBER_SLOT, Material.PLAYER_HEAD, "", "§7Inviter un joueur", "§7Kick un membre"),
                IGui.getItem("§cDésactivé", MEMBER_SLOT + 9, Material.RED_CONCRETE),
                IGui.getItem("§eClaims", CLAIM_SLOT, Material.FILLED_MAP, "", "§7Claim un chunk", "§7Unclaim un chunk"),
                IGui.getItem("§cDésactivé", CLAIM_SLOT + 9, Material.RED_CONCRETE),
                IGui.getItem("§eAccès", ACCESS_SLOT, Material.BARRIER, "", "§7Gérer les accès aux chunks"),
                IGui.getItem("§cDésactivé", ACCESS_SLOT + 9, Material.RED_CONCRETE),
                IGui.getItem("§eBanque", BANK_SLOT, Material.SUNFLOWER, "", "§7Gérer la banque"),
                IGui.getItem("§cDésactivé", BANK_SLOT + 9, Material.RED_CONCRETE),
                IGui.getItem("§eHome", HOME_SLOT, Material.COMPASS, "", "§7Déplacer le home"),
                IGui.getItem("§cDésactivé", HOME_SLOT + 9, Material.RED_CONCRETE)
        );
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 46, 47, 48, 49, 50, 51, 52, 53);
    }
    
    @Override
    public void onCreate(){
        CityRank rank = getData();
        if(rank.getId() == 0){
            setDescription(MEMBER_SLOT + 9, "", "§cCette permission ne peut", "§cpas être modifiée", "§csur ce grade");
            setDescription(CLAIM_SLOT + 9, "", "§cCette permission ne peut", "§cpas être modifiée", "§csur ce grade");
            setDescription(ACCESS_SLOT + 9, "", "§cCette permission ne peut", "§cpas être modifiée", "§csur ce grade");
            setDescription(BANK_SLOT + 9, "", "§cCette permission ne peut", "§cpas être modifiée", "§csur ce grade");
            setDescription(HOME_SLOT + 9, "", "§cCette permission ne peut", "§cpas être modifiée", "§csur ce grade");
        }
        for(CityPermission permission : CityPermission.values()){
            byte slot = getSlotPermission(permission);
            if(slot == -1){
                continue;
            }
            if(rank.hasPermission(permission)){
                setGlowing(slot, true);
                setType(slot + 9, Material.GREEN_CONCRETE);
                setDisplayName(slot + 9, "§aActivé");
            } else {
                setGlowing(slot, false);
                setType(slot + 9, Material.RED_CONCRETE);
                setDisplayName(slot + 9, "§cDésactivé");
            }
        }
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        CityPermission permission = null;
        switch(event.getSlot()){
            case RENAME_SLOT:
                SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
                City city = getCity();
                GuiManager.getInstance().userInput(player,
                        input -> {
                            if(!City.VALID_RANK.matcher(input).matches()){
                                player.sendMessage(Text.INVALID_RANK);
                                return;
                            }
                            city.setRankName(getData().getId(), input);
                            Bukkit.getScheduler().runTask(ConsulatCore.getInstance(), () -> {
                                open(player);
                            });
                        },
                        new String[]{"", "^^^^^^^^^^^^^^", "Entre le", "nom du grade"}, 0);
                return;
            case MEMBER_SLOT:
            case MEMBER_SLOT + 9:
                permission = CityPermission.MANAGE_PLAYER;
                break;
            case CLAIM_SLOT:
            case CLAIM_SLOT + 9:
                permission = CityPermission.MANAGE_CLAIM;
                break;
            case ACCESS_SLOT:
            case ACCESS_SLOT + 9:
                permission = CityPermission.MANAGE_ACCESS;
                break;
            case BANK_SLOT:
            case BANK_SLOT + 9:
                permission = CityPermission.MANAGE_BANK;
                break;
            case HOME_SLOT:
            case HOME_SLOT + 9:
                permission = CityPermission.MANAGE_HOME;
                break;
        }
        if(permission == null || getData().getId() == 0){
            return;
        }
        switchPermission(permission);
    }
    
    public void updateName(){
        CityRank rank = getData();
        setName(rank.getColor() + rank.getRankName());
    }
    
    @SuppressWarnings("unchecked")
    public City getCity(){
        return ((Datable<City>)getFather()).getData();
    }
    
    private byte getSlotPermission(CityPermission permission){
        switch(permission){
            case MANAGE_PLAYER:
                return MEMBER_SLOT;
            case MANAGE_CLAIM:
                return CLAIM_SLOT;
            case MANAGE_ACCESS:
                return ACCESS_SLOT;
            case MANAGE_BANK:
                return BANK_SLOT;
            case MANAGE_HOME:
                return HOME_SLOT;
        }
        return -1;
    }
    
    private void switchPermission(CityPermission permission){
        setPermission(!getData().hasPermission(permission), permission);
    }
    
    private void setPermission(boolean activate, CityPermission permission){
        byte slot = getSlotPermission(permission);
        if(slot == -1){
            return;
        }
        if(activate){
            getData().addPermission(permission);
            setGlowing(slot, true);
            setType(slot + 9, Material.GREEN_CONCRETE);
            setDisplayName(slot + 9, "§aActivé");
        } else {
            getData().removePermission(permission);
            setGlowing(slot, false);
            setType(slot + 9, Material.RED_CONCRETE);
            setDisplayName(slot + 9, "§cDésactivé");
        }
    }
}
