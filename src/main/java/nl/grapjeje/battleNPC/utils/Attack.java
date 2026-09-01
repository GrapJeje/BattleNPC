package nl.grapjeje.battleNPC.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Mannequin;

@RequiredArgsConstructor
@Getter
public abstract class Attack {
    protected final Mannequin npc;

    public abstract void execute();
}