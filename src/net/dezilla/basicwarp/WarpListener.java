package net.dezilla.basicwarp;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class WarpListener implements Listener{
	
	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		if(!WarpUtil.isWarping(event.getPlayer()))
			return;
		Location f = event.getFrom();
		Location t = event.getTo();
		if(f.getX()!=t.getX() || f.getY()!=t.getY() || f.getZ()!=t.getZ())
			WarpUtil.cancelWarp(event.getPlayer(), true);
	}
	
	@EventHandler
	public void onDmg(EntityDamageEvent event) {
		if(!(event.getEntity() instanceof Player))
			return;
		Player p = (Player) event.getEntity();
		if(!WarpUtil.isWarping(p))
			return;
		WarpUtil.cancelWarp(p, true);
	}

}
