package fr.amisoz.consulatcore.guis.city;

import fr.amisoz.consulatcore.guis.GuiListenerStorage;
import fr.amisoz.consulatcore.guis.city.changehome.ChangeHomeGui;
import fr.amisoz.consulatcore.guis.city.claimlist.ClaimsGui;
import fr.amisoz.consulatcore.guis.city.members.MembersGui;
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
    
    private static final byte CITY_SLOT = 4;
    private static final byte CLAIM_BUTTON = 19;
    private static final byte HOME_BUTTON = 21;
    private static final byte BANK_BUTTON = 23;
    private static final byte RANK_BUTTON = 25;
    private static final byte PERMISSION_SLOT = 40;
    
    public static final String CLAIMS = "city.claims";
    public static final String HOME = "city.home";
    public static final String MEMBERS = "city.members";
    public static final String RANKS = "city.ranks";
    
    private ClaimsGui claims = new ClaimsGui();
    private ChangeHomeGui home = new ChangeHomeGui();
    private MembersGui members = new MembersGui();
    private RankGui ranks = new RankGui();
    
    public CityGui(){
        super(6);
        setTemplate("<ville>",
                getItem("§e<nom>", CITY_SLOT, Material.PAPER),
                getItem("§eClaims", CLAIM_BUTTON, Material.FILLED_MAP, "", "§7Gérer les claims", "§7de la ville"),
                getItem("§eHome", HOME_BUTTON, Material.COMPASS),
                getItem("§eBanque", BANK_BUTTON, Material.SUNFLOWER),
                getItem("§eGrades", RANK_BUTTON, Material.OAK_SIGN),
                getItem("§ePermissions", PERMISSION_SLOT, Material.PLAYER_HEAD, "", "§7Gérer les membres", "§7de la ville"))
                .setDeco(Material.BLACK_STAINED_GLASS_PANE, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53)
                .setDeco(Material.RED_STAINED_GLASS_PANE, 0, 1, 2, 3, 5, 6, 7, 8);
        GuiListenerStorage storage = GuiListenerStorage.getInstance();
        storage.addListener(CLAIMS, claims);
        storage.addListener(MEMBERS, members);
        storage.addListener(HOME, home);
        storage.addListener(RANKS, ranks);
    }
    
    @Override
    public void onCreate(GuiCreateEvent<City> event){
        Gui<City> gui = event.getGui();
        City city = event.getData();
        updateName(gui);
        gui.getPage().setDescription(CITY_SLOT, "§7Propriétaire: §a" + Bukkit.getOfflinePlayer(city.getOwner()).getName());
        updateHome(gui, false);
        updateBank(gui);
        updateRank(gui);
        gui.prepareChild(CLAIMS, () -> claims.createGui(city, gui));
        gui.prepareChild(MEMBERS, () -> members.createGui(city, gui));
        gui.prepareChild(HOME, () -> home.createGui(city, gui));
        gui.prepareChild(RANKS, () -> ranks.createGui(city, gui));
    }
    
    @Override
    public void onOpen(GuiOpenEvent<City> event){
        SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
        Claim claim = player.getClaim();
        City city = event.getData();
        updateHome(event.getGui(), claim != null && city.isClaim(claim));
    }
    
    @Override
    public void onClick(GuiClickEvent<City> event){
        Gui<City> current = event.getGui();
        switch(event.getSlot()){
            case CLAIM_BUTTON:{
                current.getChild(CLAIMS).open(event.getPlayer());
            }
            break;
            case HOME_BUTTON:{
                City city = event.getData();
                SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
                Claim claim = player.getClaim();
                if(claim == null || !city.isClaim(claim)){
                    return;
                }
                if(!city.hasHome()){
                    ZoneManager.getInstance().setHome(city, player.getPlayer().getLocation());
                    player.sendMessage("§aTu as déplacé le home de ta ville.");
                } else {
                    confirmSethome(event.getGui(), player);
                }
            }
            break;
            case RANK_BUTTON:{
                current.getChild(RANKS).open(event.getPlayer());
            }
            break;
            case PERMISSION_SLOT:{
                current.getChild(MEMBERS).open(event.getPlayer());
            }
            break;
        }
    }
    
    public void updateName(@NotNull City city){
        updateName(getGui(city));
    }
    
    private void updateName(@NotNull Gui<City> gui){
        PagedGui<City> current = gui.getPage();
        City city = gui.getData();
        current.setName("§2" + city.getName() + "§8");
        current.setDisplayName(CITY_SLOT, "§e" + city.getName());
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
        claims.removeItemClaim(cityGui.getChild(CLAIMS), claim);
    }
    
    public void removePlayerCityPermissions(City city, UUID uuid){
        Gui<City> cityGui = getGui(false, city);
        if(cityGui == null){
            return;
        }
        members.removePlayer(cityGui.getChild(MEMBERS), uuid);
    }
    
    public void confirmSethome(City city, ConsulatPlayer player){
        confirmSethome(getGui(city), player);
    }
    
    private void confirmSethome(Gui<City> current, ConsulatPlayer player){
        current.getChild(HOME).open(player);
    }
    
    public void updateHome(@NotNull City city, boolean allow){
        updateHome(getGui(city), allow);
    }
    
    private void updateHome(@NotNull Gui<City> gui, boolean allow){
        PagedGui<City> current = gui.getPage();
        City city = gui.getData();
        if(!city.hasHome()){
            if(allow){
                current.setDescription(HOME_BUTTON, "§7Aucun home défini", "",
                        "§7§oDéfinir le home", "§7§o/ville sethome", "",
                        "§7Ou §acliquez §7pour", "§7définir le home §aici");
            } else {
                current.setDescription(HOME_BUTTON, "§7Aucun home défini");
            }
        } else {
            Location home = city.getHome();
            if(allow){
                current.setDescription(HOME_BUTTON, "§7x: " + home.getBlockX(), "§7y: " + home.getBlockY(), "§7z: " + home.getBlockZ(), "",
                        "§7§oChanger le home", "§7§o/ville sethome", "",
                        "§7Ou §acliquez §7pour", "§7définir le home §aici");
            } else {
                current.setDescription(HOME_BUTTON, "§7x: " + home.getBlockX(), "§7y: " + home.getBlockY(), "§7z: " + home.getBlockZ());
            }
        }
    }
    
    public void updateBank(@NotNull City city){
        updateBank(getGui(city));
    }
    
    private void updateBank(@NotNull Gui<City> gui){
        City city = gui.getData();
        gui.getPage().setDescription(BANK_BUTTON, "§e" + city.getMoney(), "", "§7§oAjouter de l'argent", "§7§o/ville banque add <montant>");
    }
    
    public void updateRank(@NotNull City city){
        updateRank(getGui(false, city));
    }
    
    private void updateRank(@Nullable Gui<City> gui){
        if(gui == null){
            return;
        }
        City city = gui.getData();
        gui.getPage().setDescription(RANK_BUTTON,"",
                "§b" + city.getRankName(0),
                "§e" + city.getRankName(1),
                "§7" + city.getRankName(2), "",
                "§7Pour définir les", "§7grades, §acliquez §7ici");
    }
}
