package net.dezilla.basicwarp;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

public class BasicWarp extends JavaPlugin{
	private static BasicWarp instance;
	public static BasicWarp getInstance() {
		return instance;
	}
	
	@Override
	public void onEnable() {
		instance = this;
		/*-----[Commands]-----*/
		try {
			final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

			bukkitCommandMap.setAccessible(true);
			CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
			
			List<Command> commands = Arrays.asList(
					new WarpCommand());
			commandMap.registerAll("basicwarp", commands);
		} catch(Exception e) {
			e.printStackTrace();
		}
		/*-----[Listeners]-----*/
		getServer().getPluginManager().registerEvents(new WarpListener(), this);
		//
		System.out.println("[BasicWarp] BasicWarp by DeZilla is enabled.");
	}

}
