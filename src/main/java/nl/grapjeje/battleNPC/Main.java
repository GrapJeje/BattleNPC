package nl.grapjeje.battleNPC;

import lombok.Getter;
import nl.grapjeje.battleNPC.commands.BattleNpcCommand;
import nl.grapjeje.battleNPC.utils.SkinUtil;
import nl.grapjeje.core.Framework;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    @Getter
    private static Framework framework;
    @Getter
    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;
        framework = Framework.init(this);

        // Save all the skin files
        SkinUtil.saveAllSkins();

        // Commands
        framework.registerCommand(BattleNpcCommand::new);
    }

    @Override
    public void onDisable() {
        instance = null;
    }
}
