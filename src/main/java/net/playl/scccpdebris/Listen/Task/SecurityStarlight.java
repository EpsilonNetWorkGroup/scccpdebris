package net.playl.scccpdebris.Listen.Task;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.function.Consumer;

public class SecurityStarlight implements Consumer<ScheduledTask> {
    
    private final Player p;
    private final PotionEffectType type;

    private boolean changed;

    public SecurityStarlight(Player p, PotionEffectType type) {
        this.p = p;
        this.type = type;
    }

    @Override
    public void accept(ScheduledTask scheduledTask) {
        PotionEffect pf = p.getPotionEffect(type);
        if (pf == null) {
            p.setSendViewDistance(-1);
            scheduledTask.cancel();
            return;
        }

        if (pf.getDuration() <= 30) {
            p.setSendViewDistance(-1);
            scheduledTask.cancel();
            return;
        }

        if (!changed) {
            p.setSendViewDistance(2);
            changed = true;
        }
    }
}
