package net.playl.scccpdebris;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketListener;

import net.playl.scccpdebris.Listen.SecurityPacket;

public class Main extends JavaPlugin {
    public void onEnable() {
        ProtocolLibrary.getProtocolManager().addPacketListener((PacketListener)new SecurityPacket(this));
        Bukkit.getServer().getPluginManager().registerEvents(new SecurityPacket(this), this);
    }
    
    public void onDisable() {
    }
}