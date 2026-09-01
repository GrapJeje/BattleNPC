package nl.grapjeje.battleNPC;

import nl.grapjeje.battleNPC.attacks.FallingAttack;
import nl.grapjeje.battleNPC.utils.SkinUtil;
import nl.grapjeje.core.tasks.Task;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Mannequin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.Optional;

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
        npc.setGravity(true);

        // Apply the boss scale
        AttributeInstance scale = npc.getAttribute(Attribute.SCALE);
        if (scale != null) scale.setBaseValue(2.0);

        // Validate if the skin file exists
        File skinFile = SkinUtil.getSkinFile("boss_skin.png");
        if (skinFile == null || !skinFile.exists()) return;

        // Fetch skin asynchronously, then apply it back on the main thread
//        new Task().async().run(() -> {
//            try {
//                // TODO: SkinUtil must build a PlayerProfile with a signed
//                // "textures" property — the old NPC API's Skin/NpcSkin
//                // classes are no longer available for this.
//                Optional<PlayerProfile> profile = SkinUtil.fetchProfile(skinFile);
//                new Task().sync().run(() -> {
//                    if (profile.isEmpty()) return;
//                    npc.setPlayerProfile(profile.get());
//                });
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        });

        // Start the tick task
        tickTask = new Task().async().runTimer(this::tick, 0L, 1L);
    }

    Integer fallingBlocks;

    void tick() {
        // Check if the mannequin is falling / jumping
        if (npc.getLocation().add(0, -1, 0).getBlock().getType().isAir()) {
            if (fallingBlocks == null) fallingBlocks = 1;
            else fallingBlocks++;
        } else {
            if (fallingBlocks != null && fallingBlocks > 2)
                new FallingAttack(npc).execute();
                // Reset the progress if not floating anymore
            else fallingBlocks = null;
        }
    }
}