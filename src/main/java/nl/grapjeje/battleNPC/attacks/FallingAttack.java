package nl.grapjeje.battleNPC.attacks;

import nl.grapjeje.battleNPC.utils.Attack;
import nl.grapjeje.core.tasks.Task;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Mannequin;

import java.util.ArrayList;
import java.util.List;

public class FallingAttack extends Attack {
    List<BlockCache> blockCacheList = new ArrayList<>();

    public FallingAttack(Mannequin npc) {
        super(npc);
    }

    @Override
    public void execute() {
        Location location = npc.getLocation();

        new Task().async().run(() -> {
            // Get random blocks to transform
            int radius = 6;
            long timestamp = System.currentTimeMillis();

            List<Block> blocks = new java.util.ArrayList<>();

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Block block = location.getBlock().getRelative(x, y, z);

                        if (block.getLocation().distanceSquared(location) <= radius * radius)
                            blocks.add(block);
                    }
                }
            }
            // Shuffle the blocks randomly
            java.util.Collections.shuffle(blocks);
            // Select 60% of the blocks
            int amount = (int) (blocks.size() * 0.6);
            for (int i = 0; i < amount; i++) {
                blockCacheList.add(new BlockCache(
                        blocks.get(i).getLocation(), blocks.get(i).getType(), timestamp));
            }

            new Task().sync().run(() -> {
                // Transfer all those blocks into magma
                blockCacheList.forEach(block -> {
                    Block blockToTransform = npc.getLocation().getWorld().getBlockAt(block.location);
                    blockToTransform.setType(Material.MAGMA_BLOCK);

                    // Have a 50% chance to add smoke above the block
                    if (Math.random() < .5) {
                        Location smokeLocation = blockToTransform.getLocation().add(0.5, 1.0, 0.5);

                        blockToTransform.getWorld().spawnParticle(
                                Particle.LARGE_SMOKE,
                                smokeLocation,
                                5,
                                0.2,
                                0.2,
                                0.2,
                                0.01
                        );
                    }
                });
                location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.7f);
            });

            new Task().sync().runLater(this::cleanup, 120L);
        });
    }

    void cleanup() {
        blockCacheList.forEach(block -> {
            Block blockToTransform = npc.getLocation().getWorld().getBlockAt(block.location);
            blockToTransform.setType(block.material);

            // Have a 10% chance to add a sound to the block
            if (Math.random() < .1)
                block.location.getWorld().playSound(block.location, Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f);
        });
    }

    public record BlockCache(Location location, Material material, Long timestamp) {
    }
}