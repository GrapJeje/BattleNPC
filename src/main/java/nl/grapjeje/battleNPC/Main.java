package nl.grapjeje.battleNPC;

import lombok.Getter;
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

    }

    @Override
    public void onDisable() {
        instance = null;
    }
}
