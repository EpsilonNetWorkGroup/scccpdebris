package net.playl.scccpdebris.Listen.Task;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class SecurityStarlight extends BukkitRunnable {
    
    private final Player p;
    private final PotionEffectType type;

    private boolean changed;

    public SecurityStarlight(Player p, PotionEffectType type) {
        this.p = p;
        this.type = type;
    }

    @Override
    public void run() {
        if (!p.isOnline()) {
            cancel();
            return;
        }
        
        PotionEffect pf = p.getPotionEffect(type);
        if (pf == null) {
            p.setViewDistance(p.getWorld().getViewDistance());
            p.setSimulationDistance(p.getWorld().getSimulationDistance());
            cancel();
            return;
        }

        if (pf.getDuration() <= 30) {
            p.setViewDistance(p.getWorld().getViewDistance());
            p.setSimulationDistance(p.getWorld().getSimulationDistance());
            return;
        }

        if (!changed) {
            p.setViewDistance(2);
            p.setSimulationDistance(2);
            changed = true;
        }
    }
}
