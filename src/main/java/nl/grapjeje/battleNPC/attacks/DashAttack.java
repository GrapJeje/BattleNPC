package nl.grapjeje.battleNPC.attacks;

import nl.grapjeje.battleNPC.BattleNpc;
import nl.grapjeje.battleNPC.Main;
import nl.grapjeje.battleNPC.utils.Attack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class DashAttack extends Attack {
    final Player player;
    BukkitTask dashTask;
    boolean damageDealt = false;

    public DashAttack(BattleNpc npc, Player player) {
        super(npc);
        this.player = player;
    }

    @Override
    public void execute() {
        if (npc.isAttacking()) return;

        Entity entity = npc.getNpc();
        if (entity == null || player == null || !player.isOnline()) return;

        npc.setAttacking(true);
        damageDealt = false;

        Location start = entity.getLocation();
        Location target = calculateTargetBehindPlayer(player);

        // Face the boss to the player
        this.facePlayer(entity, player);

        final Vector startVec = start.toVector();
        final Vector offset = target.toVector().subtract(startVec);

        dashTask = Bukkit.getScheduler().runTaskTimer(
                Main.getInstance(),
                new Runnable() {
                    int tick = 0;

                    @Override
                    public void run() {
                        // Validate the player existence
                        if (entity.isDead() || !player.isOnline()) {
                            cleanup();
                            return;
                        }

                        tick++;
                        double progress = Math.min(1.0, (double) tick / 10);

                        // Math shit
                        Location current = startVec.clone()
                                .add(offset.clone().multiply(progress))
                                .toLocation(start.getWorld());
                        current.setYaw(entity.getLocation().getYaw());
                        current.setPitch(entity.getLocation().getPitch());
                        entity.teleport(current);
                        facePlayer(entity, player);

                        // Add damage
                        if (!damageDealt && entity.getLocation().distance(player.getLocation()) <= 2.5) {
                            player.damage(4.0, entity);
                            damageDealt = true;
                        }
                        // Stop the dash
                        if (progress >= 1.0) cleanup();
                    }
                },
                0L, 1L
        );
    }

    Location calculateTargetBehindPlayer(Player player) {
        Location playerLoc = player.getLocation();
        Vector direction = playerLoc.getDirection().setY(0).normalize();
        Vector behind = direction.multiply(-2);
        Location target = playerLoc.clone().add(behind);
        target.setY(playerLoc.getY());
        return target;
    }

    void facePlayer(Entity entity, Player player) {
        Location entityLoc = entity.getLocation();
        Location playerLoc = player.getLocation();

        // Yaw
        Vector direction = playerLoc.toVector().subtract(entityLoc.toVector());
        direction.setY(0);
        if (direction.lengthSquared() > 0) {
            direction.normalize();
            float yaw = (float) Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));
            entity.setRotation(yaw, entity.getLocation().getPitch());
        }

        // Pitch
        double dx = playerLoc.getX() - entityLoc.getX();
        double dy = playerLoc.getY() - entityLoc.getY();
        double dz = playerLoc.getZ() - entityLoc.getZ();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        float pitch = (float) Math.toDegrees(Math.atan2(-dy, horizontalDistance));
        entity.setRotation(entity.getLocation().getYaw(), pitch);
    }

    @Override
    protected void cleanup() {
        if (dashTask != null && !dashTask.isCancelled())
            dashTask.cancel();
        super.cleanup();
    }
}