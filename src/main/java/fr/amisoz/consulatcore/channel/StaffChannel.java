package fr.amisoz.consulatcore.channel;

import fr.leconsulat.api.channel.Channel;
import fr.leconsulat.api.channel.Speakable;
import fr.leconsulat.api.player.ConsulatPlayer;

public class StaffChannel  extends Channel implements Speakable {
    
    public StaffChannel(){
        super("staff");
    }
    
    @Override
    public String speak(ConsulatPlayer sender, String message){
        return "§2(Staff)§a " + sender.getName() + "§7 : " + message;
    }
}
