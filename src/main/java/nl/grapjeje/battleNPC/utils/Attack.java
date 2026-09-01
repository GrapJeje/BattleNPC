package nl.grapjeje.battleNPC.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.grapjeje.battleNPC.BattleNpc;
import org.bukkit.entity.Mannequin;

@RequiredArgsConstructor
@Getter
public abstract class Attack {
    protected final BattleNpc npc;

    public abstract void execute();

    protected void cleanup() {
        npc.setAttacking(false);
    }
}