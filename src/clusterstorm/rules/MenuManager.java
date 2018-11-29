package clusterstorm.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MenuManager {
	
	Inventory i, c;
	int accept, deny;
	
	String kickMessage;

	public MenuManager() {
		reload();
	}
	
	public void reload() {
		FileConfiguration config = Rules.getInstance().getConfig();
		String name = config.getString("inventory.name", "Rules").replace("&", "§");
		kickMessage = config.getString("kickMessage", "Disconnected").replace("&", "§");
		// Only recreate the inventory if not already set, prevent existing opened inventory to be empty after reload
		if (i == null) 
		  i = Bukkit.createInventory(null, config.getInt("inventory.simpleMenuRows") * 9, name);
		else i.clear();
    // Only recreate the inventory if not already set, prevent existing opened inventory to be empty after reload
		if (c == null)
		  c = Bukkit.createInventory(null, config.getInt("inventory.comfirmMenuRows") * 9, name);
		else c.clear(); 
		
		ConfigurationSection sec = config.getConfigurationSection("items");
		if(sec != null)
		{
			Set<String> keys = sec.getKeys(false);
			if(keys != null)
			{
				int lastSlot = 0;
				for (String key : keys)
				{
					ItemStack item = deserialize(config, "items." + key);
					if(item == null) continue;
					int slot = config.getInt("items." + key + ".slot", lastSlot++);
					this.i.setItem(slot, item);
					this.c.setItem(slot, item);
					
				}
			}
		}
		
		ItemStack a = deserialize(config, "accept");
		accept = config.getInt("accept.slot", 21);
		c.setItem(accept, a);
		
		a = deserialize(config, "deny");
		deny = config.getInt("accept.deny", 23);
		c.setItem(deny, a);
	}
	
	
	public void openRules(Player p) {
		p.openInventory(i);
	}
	
	public void openConfirm(Player p) {
		p.openInventory(c);
	}
	
	public void accept(Player p) {
		String player = p.getName();
		if(Rules.players().hasPlayer(player)) return;
		
		Rules.players().writePlayer(player);
		if(Rules.sound != null) p.playSound(p.getLocation(), Rules.sound, 1, 1);
		p.closeInventory();
		
		Bukkit.getPluginManager().callEvent(new RulesConfirmedEvent(p));
	}

	public void deny(Player p) {
		String player = p.getName();
		if(Rules.players().hasPlayer(player)) return;
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(Rules.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				p.kickPlayer(kickMessage);
			}
		});
	}
	
	
	
	private ItemStack deserialize(FileConfiguration c, String section) {
		String id = c.getString(section + ".id");
		if(id == null) return null;
		
		Material m;
		
		try {
			m = Material.valueOf(id);
		} catch (Exception e) {
			return null;
		}
		
		byte data = (byte) c.getInt(section + ".data", 0);
		String name = c.getString(section + ".name", "&a");
		List<String> lore = c.getStringList(section + ".lore");
		boolean ench = c.getBoolean(section + ".ench", false);
		
		
		
		
		ItemStack item = new ItemStack(m, 1, data);
		ItemMeta meta = item.getItemMeta();
		if(name != null) meta.setDisplayName(name.replace("&", "\u00a7"));
		if(lore != null)
		{
			List<String> lorez = new ArrayList<String>();
			for (String l : lore) {
				if(l.startsWith("pastebin:")) {
					String pastebin = l.replaceAll("pastebin:", "").trim();
					if(pastebin.contains(" ")) continue;
					try {
						List<String> payload = Rules.pastebin().getList(pastebin);
						for (String p : payload) {
							lorez.add("§7" + p.replace("&", "\u00a7"));
						}
						continue;
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}
				
				lorez.add("§7" + l.replace("&", "\u00a7"));
			}
			meta.setLore(lorez);
		}
		if(ench) {
			meta.addEnchant(Enchantment.DURABILITY, 1, true);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		item.setItemMeta(meta);
		return item;
	}
	
}
