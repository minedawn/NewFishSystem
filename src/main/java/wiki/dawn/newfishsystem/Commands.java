package wiki.dawn.newfishsystem;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;

public class Commands implements CommandExecutor {
    private final NewFishSystem plugin;

    public Commands(NewFishSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§4你没有权限执行该命令！");
            return true;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (plugin.fishingState.isEmpty()) {
                    plugin.config.reloadConfig();
                    sender.sendMessage(ChatColor.AQUA + "[原钓] >> 重新载入完成！如有异常请留意后台是否报错！");
                    return true;
                } else {
                    sender.sendMessage("§4[原钓] >> 当前有玩家正在钓鱼，无法重载插件！");
                }
            } else sendHelp(sender);
        } else sendHelp(sender);
        return true;
    }

    void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "[原钓] >> ------------------------------");
        sender.sendMessage(ChatColor.AQUA + "[原钓] >> /nfs help : 提示");
        sender.sendMessage(ChatColor.AQUA + "[原钓] >> /nfs reload : 重载插件");
        sender.sendMessage(ChatColor.AQUA + "[原钓] >> ------------------------------");
    }
}