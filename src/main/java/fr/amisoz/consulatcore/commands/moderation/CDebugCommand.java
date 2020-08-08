package fr.amisoz.consulatcore.commands.moderation;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.amisoz.consulatcore.chunks.ChunkManager;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

import java.util.UUID;

public class CDebugCommand extends ConsulatCommand {
    
    private UUID uuid = UUID.fromString("43da311c-d869-4e88-9b78-f1d4fc193ed4");
    
    public CDebugCommand(){
        super("consulat.core", "cdebug", "/cdebug", 0, Rank.ADMIN);
        suggest(listener -> {
                    ConsulatPlayer player = getConsulatPlayer(listener);
                    return player != null && player.getUUID().equals(uuid);
                },
                LiteralArgumentBuilder.literal("chunk"));
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        if(!sender.getUUID().equals(uuid)){
            return;
        }
        if(args.length > 0){
            switch(args[0]){
                case "chunk":
                    sender.sendMessage(ChunkManager.getInstance().getChunk(sender.getPlayer().getChunk()).toString());
                    break;
            }
        }
    }
}
