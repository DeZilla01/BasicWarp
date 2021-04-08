package net.dezilla.basicwarp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dezilla.basicwarp.WarpUtil.Warp;

public class WarpCommand extends Command{

	public WarpCommand() {
		super("warp", "Warp to different locations.", "/blocks", Arrays.asList("g", "go"));
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		//is Player check
		if(!(sender instanceof Player)) {
			sender.sendMessage("You must be a player to use this command.");
			return true;
		}
		Player player = (Player) sender;
		//permission check
		if(!player.hasPermission("basicwarp.warp") && !player.isOp()) {
			player.sendMessage(WarpUtil.MSG_START+ChatColor.RED+"You do not have permission to use this command.");
			return true;
		}
		//teleport to other player's warps
		OfflinePlayer target = null;
		if(player.hasPermission("basicwarp.admin") || player.isOp()) {
			if(commandLabel.toLowerCase().startsWith("g") && args.length>=2) {
				target = Bukkit.getOfflinePlayer(args[1]);
				if(target == null) {
					invalidTarget(player);
					return true;
				}
			} else if(args.length>=3) {
				target = Bukkit.getOfflinePlayer(args[2]);
				if(target == null) {
					invalidTarget(player);
					return true;
				}
			}
		}
		//No args
		if(args.length==0) {
			usage(player);
			return true;
		}
		//Go
		if(commandLabel.toLowerCase().startsWith("g") || args[0].equalsIgnoreCase("go")) {
			if(!commandLabel.toLowerCase().startsWith("g") && args.length < 2) {
				noName(player);
				return true;
			}
			Warp warp;
			if(target == null)
				warp = WarpUtil.getWarp(player, args[(commandLabel.toLowerCase().startsWith("g") ? 0 : 1)]);
			else
				warp = WarpUtil.getWarp(target, args[(commandLabel.toLowerCase().startsWith("g") ? 0 : 1)]);
			if(warp == null) {
				notFound(player);
				return true;
			}
			int delay = WarpUtil.getDelay(player);
			if(delay != 0) {
				player.sendMessage(WarpUtil.MSG_START+ChatColor.DARK_AQUA+"Warping in "+delay+" seconds.");
				WarpUtil.delayedWarp(player, warp, 20*delay);
			}
			else
				warp.go(player);
			return true;
		}
		//set
		if(args[0].equalsIgnoreCase("set")) {
			if(args.length < 2) {
				noName(player);
				return true;
			}
			Warp warp;
			if(target == null)
				warp = WarpUtil.getWarp(player, args[1]);
			else
				warp = WarpUtil.getWarp(target, args[1]);
			if(warp != null)
				WarpUtil.deleteWarp(player, args[1]);
			else if(!WarpUtil.canCreate(player)) {
				limit(player);
				return true;
			}
			WarpUtil.saveWarp(new Warp(player.getLocation(), (target!=null ? target : player), args[1]));
			player.sendMessage(WarpUtil.MSG_START+ChatColor.DARK_AQUA+"Warp "+args[1]+" set.");
			return true;
		}
		//delete
		if(args[0].equalsIgnoreCase("delete")) {
			if(args.length < 2) {
				noName(player);
				return true;
			}
			Warp warp;
			if(target == null)
				warp = WarpUtil.getWarp(player, args[1]);
			else
				warp = WarpUtil.getWarp(target, args[1]);
			if(warp==null) {
				notFound(player);
				return true;
			}
			WarpUtil.deleteWarp(player, args[1]);
			player.sendMessage(WarpUtil.MSG_START+ChatColor.DARK_AQUA+"Warp "+args[1]+" deleted.");
			return true;
		}
		//list
		if(args[0].equalsIgnoreCase("list")) {
			List<Warp> warps = WarpUtil.getWarps(target == null ? player : target);
			if(warps.isEmpty()) {
				notFound(player);
				return true;
			}
			player.sendMessage(WarpUtil.MSG_START+ChatColor.DARK_AQUA+"Warp List: ");
			for(Warp w : warps) {
				player.sendMessage(""+ChatColor.GOLD+ChatColor.ITALIC+" - "+w.getName());
			}
			return true;
		}
		usage(player);
		return true;
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		Player player = null;
		if(sender instanceof Player) {
			player = (Player) sender;
			if(!player.hasPermission("basicwarp.warp") && !player.isOp())
				return new ArrayList<String>();
		}
		List<String> list = new ArrayList<String>();
		if(args.length==1 && alias.equalsIgnoreCase("warp")) {
			for(String s : Arrays.asList("set", "go", "delete", "list")) {
				if(s.toLowerCase().startsWith(args[0].toLowerCase()))
					list.add(s);
			}
		} else if((alias.toLowerCase().startsWith("g") && args.length == 1) || (alias.equalsIgnoreCase("warp") && args.length == 2)) {
			if(player == null)
				return list;
			for(Warp w : WarpUtil.getWarps(player))
				if(w.getName().toLowerCase().startsWith(args[args.length-1].toLowerCase()))
					list.add(w.getName());
		} else if(player != null && (player.hasPermission("basicwarp.admin") || player.isOp()) &&
				((alias.toLowerCase().startsWith("g") && args.length == 2) || (alias.equalsIgnoreCase("warp") && args.length == 3))) {
			for(Player p : Bukkit.getOnlinePlayers()) 
				if(p.getName().toLowerCase().startsWith(args[args.length-1].toLowerCase()))
					list.add(p.getName());
		}
		return list;
	}
	
	private void usage(Player player) {
		player.sendMessage(WarpUtil.MSG_START+ChatColor.DARK_AQUA+"Usage: ");
		player.sendMessage(""+ChatColor.GOLD+ChatColor.ITALIC+" /warp set <name>");
		player.sendMessage(""+ChatColor.GOLD+ChatColor.ITALIC+" /warp go <name>");
		player.sendMessage(""+ChatColor.GOLD+ChatColor.ITALIC+" /warp delete <name>");
		player.sendMessage(""+ChatColor.GOLD+ChatColor.ITALIC+" /warp list <name>");
		player.sendMessage(""+ChatColor.GOLD+ChatColor.ITALIC+" /go <name>");
	}
	
	private void noName(Player player) {
		player.sendMessage(WarpUtil.MSG_START+ChatColor.RED+"Please enter a name.");
	}
	
	private void notFound(Player player) {
		player.sendMessage(WarpUtil.MSG_START+ChatColor.RED+"Warp not found.");
	}
	
	private void invalidTarget(Player player) {
		player.sendMessage(WarpUtil.MSG_START+ChatColor.RED+"Invalid player.");
	}
	
	private void limit(Player player) {
		player.sendMessage(WarpUtil.MSG_START+ChatColor.RED+"You cannot create more warps.");
	}
}
