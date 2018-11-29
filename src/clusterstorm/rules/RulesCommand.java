package clusterstorm.rules;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class RulesCommand implements CommandExecutor {

	public static final String prefix = "§2§lRules §3> §f";
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	  FileConfiguration config = Rules.getInstance().getConfig();
	  
		if(args.length > 0 && sender.hasPermission("rules.options")) 
		{
      if(args[0].equalsIgnoreCase("-rl")) {
        sender.sendMessage(prefix + config.getString("messages.reload_start"));
        Rules.getInstance().reload();
        sender.sendMessage(prefix + config.getString("messages.reload_complete"));
        return true;
      }

      if(args[0].equalsIgnoreCase("-status")) {
        if (args.length == 2) {
          sender.sendMessage(prefix + config.getString("messages.player_status_" + (Rules.players().hasPlayer(args[1])? "accepted": "not_yet")).replace("{player}", args[1]));
          return true;
        }

        sender.sendMessage(prefix + config.getString("messages.wrong_usage_status"));
        return false;
      }

      if(args[0].equalsIgnoreCase("-accept")) {
        if (args.length == 2) {
          if (Rules.players().hasPlayer(args[1])) {
            sender.sendMessage(prefix + config.getString("messages.player_accept_already").replace("{player}", args[1]));
            return false;
          }
          
          Rules.players().writePlayer(args[1]);
          sender.sendMessage(prefix + config.getString("messages.player_accept_added").replace("{player}", args[1]));
          return true;
        }

        sender.sendMessage(prefix + config.getString("messages.wrong_usage_accept"));
        return false;
      }
			
			if(args[0].equalsIgnoreCase("-clear")) {
			  if (args.length == 2) {
          if (Rules.players().hasPlayer(args[1])) {
            Rules.players().clear(args[1]);
            sender.sendMessage(prefix + config.getString("messages.player_cleared").replace("{player}", args[1]));
            
            Player p = Bukkit.getPlayerExact(args[1]);
            if (p != null && p.isOnline()) {
              Rules.menu().openConfirm(p);
            }
            
            return true;
          };

          sender.sendMessage(prefix + config.getString("messages.player_status_not_yet").replace("{player}", args[1]));
          return false;
			  }
			  else {
          Rules.players().clear();
          sender.sendMessage(prefix + config.getString("messages.players_cleared"));
          
          for (Player p:Bukkit.getOnlinePlayers()) {
            if (!Rules.players().hasPlayer(p.getName())) {
              Rules.menu().openConfirm(p);
            }
          }
          return true;
			  }
			}
			
			if(args[0].equalsIgnoreCase("-reload")) {
				Bukkit.getScheduler().runTaskAsynchronously(Rules.getInstance(), new Runnable() {
					
					@Override
					public void run() {
						sender.sendMessage(prefix + config.getString("messages.reload_pastebin_start"));
						Rules.pastebin().reload();
						Rules.getInstance().reload();
						sender.sendMessage(prefix + config.getString("messages.reload_pastebin_complete"));
					}
				});
				return true;
			}
		}
		
		if(!sender.hasPermission("rules.rules")) {
			sender.sendMessage(config.getString("messages.insufficient_permissions"));
			return true;
		}
		
		Player p = null;
		if(args.length > 0 && sender.hasPermission("rules.rules.others")) 
		{
			p = Bukkit.getPlayerExact(args[0]);
			if(p == null) {
				sender.sendMessage(prefix + config.getString("messages.player_not_found").replace("{player}", args[0]));
				return true;
			}
		} else {
			if(sender instanceof Player) p = (Player) sender;
			else {
        sender.sendMessage(prefix + config.getString("messages.wrong_usage"));
				return true;
			}
			// Because if in a bed the player can drag and drop items into his inventory...			
			if (p.isSleeping()) {
        sender.sendMessage(config.getString("messages.prevent_while_sleeping"));
        return false;
			}
		}
		Rules.menu().openRules(p);
		
		return true;
	}
	
}
