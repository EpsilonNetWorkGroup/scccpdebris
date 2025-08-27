package net.playl.scccpdebris;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import net.playl.scccpdebris.Listen.SecurityPacket;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin {

    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    public void onEnable() {
        //We register before calling PacketEvents#init, because that method might already call some events.
        PacketEvents.getAPI().getEventManager().registerListener(new SecurityPacket(this));
        PacketEvents.getAPI().init();
    }
    
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }
}