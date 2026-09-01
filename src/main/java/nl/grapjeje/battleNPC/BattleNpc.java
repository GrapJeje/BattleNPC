package nl.grapjeje.battleNPC;

import nl.grapjeje.battleNPC.attacks.FallingAttack;
import nl.grapjeje.core.tasks.Task;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Mannequin;
import org.bukkit.scheduler.BukkitTask;

public class BattleNpc {
    Mannequin npc;

    BukkitTask tickTask;

    public BattleNpc(Location location) {
        location = location.add(0, 5, 0);

        // Spawning an entity must happen on the main thread
        npc = location.getWorld().spawn(location, Mannequin.class);
        npc.setImmovable(true);
        npc.setCustomNameVisible(false);
        npc.setAI(false);
        npc.setCollidable(false);

        // Apply the boss scale
        AttributeInstance scale = npc.getAttribute(Attribute.SCALE);
        if (scale != null) scale.setBaseValue(2.0);

        // Start the tick task
        tickTask = new Task().sync().runTimer(this::tick, 0L, 1L);
    }

    Integer fallingBlocks;

    void tick() {
        // Check if the mannequin is falling / jumping
        Location loc = npc.getLocation();
        boolean isAirBelow = loc.clone().subtract(0, .01, 0).getBlock().getType().isAir();

        if (isAirBelow) {
            // GRAVITY
            npc.teleport(loc.subtract(0, 0.2, 0));

            // Update the counter
            if (fallingBlocks == null) fallingBlocks = 1;
            else fallingBlocks++;
        } else {
            // NPC has landed
            if (fallingBlocks != null) {
                if (fallingBlocks > 2) new FallingAttack(npc).execute();
                fallingBlocks = null;
            }
        }
    }

    public void stop() {
        if (npc != null) npc.remove();
        if (tickTask != null && !tickTask.isCancelled()) {
            tickTask.cancel();
            tickTask = null;
        }
    }
}