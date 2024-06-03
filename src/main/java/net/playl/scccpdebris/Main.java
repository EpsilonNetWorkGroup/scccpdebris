package net.playl.scccpdebris;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin {

    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        //Are all listeners read only?
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false)
                .checkForUpdates(true)
                .bStats(true);
        PacketEvents.getAPI().load();
    }

    public void onEnable() {
        //We register before calling PacketEvents#init, because that method might already call some events.
        PacketEvents.getAPI().getEventManager().registerListener(new Ser());
        PacketEvents.getAPI().init();
    }
    
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }
}