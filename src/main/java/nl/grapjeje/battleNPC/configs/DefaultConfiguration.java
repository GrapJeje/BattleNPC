package nl.grapjeje.battleNPC.configs;

import lombok.Getter;
import nl.grapjeje.core.Config;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;

@Getter
public class DefaultConfiguration extends Config {

    public record Skin(String signature, String value) {
    }
    Skin skin;

    public DefaultConfiguration(File folder) {
        super(folder, "config.yml", "config.yml", true);
    }

    @Override
    public void values() {
        // Get the skin values
        ConfigurationSection databaseSection = config.getConfigurationSection("skin");
        assert databaseSection != null;
        skin = new Skin(
                databaseSection.getString("signature"),
                databaseSection.getString("value")
        );
    }
}
