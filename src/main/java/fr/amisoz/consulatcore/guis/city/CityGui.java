package fr.amisoz.consulatcore.guis.city;

import fr.amisoz.consulatcore.guis.city.changehome.ChangeHomeGui;
import fr.amisoz.consulatcore.guis.city.claimlist.ClaimsGui;
import fr.amisoz.consulatcore.guis.city.members.MembersGui;
import fr.amisoz.consulatcore.guis.city.rank.RankGui;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.zones.ZoneManager;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.leconsulat.api.gui.GuiContainer;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.event.GuiOpenEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.Datable;
import fr.leconsulat.api.gui.gui.module.api.Relationnable;
import fr.leconsulat.api.gui.gui.template.DataRelatGui;
import fr.leconsulat.api.player.ConsulatPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

public class CityGui extends DataRelatGui<City> {
    
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
    
    public CityGui(City city){
        super(city, "<ville>", 6,
                IGui.getItem("§e<nom>", CITY_SLOT, Material.PAPER),
                IGui.getItem("§eClaims", CLAIM_BUTTON, Material.FILLED_MAP, "", "§7Gérer les claims", "§7de la ville"),
                IGui.getItem("§eHome", HOME_BUTTON, Material.COMPASS),
                IGui.getItem("§eBanque", BANK_BUTTON, Material.SUNFLOWER),
                IGui.getItem("§eGrades", RANK_BUTTON, Material.OAK_SIGN),
                IGui.getItem("§eMembres", PERMISSION_SLOT, Material.PLAYER_HEAD, "", "§7Gérer les membres", "§7de la ville"));
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53);
        setDeco(Material.RED_STAINED_GLASS_PANE, 0, 1, 2, 3, 5, 6, 7, 8);
    }
    
    @Override
    public void onCreate(){
        updateName();
        setDescription(CITY_SLOT, "§7Propriétaire: §a" + Bukkit.getOfflinePlayer(getData().getOwner()).getName());
        updateHome(false);
        updateBank();
        updateRank();
    }
    
    @Override
    public Relationnable createChild(@Nullable Object key){
        if(!(key instanceof String)){
            throw new IllegalArgumentException();
        }
        switch((String)key){
            case CLAIMS:
                return new ClaimsGui(getData());
            case HOME:
                return new ChangeHomeGui(getData());
            case MEMBERS:
                return new MembersGui(getData());
            case RANKS:
                return new RankGui(getData());
        }
        throw new IllegalArgumentException();
    }
    
    @Override
    public void onOpen(GuiOpenEvent event){
        Claim claim = ((SurvivalPlayer)event.getPlayer()).getClaim();
        City city = getData();
        updateHome(claim != null && city.isClaim(claim));
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        switch(event.getSlot()){
            case CLAIM_BUTTON:{
                getChild(CLAIMS).open(event.getPlayer());
            }
            break;
            case HOME_BUTTON:{
                City city = getData();
                SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
                Claim claim = player.getClaim();
                if(claim == null || !city.isClaim(claim)){
                    return;
                }
                if(!city.hasHome()){
                    ZoneManager.getInstance().setHome(city, player.getPlayer().getLocation());
                    player.sendMessage("§aTu as déplacé le home de ta ville.");
                } else {
                    confirmSethome(player);
                }
            }
            break;
            case RANK_BUTTON:{
                getChild(RANKS).open(event.getPlayer());
            }
            break;
            case PERMISSION_SLOT:{
                getChild(MEMBERS).open(event.getPlayer());
            }
            break;
        }
    }
    
    public void updateName(){
        City city = getData();
        setName("§2" + city.getName() + "§8");
        setDisplayName(CITY_SLOT, "§e" + city.getName());
    }
    
    public void confirmSethome(ConsulatPlayer player){
        getChild(HOME).open(player);
    }
    
    public void updateHome(boolean allow){
        City city = getData();
        if(!city.hasHome()){
            if(allow){
                setDescription(HOME_BUTTON, "§7Aucun home défini", "",
                        "§7§oDéfinir le home", "§7§o/ville sethome", "",
                        "§7Ou §acliquez §7pour", "§7définir le home §aici");
            } else {
                setDescription(HOME_BUTTON, "§7Aucun home défini");
            }
        } else {
            Location home = city.getHome();
            if(allow){
                setDescription(HOME_BUTTON, "§7x: " + home.getBlockX(), "§7y: " + home.getBlockY(), "§7z: " + home.getBlockZ(), "",
                        "§7§oChanger le home", "§7§o/ville sethome", "",
                        "§7Ou §acliquez §7pour", "§7définir le home §aici");
            } else {
                setDescription(HOME_BUTTON, "§7x: " + home.getBlockX(), "§7y: " + home.getBlockY(), "§7z: " + home.getBlockZ());
            }
        }
    }
    
    public void updateBank(){
        City city = getData();
        setDescription(BANK_BUTTON, "§e" + city.getMoney(), "", "§7§oAjouter de l'argent", "§7§o/ville banque add <montant>");
    }
    
    public void updateRank(){
        City city = getData();
        setDescription(RANK_BUTTON, "",
                "§b" + city.getRankName(0),
                "§e" + city.getRankName(1),
                "§7" + city.getRankName(2), "",
                "§7Pour définir les", "§7grades, §acliquez §7ici");
    }
    
    public static class Container extends GuiContainer<City> {
        
        private static Container instance;
        
        public Container(){
            if(instance != null){
                throw new IllegalStateException();
            }
            instance = this;
            GuiManager.getInstance().addContainer("city", this);
        }
        
        @Override
        public Datable<City> createGui(City city){
            return new CityGui(city);
        }
    }
    
}
