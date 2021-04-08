package net.dezilla.basicwarp;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.SystemUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class WarpUtil {
	
	public static String MSG_START = ChatColor.DARK_GRAY+"» "+ChatColor.WHITE;
	private static String slash;
	private static JSONArray array;
	static {
		slash = (SystemUtils.IS_OS_WINDOWS ? "\\" : "/");
		load();
	}
	
	public static boolean canCreate(Player player) {
		for(PermissionAttachmentInfo i : player.getEffectivePermissions()) {
			if(i.getPermission().toLowerCase().startsWith("basicwarp.limit.")) {
				try {
					int limit = Integer.parseInt(i.getPermission().split("basicwarp.limit.")[1]);
					if(getWarps(player).size()>=limit)
						return false;
				} catch(Exception e) {
					return true;
				}
			}
		}
		return true;
	}
	
	public static int getDelay(Player player) {
		for(PermissionAttachmentInfo i : player.getEffectivePermissions()) {
			if(i.getPermission().toLowerCase().startsWith("basicwarp.delay.")) {
				try {
					return Integer.parseInt(i.getPermission().split("basicwarp.delay.")[1]);
				} catch(Exception e) {
					return 0;
				}
			}
		}
		return 0;
	}
	
	private static Map<Player, Integer> delayed = new HashMap<Player, Integer>();
	
	public static boolean isWarping(Player player) {
		return delayed.containsKey(player);
	}
	
	public static void cancelWarp(Player player, boolean msg) {
		if(delayed.containsKey(player)) {
			Bukkit.getScheduler().cancelTask(delayed.get(player));
			delayed.remove(player);
			if(msg)
				player.sendMessage(MSG_START+ChatColor.DARK_AQUA+"Warp cancelled.");
		}
	}
	
	public static void delayedWarp(Player player, Warp warp, int ticks) {
		if(isWarping(player))
			return;
		int id = Bukkit.getScheduler().scheduleSyncDelayedTask(BasicWarp.getInstance(), () -> {
			warp.go(player);
			delayed.remove(player);
		}, ticks);
		delayed.put(player, id);
	}
	
	public static Warp getWarp(Player player, String name) {
		return getWarp(Bukkit.getOfflinePlayer(player.getName()), name);
	}
	
	public static Warp getWarp(OfflinePlayer player, String name) {
		for(Warp w : getWarps(player)) {
			if(w.getName().equalsIgnoreCase(name))
				return w;
		}
		return null;
	}
	
	public static List<Warp> getWarps(Player player){
		return getWarps(Bukkit.getOfflinePlayer(player.getName()));
	}
	
	public static List<Warp> getWarps(OfflinePlayer player){
		List<Warp> warps = new ArrayList<Warp>();
		for(Warp w : getWarps()) {
			if(w.getOwner().getName().equals(player.getName()))
				warps.add(w);
		}
		return warps;
	}
	
	public static List<Warp> getWarps(){
		List<Warp> warps = new ArrayList<Warp>();
		array.forEach(a -> {
			try {
				JSONObject o = (JSONObject) a;
				Warp w = new Warp();
				OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString((String) o.get("owner")));
				w.setOwner(p);
				w.setWorld((String) o.get("world"));
				w.setX((double) o.get("x"));
				w.setY((double) o.get("y"));
				w.setZ((double) o.get("z"));
				w.setYaw(Float.parseFloat(Double.toString((double) o.get("yaw"))));//because it doesnt like to be cast directly in float for some dumb reason
				w.setPitch(Float.parseFloat(Double.toString((double) o.get("pitch"))));
				w.setName((String) o.get("name"));
				warps.add(w);
			}catch(Exception e) {
				e.printStackTrace();
			}
		});
		return warps;
	}
	
	public static void deleteWarp(Player player, String name) {
		deleteWarp(Bukkit.getOfflinePlayer(player.getName()), name);
	}
	
	public static void deleteWarp(OfflinePlayer player, String name){
		List<JSONObject> l = new ArrayList<JSONObject>();
		array.forEach(a -> {
			try {
				JSONObject o = (JSONObject) a;
				OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString((String) o.get("owner")));
				String n = (String) o.get("name");
				if(name.equalsIgnoreCase(n) && p.getName().equals(player.getName())) {
					l.add(o);
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		});
		for(JSONObject o : l)
			array.remove(o);
		save();
	}
	
	public static void saveWarp(Warp warp) {
		JSONObject jwarp = new JSONObject();
		jwarp.put("owner", warp.getOwner().getUniqueId().toString());
		jwarp.put("world", warp.getWorldName());
		jwarp.put("x", warp.getX());
		jwarp.put("y", warp.getY());
		jwarp.put("z", warp.getZ());
		jwarp.put("yaw", warp.getYaw());
		jwarp.put("pitch", warp.getPitch());
		jwarp.put("name", warp.getName());
		array.add(jwarp);
		save();
		load();
	}
	
	private static void save() {
		slash = (SystemUtils.IS_OS_WINDOWS ? "\\" : "/");
		File folder = BasicWarp.getInstance().getDataFolder();
		if(!folder.exists() || !folder.isDirectory()) {
			folder.mkdir();
		}
		File file = new File(folder.getPath()+slash+"warps.json");
		try(FileWriter f = new FileWriter(file)){
			f.write(array.toJSONString());
			f.flush();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void load() {
		File folder = BasicWarp.getInstance().getDataFolder();
		if(!folder.exists() || !folder.isDirectory()) {
			folder.mkdir();
		}
		File file = new File(folder.getPath()+slash+"warps.json");
		//create file
		if(!file.exists()) {
			try(FileWriter f = new FileWriter(file)){
				array = new JSONArray();
				f.write(array.toJSONString());
				f.flush();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		//read file
		else {
			JSONParser jsonParser = new JSONParser();
			try(FileReader reader = new FileReader(file)){
				Object obj = jsonParser.parse(reader);
				array = (JSONArray) obj;
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static class Warp{
		String world;
		double x;
		double y;
		double z;
		float yaw;
		float pitch;
		String name;
		OfflinePlayer owner;
		Warp(Location location, Player player, String name){
			this.owner = Bukkit.getOfflinePlayer(player.getName());
			this.world = location.getWorld().getName();
			this.x = location.getX();
			this.y = location.getY();
			this.z = location.getZ();
			this.yaw = location.getYaw();
			this.pitch = location.getPitch();
			this.name = name;
		}
		Warp(Location location, OfflinePlayer player, String name){
			this.owner = player;
			this.world = location.getWorld().getName();
			this.x = location.getX();
			this.y = location.getY();
			this.z = location.getZ();
			this.yaw = location.getYaw();
			this.pitch = location.getPitch();
			this.name = name;
		}
		Warp(){
		}
		public boolean go(Player player) {
			try {
				player.teleport(getLocation());
				return true;
			}catch(Exception e) {
				return false;
			}
		}
		public Location getLocation() {
			try {
				Location l = new Location(Bukkit.getWorld(world), x, y ,z);
				l.setYaw(yaw);
				l.setPitch(pitch);
				return l;
			}catch(Exception e) {
				return null;
			}
		}
		public void setLocation(Location location) {
			this.world = location.getWorld().getName();
			this.x = location.getX();
			this.y = location.getY();
			this.z = location.getZ();
			this.yaw = location.getYaw();
			this.pitch = location.getPitch();
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public OfflinePlayer getOwner() {
			return owner;
		}
		public void setOwner(OfflinePlayer player) {
			this.owner = player;
		}
		public void setOwner(Player player) {
			this.owner = Bukkit.getOfflinePlayer(player.getName());
		}
		public double getX() {
			return x;
		}
		public double getY() {
			return y;
		}
		public double getZ() {
			return z;
		}
		public void setX(double x) {
			this.x = x;
		}
		public void setY(double y) {
			this.y = y;
		}
		public void setZ(double z) {
			this.z = z;
		}
		public float getYaw() {
			return yaw;
		}
		public float getPitch() {
			return pitch;
		}
		public void setYaw(float yaw) {
			this.yaw = yaw;
		}
		public void setPitch(float pitch) {
			this.pitch = pitch;
		}
		public World getWorld() {
			return Bukkit.getWorld(world);
		}
		public String getWorldName() {
			return world;
		}
		public void setWorld(World world) {
			this.world = world.getName();
		}
		public void setWorld(String world) {
			this.world = world;
		}
	}

}
