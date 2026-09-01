package nl.grapjeje.battleNPC;

import lombok.Getter;
import lombok.Setter;
import nl.grapjeje.battleNPC.attacks.FallingAttack;
import nl.grapjeje.battleNPC.commands.BattleNpcCommand;
import nl.grapjeje.core.Framework;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    @Getter
    static Framework framework;
    @Getter
    static Main instance;
    @Getter
    @Setter
    static BattleNpc boss;

    @Override
    public void onEnable() {
        instance = this;
        framework = Framework.init(this);

        // Commands
        framework.registerCommand(BattleNpcCommand::new);
    }

    @Override
    public void onDisable() {
        boss.stop();
        FallingAttack.getBlockCacheList().forEach(block -> {
            block.location().getBlock().setType(block.material());
        });

        instance = null;
    }
}
