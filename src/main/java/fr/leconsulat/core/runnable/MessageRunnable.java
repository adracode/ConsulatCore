package fr.leconsulat.core.runnable;

import fr.leconsulat.core.ConsulatCore;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class MessageRunnable implements Runnable {
    
    private List<String> messageList = new ArrayList<>();
    private int index = 0;
    
    public MessageRunnable(){
        messageList.add("N'oublie pas de t'inscrire sur le site, pour y accéder tu peux faire /site.");
        messageList.add("N'oublie pas de rejoindre le discord via le /discord.");
        messageList.add("Le /help est ton ami si tu as une question");
        messageList.add("Cheater et insulter ne sert à rien !");
        messageList.add("Tu peux acheter et vendre des items à l'admin shop au spawn !");
        messageList.add("Tu peux vendre et acheter des items à d'autres joueurs, /shop help !");
        messageList.add("Le grade Touriste est achetable avec l'argent IG ! Il te donne plusieurs avantages, comme l'accès au serveur plein, par exemple.");
        messageList.add("Le /safari te permet d'aller chasser les différents mobs hostiles qui existent ici... idéal pour revendre leurs loots ! ");
        messageList.add("L'end sera activé quelques jours après l'ouverture, tiens toi au courant sur le Discord !");
        messageList.add("L'end et le nether seront reset au moins une fois par mois, inutile de trop construire là-bas.");
    }
    
    @Override
    public void run(){
        String message = messageList.get(index);
        Bukkit.broadcastMessage("§7[§6Annonce§7]§6 " + message);
        index++;
        if(messageList.size() == index){
            index = 0;
        }
    }
}
