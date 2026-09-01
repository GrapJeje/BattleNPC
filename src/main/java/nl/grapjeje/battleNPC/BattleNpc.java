package nl.grapjeje.battleNPC;

import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import lombok.Getter;
import lombok.Setter;
import nl.grapjeje.battleNPC.attacks.DashAttack;
import nl.grapjeje.battleNPC.attacks.FallingAttack;
import nl.grapjeje.battleNPC.configs.DefaultConfiguration;
import nl.grapjeje.core.tasks.Task;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Optional;

public class BattleNpc {
    @Getter
    Mannequin npc;
    BossBar bossBar;

    BukkitTask tickTask;
    @Getter
    @Setter
    boolean attacking = false;

    @SuppressWarnings("UnstableApiUsage")
    public BattleNpc(Location location) {
        location = location.add(0, 5, 0);
        npc = location.getWorld().spawn(location, Mannequin.class);
        // Apply the boss skin
        DefaultConfiguration.Skin skin = Main.getInstance()
                .getDefaultConfiguration()
                .getSkin();
        if (skin != null) {
            ResolvableProfile currentProfile = npc.getProfile();
            if (currentProfile != null) {
                ResolvableProfile.Builder builder = ResolvableProfile.resolvableProfile()
                        .name(currentProfile.name())
                        .uuid(currentProfile.uuid());

                // Add the texture
                for (ProfileProperty property : currentProfile.properties()) {
                    if (!property.getName().equals("textures")) {
                        builder.addProperty(property);
                    }
                }
                builder.addProperty(new ProfileProperty("textures", skin.value(), skin.signature()));

                // Build the profile
                ResolvableProfile updatedProfile = builder.build();
                npc.setProfile(updatedProfile);
            }
        }
        npc.setImmovable(true);
        npc.setCustomNameVisible(false);
        npc.setAI(false);
        npc.setCollidable(false);

        // Apply the boss scale
        AttributeInstance scale = npc.getAttribute(Attribute.SCALE);
        if (scale != null) scale.setBaseValue(2.0);

        // Apply the boss health
        AttributeInstance maxHealth = npc.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(150);
            npc.setHealth(150);
        }

        // Create the bossbar
        bossBar = Bukkit.createBossBar(
                "BattleNPC",
                BarColor.RED,
                BarStyle.SOLID
        );
        bossBar.setProgress(1.0);

        // Start the tick task
        tickTask = new Task().sync().runTimer(this::tick, 0L, 1L);
    }

    Integer fallingBlocks;

    void tick() {
        this.updateBossBar();
        // Stop everything if the npc is null or death
        if (npc == null || npc.isDead()) {
            this.stop();
            return;
        }
        // Check if the mannequin is falling
        Location loc = npc.getLocation();
        boolean isAirBelow = loc.clone().subtract(0, .01, 0).getBlock().getType().isAir();

        if (isAirBelow) {
            // GRAVITY
            npc.teleport(loc.subtract(0, 0.2, 0));

            // Update the counter
            if (fallingBlocks == null) fallingBlocks = 1;
            else fallingBlocks++;
            return;
        } else {
            // NPC has landed
            if (fallingBlocks != null) {
                if (fallingBlocks > 2) new FallingAttack(this).execute();
                fallingBlocks = null;
            }
        }

        var player = Bukkit.getOnlinePlayers().stream().findFirst();

        if (Math.random() < .3) {
            new DashAttack(this, player.get()).execute();
        }
    }

    void updateBossBar() {
        if (bossBar == null || npc == null) return;
        // Configure the bossbar progress
        double maxHealth = 50;
        double currentHealth = npc.getHealth();
        double progress = Math.clamp(currentHealth / maxHealth, 0.0, 1.0);
        bossBar.setProgress(progress);

        // Add and remove the players in a radius of 50 blocks
        var players = new ArrayList<>();
        npc.getLocation().getNearbyPlayers(50).forEach(player -> {
            players.add(player);
            bossBar.addPlayer(player);
        });

        bossBar.getPlayers().forEach(player -> {
            if (!players.contains(player))
                bossBar.removePlayer(player);
        });
    }

    public void stop() {
        if (npc != null) npc.remove();
        if (tickTask != null && !tickTask.isCancelled()) {
            tickTask.cancel();
            tickTask = null;
        }
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
    }
}