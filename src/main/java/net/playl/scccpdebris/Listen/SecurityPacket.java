package net.playl.scccpdebris.Listen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.advancements.AdvancementProgress;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAdvancements;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateViewDistance;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
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
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Action;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.playl.scccpdebris.Main;
import net.playl.scccpdebris.Listen.Task.SecurityStarlight;

public class SecurityPacket extends PacketListenerAbstract implements Listener {

    private final Main plugin;

    public SecurityPacket(Main plugin) {
        super(PacketListenerPriority.LOW);
        this.plugin = plugin;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        PacketTypeCommon type = event.getPacketType();
        if (type == PacketType.Play.Server.ENTITY_METADATA) {
            entityMetaData(event);
        } else if (type == PacketType.Play.Server.ENTITY_EFFECT) {
            entityMetaEffect(event);
        } else if (type == PacketType.Play.Server.UPDATE_VIEW_DISTANCE) {
            fogWhisperVD(event);
        } else if (type == PacketType.Play.Server.UPDATE_ADVANCEMENTS) {
            advGuard(event);
        }
    }

    private void entityMetaData(PacketSendEvent e) {
        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(e);
        Player p = e.getPlayer();

        // safe or not, idk
        var entity = SpigotConversionUtil.getEntityById(p.getWorld(), packet.getEntityId());
        if (entity == null) {
            return;
        }

        if (!(entity instanceof LivingEntity)) {
            return;
        }

        /*
         * 9 - health
         * https://wiki.vg/Protocol#Set_Entity_Metadata
         * https://wiki.vg/Entity_metadata#Living
         */
        var entityMetadata = packet.getEntityMetadata();
        entityMetadata.forEach(entityData -> {


            if (entityData.getIndex() == 9) {
                if (entity.getEntityId() != p.getEntityId() && !entity.getPassengers().contains(p)
                        && !(entity instanceof Wolf)
                        && !(entity instanceof EnderDragon) && (entity instanceof Wither)
                        && !(entity instanceof IronGolem)) {
                    Float value = (Float) entityData.getValue();
                    if (value > 0) {
                        e.markForReEncode(true);
                        double health = ((new Random()).nextFloat() * 1.5F)
                                * ((LivingEntity) entity).getAttribute(Attribute.MAX_HEALTH).getValue();
                        ((EntityData<Float>) entityData).setValue((float) health);
                    }
                }
            }
        });
        e.markForReEncode(true);
    }

    private void advGuard(PacketSendEvent e) {
        // https://wiki.vg/Protocol#Update_Advancements
        WrapperPlayServerUpdateAdvancements packet = new WrapperPlayServerUpdateAdvancements(e);
        Map<ResourceLocation, AdvancementProgress> map = packet.getProgress();

        if (map != null) {
            e.markForReEncode(true);
            map.forEach((k, v) -> {
                if (k.getKey().contains("recipes")) {
                    return;
                }
                List<String> shufflekey = new ArrayList<>(v.getCriteria().keySet());
                Collections.shuffle(shufflekey);
                Iterator<String> vIter = shufflekey.iterator();

                Map<String, AdvancementProgress. CriterionProgress> shuffledAdvProgress = new HashMap<>();
                v.getCriteria().values().forEach(Crprogress -> shuffledAdvProgress.put(vIter.next(), Crprogress));

                v.setCriteria(shuffledAdvProgress);
                map.put(k, v);
            });
        }
    }

    private int getPlayerAdjustVD(Player p) {
        int WorldVD = p.getWorld().getSendViewDistance();
        int ClientVD = p.getClientViewDistance();
        int minVD = Math.min(WorldVD, ClientVD);
        // IDK, but fog reduces viewing distance by about 60%
        return Math.max(2, minVD - ((int) Math.ceil((minVD * 0.6))));
    }

    private void fogWhisperVD(PacketSendEvent e) {
        // https://wiki.vg/Protocol#Set_Render_Distance
        WrapperPlayServerUpdateViewDistance packet = new WrapperPlayServerUpdateViewDistance(e);
        Player p = (Player) e.getPlayer();

        if (p.getWorld().getEnvironment() == Environment.NETHER) {

            try {
                p.setSendViewDistance(getPlayerAdjustVD(p));
            } catch (IllegalStateException ignored) {
            }

            packet.setViewDistance(p.getWorld().getSendViewDistance());
        }

        if (p.getWorld().getEnvironment() == Environment.NORMAL) {
            try {
                p.setSendViewDistance(-1);
            } catch (IllegalStateException ignored) {
            }
        }

        if (p.getWorld().getEnvironment() == Environment.THE_END) {
            if (packet.getViewDistance() != p.getWorld().getSendViewDistance()) {
                packet.setViewDistance(p.getWorld().getSendViewDistance());
            }
        }
        e.markForReEncode(true);
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
            } catch (IllegalStateException ignored) {
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
                if (w.getEnderDragonBattle() == null || w.getEnderDragonBattle().getEnderDragon() == null) {
                    try {
                        p.setSendViewDistance(-1);
                    } catch (IllegalStateException r) {
                        return;
                    }
                    task.cancel();
                } else if (!w.getEnderDragonBattle().getBossBar().getPlayers().contains(p)) {
                    try {
                        p.setSendViewDistance(-1);
                    } catch (IllegalStateException r) {
                        return;
                    }
                    task.cancel();
                }
            }, 5L, 20L);
        });
    }

    private void entityMetaEffect(PacketSendEvent e) {
        // https://wiki.vg/Protocol#Entity_Effect
        WrapperPlayServerEntityEffect packet = new WrapperPlayServerEntityEffect(e);
        Player p = (Player) e.getPlayer();
        int entityID = packet.getEntityId();

        if (p.getEntityId() == entityID) {
            return;
        }

        packet.setPotionType(PotionTypes.UNLUCK);
        packet.setEffectAmplifier(0);
        packet.setEffectDurationTicks(0);
        e.markForReEncode(true);
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
        if (!(entity instanceof Player) || !(mount instanceof LivingEntity ent)) {
            return;
        }

        double health = ent.getHealth();

        // Force the server to resend health packet
        ent.setHealth(1);
        ent.setHealth(health);
    }

    @EventHandler
    public void starlightCheck(PlayerToggleSprintEvent event) {
        if (event.getPlayer().hasPotionEffect(PotionEffectType.BLINDNESS)) {
            if (event.isSprinting()) {
                plugin.getLogger().warning("One player was kicked off the server for sprinting while under the" +
                        " effect of blindness, which is something that normal clients would never do.");
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
