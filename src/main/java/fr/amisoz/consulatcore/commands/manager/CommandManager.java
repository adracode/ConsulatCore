package fr.amisoz.consulatcore.commands.manager;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.commands.moderation.*;
import fr.amisoz.consulatcore.commands.players.*;

public class CommandManager {

    public CommandManager(ConsulatCore consulatCore) {
        consulatCore.getCommand("report").setExecutor(new ReportCommand(consulatCore));
        consulatCore.getCommand("rank").setExecutor(new RankCommand(consulatCore));
        consulatCore.getCommand("sanction").setExecutor(new SanctionCommand());
        consulatCore.getCommand("annonce").setExecutor(new BroadcastCommand());
        consulatCore.getCommand("unban").setExecutor(new UnbanCommand(consulatCore));
        consulatCore.getCommand("unmute").setExecutor(new UnmuteCommand(consulatCore));
        consulatCore.getCommand("staff").setExecutor(new ModerateCommand(consulatCore));
        consulatCore.getCommand("tpmod").setExecutor(new TpmodCommand());
        consulatCore.getCommand("spawn").setExecutor(new SpawnCommand());
        consulatCore.getCommand("help").setExecutor(new HelpCommand());
        consulatCore.getCommand("mp").setExecutor(new MpCommand());
        consulatCore.getCommand("advert").setExecutor(new AdvertCommand());
        consulatCore.getCommand("kick").setExecutor(new KickCommand());
        consulatCore.getCommand("sc").setExecutor(new StaffChatCommand());
        consulatCore.getCommand("chat").setExecutor(new ToggleChatCommand());
        consulatCore.getCommand("gm").setExecutor(new GamemodeCommand());
        consulatCore.getCommand("back").setExecutor(new BackCommand());
        consulatCore.getCommand("stafflist").setExecutor(new StaffListCommand());
        consulatCore.getCommand("hub").setExecutor(new HubCommand());
        consulatCore.getCommand("r").setExecutor(new AnswerCommand());
        consulatCore.getCommand("news").setExecutor(new NewsCommand());
        consulatCore.getCommand("seen").setExecutor(new SeenCommand());


    }
}
