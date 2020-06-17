package fr.amisoz.consulatcore.guis.city;

import fr.amisoz.consulatcore.guis.city.changehome.ChangeHomeGui;
import fr.amisoz.consulatcore.guis.city.claimlist.ListCityClaimsGui;
import fr.amisoz.consulatcore.guis.city.memberpermissions.ListMembersGui;
import fr.amisoz.consulatcore.guis.city.rank.RankGui;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.zones.ZoneManager;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.leconsulat.api.gui.Gui;
import fr.leconsulat.api.gui.GuiContainer;
import fr.leconsulat.api.gui.PagedGui;
import fr.leconsulat.api.gui.events.GuiClickEvent;
import fr.leconsulat.api.gui.events.GuiCreateEvent;
import fr.leconsulat.api.gui.events.GuiOpenEvent;
import fr.leconsulat.api.player.ConsulatPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CityGui extends GuiContainer<City> {
    
    private static final byte CLAIM_BUTTON = 19;
    private static final byte HOME_BUTTON = 21;
    private static final byte BANK_BUTTON = 23;
    private static final byte RANK_BUTTON = 25;
    private static final byte PERMISSION_SLOT = 40;
    
    private ListCityClaimsGui listCityClaimsGui = new ListCityClaimsGui();
    private ChangeHomeGui changeHomeGui = new ChangeHomeGui();
    private ListMembersGui membersGui = new ListMembersGui();
    private RankGui rankGui = new RankGui();
    
    public CityGui(){
        super(6);
        setTemplate("<ville>",
                getItem("§e<nom>", 4, Material.PAPER),
                getItem("§eClaims", CLAIM_BUTTON, Material.FILLED_MAP),
                getItem("§eHome", HOME_BUTTON, Material.COMPASS),
                getItem("§eBanque", BANK_BUTTON, Material.SUNFLOWER),
                getItem("§eGrades", RANK_BUTTON, Material.OAK_SIGN),
                getItem("§ePermissions", PERMISSION_SLOT, Material.BARRIER))
                .setDeco(Material.BLACK_STAINED_GLASS_PANE, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53)
                .setDeco(Material.RED_STAINED_GLASS_PANE, 0, 1, 2, 3, 5, 6, 7, 8);
    }
    
    @Override
    public void onCreate(GuiCreateEvent<City> event){
        Gui<City> gui = event.getGui();
        City city = event.getData();
        setCityName(gui, city);
        setHome(gui, city, false);
        setBank(gui, city);
        setRanks(gui, city);
        gui.prepareChild(listCityClaimsGui, () -> listCityClaimsGui.createGui(city, gui));
        gui.prepareChild(membersGui, () -> membersGui.createGui(city, gui));
        gui.prepareChild(changeHomeGui, () -> changeHomeGui.createGui(city, gui));
        gui.prepareChild(rankGui, () -> rankGui.createGui(city, gui));
    }
    
    @Override
    public void onOpen(GuiOpenEvent<City> event){
        SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
        Claim claim = player.getClaim();
        City city = event.getData();
        setHome(event.getGui(), city, claim != null && city.isClaim(claim));
    }
    
    @Override
    public void onClick(GuiClickEvent<City> event){
        Gui<City> current = event.getGui();
        switch(event.getSlot()){
            case CLAIM_BUTTON:{
                current.getChild(listCityClaimsGui).open(event.getPlayer());
            }
            break;
            case HOME_BUTTON:{
                City city = event.getData();
                SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
                Claim claim = player.getClaim();
                if(claim == null || !city.isClaim(claim)){
                    return;
                }
                if(city.getHome() == null){
                    ZoneManager.getInstance().setHome(city, player.getPlayer().getLocation());
                    player.sendMessage("§aTu as déplacé le home de ta ville.");
                } else {
                    confirmSethome(event.getGui(), player);
                }
            }
            break;
            case RANK_BUTTON:{
                current.getChild(rankGui).open(event.getPlayer());
            }
            break;
            case PERMISSION_SLOT:
                current.getChild(membersGui).open(event.getPlayer());
                break;
            
        }
    }
    
    
    /**
     * Ajoute un item représentant un claim à la liste des claims d'une ville,
     * pour permettre de gérer les joueurs ayant accès à ce claim
     *
     * @param city  La ville où l'on ajoute le claim
     * @param claim Le claim à ajouter au PagedGui
     */
    public void addClaimToList(City city, Claim claim){
        Gui<City> cityGui = getGui(city);
        if(cityGui == null){
            return;
        }
        listCityClaimsGui.addItemClaim(cityGui.getChild(listCityClaimsGui), claim);
    }
    
    /**
     * Enlève l'item représentant un claim à la liste des claims d'une ville
     *
     * @param city  La ville où l'on retire le claim
     * @param claim Le claim à retirer du PagedGui
     */
    public void removeClaimFromList(City city, Claim claim){
        Gui<City> cityGui = getGui(city);
        if(cityGui == null){
            return;
        }
        listCityClaimsGui.removeItemClaim(cityGui.getChild(listCityClaimsGui), claim);
    }
    
    public void addPlayerCityPermissions(City city, UUID uuid, String name){
        Gui<City> cityGui = getGui(city);
        if(cityGui == null){
            return;
        }
        membersGui.addPlayer(cityGui.getChild(membersGui), uuid, name);
    }
    
    public void removePlayerCityPermissions(City city, UUID uuid){
        Gui<City> cityGui = getGui(city);
        if(cityGui == null){
            return;
        }
        membersGui.removePlayer(cityGui.getChild(membersGui), uuid);
    }
    
    public void openListClaim(ConsulatPlayer player, City city){
        getGui(city).getChild(city).open(player);
    }
    
    public void confirmSethome(City city, ConsulatPlayer player){
        confirmSethome(getGui(city), player);
    }
    
    private void confirmSethome(Gui<City> current, ConsulatPlayer player){
        current.getChild(changeHomeGui).open(player);
    }
    
    public void setCityName(@NotNull City city){
        setCityName(getGui(city), city);
    }
    
    private void setCityName(@NotNull Gui<City> gui, @NotNull City city){
        PagedGui<City> mainGui = gui.getPage();
        mainGui.setName("§2" + city.getName() + "§8");
        mainGui.setDisplayName(4, "§e" + city.getName());
        mainGui.setDescription(4, "§7Propriétaire: §a" + Bukkit.getOfflinePlayer(city.getOwner()).getName());
    }
    
    public void setHome(@NotNull City city, boolean allow){
        setHome(getGui(city), city, allow);
    }
    
    private void setHome(@NotNull Gui<City> gui, @NotNull City city, boolean allow){
        Location home = city.getHome();
        PagedGui<City> mainGui = gui.getPage();
        if(home == null){
            if(allow){
                mainGui.setDescription(HOME_BUTTON, "§7Aucun home défini", "",
                        "§7§oDéfinir le home", "§7§o/ville sethome", "",
                        "§7Ou §aCliquez §7pour", "§7définir le home §aici");
            } else {
                mainGui.setDescription(HOME_BUTTON, "§7Aucun home défini");
            }
        } else {
            if(allow){
                mainGui.setDescription(HOME_BUTTON, "§7x: " + home.getBlockX(), "§7y: " + home.getBlockY(), "§7z: " + home.getBlockZ(), "",
                        "§7§oChanger le home", "§7§o/ville sethome", "",
                        "§7Ou §aCliquez §7pour", "§7définir le home §aici");
            } else {
                mainGui.setDescription(HOME_BUTTON, "§7x: " + home.getBlockX(), "§7y: " + home.getBlockY(), "§7z: " + home.getBlockZ());
            }
        }
    }
    
    public void setBank(@NotNull City city){
        setBank(getGui(city), city);
    }
    
    private void setBank(@NotNull Gui<City> gui, @NotNull City city){
        gui.getPage().setDescription(BANK_BUTTON, "§e" + city.getMoney(), "", "§7§oAjouter de l'argent", "§7§o/ville banque add <montant>");
    }
    
    public void setRanks(@NotNull City city){
        setRanks(getGui(false, city), city);
    }
    
    private void setRanks(@Nullable Gui<City> gui, @NotNull City city){
        if(gui == null){
            return;
        }
        gui.getPage().setDescription(RANK_BUTTON, "§7" + city.getRank(0), "§7" + city.getRank(1), "§7" + city.getRank(2), "",
                "§7Pour définir les", "§7grades, §acliquez §7ici");
    }
    
    public ListCityClaimsGui getListCityClaimsGui(){
        return listCityClaimsGui;
    }
    
    public ChangeHomeGui getChangeHomeGui(){
        return changeHomeGui;
    }
    
    public ListMembersGui getMembersGui(){
        return membersGui;
    }
    
    public RankGui getRankGui(){
        return rankGui;
    }
}
