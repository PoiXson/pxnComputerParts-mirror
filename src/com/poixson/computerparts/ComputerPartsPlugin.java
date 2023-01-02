package com.poixson.computerparts;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.poixson.computerparts.listeners.ComputerPartsCommands;
import com.poixson.computerparts.parts.ComputerPart;


public class ComputerPartsPlugin extends JavaPlugin {
	public static final String LOG_PREFIX  = "[Computer] ";
	public static final String CHAT_PREFIX = ChatColor.AQUA + "[Computer] " + ChatColor.WHITE;
	public static final Logger log = Logger.getLogger("Minecraft");
	protected static final AtomicReference<ComputerPartsPlugin> instance = new AtomicReference<ComputerPartsPlugin>(null);
	protected static final AtomicReference<Metrics>             metrics  = new AtomicReference<Metrics>(null);

	// listeners
	protected final AtomicReference<ComputerPartsCommands> commandListener = new AtomicReference<ComputerPartsCommands>(null);

	protected final CopyOnWriteArraySet<ComputerPart> parts = new CopyOnWriteArraySet<ComputerPart>();



	public ComputerPartsPlugin() {
	}



	@Override
	public void onEnable() {
		if (!instance.compareAndSet(null, this))
			throw new RuntimeException("Plugin instance already enabled?");
		// commands listener
		{
			final ComputerPartsCommands listener = new ComputerPartsCommands(this);
			final ComputerPartsCommands previous = this.commandListener.getAndSet(listener);
			if (previous != null)
				previous.unregister();
			listener.register();
		}
		// bStats
		System.setProperty("bstats.relocatecheck","false");
		metrics.set(new Metrics(this, 17232));
	}

	@Override
	public void onDisable() {
		// unload emulators
		{
			final Iterator<ComputerPart> it = this.parts.iterator();
			while (it.hasNext()) {
				final ComputerPart part = it.next();
				part.unload();
				it.remove();
			}
		}
		// commands listener
		{
			final ComputerPartsCommands listener = this.commandListener.getAndSet(null);
			if (listener != null)
				listener.unregister();
		}
		// stop schedulers
		try {
			Bukkit.getScheduler()
				.cancelTasks(this);
		} catch (Exception ignore) {}
		// stop listeners
		HandlerList.unregisterAll(this);
		if (!instance.compareAndSet(this, null))
			throw new RuntimeException("Disable wrong instance of plugin?");
	}



}