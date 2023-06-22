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
            p.setSendViewDistance(-1);
            cancel();
            return;
        }

        if (pf.getDuration() <= 30) {
            p.setSendViewDistance(-1);
            cancel();
            return;
        }

        if (!changed) {
            p.setSendViewDistance(2);
            changed = true;
        }
    }
}
