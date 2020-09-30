package fr.leconsulat.core.guis.pvp;

import fr.leconsulat.api.gui.GuiHeadItem;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.event.GuiCreateEvent;
import fr.leconsulat.api.gui.event.GuiOpenEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.MainPage;
import fr.leconsulat.api.gui.gui.module.api.Pageable;
import fr.leconsulat.api.gui.gui.template.PagedGui;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.players.SurvivalPlayer;
import org.bukkit.Material;

import java.util.Iterator;

public class PVPGui extends PagedGui {
    
    private static final byte PLAYER_STATUS = 49;
    private static final GuiItem ON = IGui.getItem("§aActivé", PLAYER_STATUS, Material.GREEN_CONCRETE,
            "§7Tu peux §acombattre§7 !",
            "",
            "§7§oClique pour désactiver"
    );
    private static final GuiItem OFF = IGui.getItem("§cDésactivé", PLAYER_STATUS, Material.RED_CONCRETE,
            "§7Tu ne peux pas §ccombattre§7 !",
            "",
            "§7§oClique pour activer",
            "",
            "§cTu perds ton stuff si",
            "§ctu meurs ou tu déco",
            "§cen combat !");
    
    public PVPGui(){
        super("PvP", 6,
                IGui.getItem("§ePvP", 4, Material.DIAMOND_SWORD,
                        "§7Le PvP est actif partout, ",
                        "§7sauf au spawn ! ",
                        "",
                        "§7Tu peux seulement combattre ",
                        "§7des joueurs qui l'ont aussi ",
                        "§7activé, et vice-versa.",
                        "",
                        "§cSi tu perds ton stuff,",
                        "§cc'est de ta faute. ",
                        "",
                        "§7Tu ne dois pas deco ",
                        "§7en combat (1 min)",
                        "",
                        "§cTu ne peux pas te",
                        "§ctéléporter (/spawn, /tpa...)",
                        "§cen combat !").setGlowing(true),
                IGui.getItem("§eStatus", PLAYER_STATUS, Material.GRAY_CONCRETE, "§7Récupération du statut...")
        );
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 50, 51, 52, 53);
        setDynamicItems(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
        setTemplateItems(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53);
    }
    
    @Override
    public void onPageCreated(GuiCreateEvent event, Pageable page){
        if(page.getPage() != 0){
            IGui gui = page.getGui();
            gui.setItem(IGui.getItem("§7Précédent", 47, Material.ARROW));
            getPage(page.getPage() - 1).getGui().setItem(IGui.getItem("§7Suivant", 51, Material.ARROW));
            gui.setDeco(Material.BLACK_STAINED_GLASS_PANE, 51);
        }
    }
    
    @Override
    public void onPageOpen(GuiOpenEvent event, Pageable pageGui){
        SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
        if(player.isInCombat()){
            player.sendMessage(Text.IN_COMBAT);
            event.setCancelled(true);
            return;
        }
        pageGui.getGui().setFakeItem(PLAYER_STATUS, player.isPvp() ? ON : OFF, player);
    }
    
    @Override
    public void onPageClick(GuiClickEvent event, Pageable page){
        switch(event.getSlot()){
            case PLAYER_STATUS:
                SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
                player.setPvp(!player.isPvp());
                break;
            case 47:{
                GuiItem clickedItem = page.getGui().getItem(event.getSlot());
                if(clickedItem.getType() == Material.ARROW){
                    getPage(page.getPage() - 1).getGui().open(event.getPlayer());
                }
            }
            break;
            case 51:{
                GuiItem clickedItem = page.getGui().getItem(event.getSlot());
                if(clickedItem.getType() == Material.ARROW){
                    getPage(page.getPage() + 1).getGui().open(event.getPlayer());
                }
            }
            break;
        }
    }
    
    public void switchPvp(SurvivalPlayer player){
        if(player.isPvp()){
            addPlayer(player);
        } else {
            removePlayer(player);
        }
        IGui currentlyOpen = player.getCurrentlyOpen();
        if(currentlyOpen instanceof Pageable){
            MainPage mainPage = ((Pageable)currentlyOpen).getMainPage();
            if(mainPage.getGui() instanceof PVPGui){
                onPageOpen(new GuiOpenEvent(player), (Pageable)currentlyOpen);
            }
        }
    }
    
    public void addPlayer(ConsulatPlayer player){
        GuiItem item = new GuiHeadItem(player.getUUID(), this).
                setDisplayName("§e" + player.getName());
        item.setAttachedObject(player);
        addItem(item);
    }
    
    public void removePlayer(ConsulatPlayer player){
        for(Iterator<GuiItem> iterator = this.iterator(); iterator.hasNext(); ){
            if(player.equals(iterator.next().getAttachedObject())){
                iterator.remove();
                break;
            }
        }
    }
}
