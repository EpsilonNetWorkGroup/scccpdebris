package net.playl.scccpdebris;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketListener;

import net.playl.scccpdebris.Listen.SecurityPacket;

import static com.comphenix.protocol.utility.Util.classExists;

public class Main extends JavaPlugin {
    private static final boolean IS_FOLIA = classExists("io.papermc.paper.threadedregions.RegionizedServer");

    public void onEnable() {
        if (IS_FOLIA) {
            getLogger().warning("The folia version is for testing only. You are responsible for any consequences caused by it.");
        }

        ProtocolLibrary.getProtocolManager().addPacketListener((PacketListener)new SecurityPacket(this));
        Bukkit.getServer().getPluginManager().registerEvents(new SecurityPacket(this), this);
    }
    
    public void onDisable() {
    }
}