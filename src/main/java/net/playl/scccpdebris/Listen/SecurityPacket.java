package net.playl.scccpdebris.Listen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EnderDragonChangePhaseEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Action;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.AutoWrapper;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.Converters;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.comphenix.protocol.wrappers.WrappedDataValue;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.playl.scccpdebris.Main;
import net.playl.scccpdebris.Listen.Task.SecurityStarlight;

public class SecurityPacket extends PacketAdapter implements Listener {
    
    private static final List<PacketType> LISTENING_PACKETS = Arrays.asList(PacketType.Play.Server.ENTITY_METADATA,
    PacketType.Play.Server.ENTITY_EFFECT, PacketType.Play.Server.VIEW_DISTANCE, PacketType.Play.Server.ADVANCEMENTS);

    public SecurityPacket(Main plugin) {
        super((Plugin) plugin, ListenerPriority.NORMAL, LISTENING_PACKETS);
        this.plugin = plugin;
    }

    public void onPacketSending(PacketEvent event) {
        PacketType type = event.getPacketType();
        if (type == PacketType.Play.Server.ENTITY_METADATA) {
            entityMetaData(event);
        } else if (type == PacketType.Play.Server.ENTITY_EFFECT) {
            entityMetaEffect(event);
        } else if (type == PacketType.Play.Server.VIEW_DISTANCE) {
            fogWhisperVD(event);
        } else if (type == PacketType.Play.Server.ADVANCEMENTS) {
            advGuard(event);
        }
    }

    private void entityMetaData(PacketEvent e) {
        PacketContainer packet = e.getPacket();
        Player p = e.getPlayer();
        Entity entity = packet.getEntityModifier(e).read(0);
        List<WrappedDataValue> modifier = packet.getDataValueCollectionModifier().read(0);
        if (!(entity instanceof LivingEntity) || e.isPlayerTemporary()) {
            return;
        }
        for (WrappedDataValue obj : modifier) {
            /*
             * 9 - health
             * https://wiki.vg/Protocol#Set_Entity_Metadata
             * https://wiki.vg/Entity_metadata#Living
             */
            if (obj.getIndex() == 9) {
                if (entity.getEntityId() == p.getEntityId() || entity.getPassengers().contains(p)
                        || (entity instanceof Wolf)
                        || (entity instanceof EnderDragon) || (entity instanceof Wither)
                        || (entity instanceof IronGolem)) {
                    obj.setValue((float) ((LivingEntity) entity).getHealth());
                } else {
                    Float value = (Float) obj.getValue();
                    if (value > 0) {
                        double health = ((new Random()).nextFloat() * 1.5F)
                                * ((LivingEntity) entity).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                        obj.setValue((float) health);
                    }
                }
            }
        }
    }

    public static class CriterionProgress {
        public Date date;
    }

    public static class AdvancementProgress {
        public Map<String, CriterionProgress> progress;
    }

    private static final AutoWrapper<CriterionProgress> CRITERION = AutoWrapper
    .wrap(CriterionProgress.class, "advancements.CriterionProgress");

    private static final AutoWrapper<AdvancementProgress> PROGRESS = AutoWrapper
    .wrap(AdvancementProgress.class, "advancements.AdvancementProgress")
    .field(0, BukkitConverters.getMapConverter(Converters.passthrough(String.class), CRITERION));

    private void advGuard(PacketEvent e) {
        // https://wiki.vg/Protocol#Update_Advancements
        PacketContainer packet = e.getPacket();
        StructureModifier<Map<MinecraftKey, AdvancementProgress>> modifier = packet.getMaps(MinecraftKey.getConverter(), PROGRESS);
        Map<MinecraftKey, AdvancementProgress> map = modifier.optionRead(1).orElse(null);

        if (map != null) {
            map.forEach((k, v) -> {
                if (k.getKey().contains("recipes")) {
                    return;
                }
                List<String> shufflekey = new ArrayList<>(v.progress.keySet());
                Collections.shuffle(shufflekey);
                Iterator<String> vIter = shufflekey.iterator();

                Map<String, CriterionProgress> shuffledAdvProgress = new HashMap<>();
                v.progress.values().forEach(Crprogress -> {
                    shuffledAdvProgress.put(vIter.next(), Crprogress);
                });

                v.progress = shuffledAdvProgress;
                map.put(k, v);
            });
            modifier.write(1, map);
        }
    }

    private int getPlayerAdjustVD(Player p) {
        int WorldVD = p.getWorld().getSendViewDistance();
        int ClientVD = p.getClientViewDistance();
        int minVD = Math.min(WorldVD, ClientVD);
        // IDK, but fog reduces viewing distance by about 60%
        return Math.max(2, minVD - ((int) Math.ceil((minVD * 0.6))));
    }

    private void fogWhisperVD(PacketEvent e) {
        // https://wiki.vg/Protocol#Set_Render_Distance
        PacketContainer packet = e.getPacket();
        
        Bukkit.broadcast(Component.text("playervdnow: "+e.getPlayer().getSendViewDistance()+" packetsend:"+packet.getIntegers().read(0)));

        if (e.getPlayer().getWorld().getEnvironment() == Environment.NETHER) {

            try {
                e.getPlayer().setSendViewDistance(getPlayerAdjustVD(e.getPlayer()));
            } catch (IllegalStateException r) {
            }
                
            packet.getIntegers().write(0, e.getPlayer().getWorld().getSendViewDistance());
        }

        if (e.getPlayer().getWorld().getEnvironment() == Environment.NORMAL) {
            try {
                e.getPlayer().setSendViewDistance(-1);
            } catch (IllegalStateException r) {
            }
        }

        if (e.getPlayer().getWorld().getEnvironment() == Environment.THE_END) {
            if (e.getPlayer().getSendViewDistance() != e.getPlayer().getWorld().getSendViewDistance()) {
                packet.getIntegers().write(0, e.getPlayer().getWorld().getSendViewDistance());
                return;
            }
        }
    }

    @EventHandler
    public void fogWhisper(EnderDragonChangePhaseEvent e) {
        if (e.getEntity().getDragonBattle() == null) {
            return;
        }

        e.getEntity().getDragonBattle().getBossBar().getPlayers().forEach(p -> {
            if (p.getSendViewDistance() != p.getWorld().getSendViewDistance()) {
                return;
            }

            try {
                p.setSendViewDistance(getPlayerAdjustVD(p));
            } catch (IllegalStateException r) {
            }
            Bukkit.getScheduler().runTaskTimer(plugin, (task) -> {
                if (!p.isOnline()) {
                    task.cancel();
                    return;
                }
                World w = p.getWorld();
                if (w.getEnvironment() != Environment.THE_END) {
                    task.cancel();
                    return;
                }
                if (w.getEnderDragonBattle().getEnderDragon() == null) {
                    try {
                        p.setSendViewDistance(-1);
                    } catch (IllegalStateException r) {
                        return;
                    }
                    task.cancel();
                    return;
                }
                if (!w.getEnderDragonBattle().getBossBar().getPlayers().contains(p)) {
                    try {
                        p.setSendViewDistance(-1);
                    } catch (IllegalStateException r) {
                        return;
                    }
                    task.cancel();
                    return;
                }
            }, 5L, 20L);
        });
    }

    private void entityMetaEffect(PacketEvent e) {
        // https://wiki.vg/Protocol#Entity_Effect
        PacketContainer packet = e.getPacket();
        int entityID = packet.getIntegers().read(0);

        if (e.getPlayer().getEntityId() == entityID) {
            return;
        }

        packet.getEffectTypes().write(0, PotionEffectType.UNLUCK);
        packet.getBytes().write(0, (byte) 0);
        packet.getIntegers().write(1, 0);

    }

    @EventHandler
    public void onMount(EntityMountEvent event) {
        updateMount(event.getEntity(), event.getMount());
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        updateMount(event.getEntity(), event.getDismounted());
    }

    private void updateMount(Entity entity, Entity mount) {
        if (!(entity instanceof Player) || !(mount instanceof LivingEntity)) {
            return;
        }

        LivingEntity ent = (LivingEntity) mount;
        double health = ent.getHealth();

        ent.setHealth(1);
        ent.setHealth(health);
    }

    @EventHandler
    public void starlightCheck(PlayerToggleSprintEvent event) {
        if (event.getPlayer().hasPotionEffect(PotionEffectType.BLINDNESS)) {
            if (event.isSprinting()) {
                event.getPlayer().kick(Component.text("impossible move (Starlight)", NamedTextColor.RED));
            }
        }
    }

    @EventHandler
    public void starlight(EntityPotionEffectEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) {
            return;
        }
        if (event.getAction() != Action.ADDED) {
            return;
        }
        if (!event.getModifiedType().equals(PotionEffectType.DARKNESS)
                && !event.getModifiedType().equals(PotionEffectType.BLINDNESS)) {
            return;
        }
        new SecurityStarlight((Player) event.getEntity(), event.getModifiedType()).runTaskTimer(plugin, 25L, 10L);
    }
}
