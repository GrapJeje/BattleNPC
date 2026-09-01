package nl.grapjeje.battleNPC.utils;

import de.eisi05.npc.api.objects.Skin;
import lombok.experimental.UtilityClass;
import nl.grapjeje.battleNPC.Main;
import nl.grapjeje.core.tasks.Task;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.jar.JarFile;

@UtilityClass
public class SkinUtil {
    static final int MAX_RETRIES = 3;
    static final long RETRY_DELAY_MS = 2000;

    @SuppressWarnings("unchecked")
    public static void fetchSkinAsync(File file, Consumer callback) {
        fetchSkinAsync(file, 0, callback);
    }

    @SuppressWarnings("unchecked")
    static void fetchSkinAsync(File file, int attempt, Consumer callback) {
        new Task().async().run(() -> {
            try {
                if (file == null || !file.exists()) {
                    callback.accept(fallbackSkin());
                    return;
                }

                Optional<Skin> skin = Skin.fetchSkin(file);
                if (skin.isPresent()) {
                    callback.accept(skin);
                    return;
                }

                Bukkit.getLogger().warning("Skin fetch failed (attempt " + (attempt + 1) + ")");

                if (attempt < MAX_RETRIES) {
                    try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ignored) {}
                    fetchSkinAsync(file, attempt + 1, callback);
                } else callback.accept(fallbackSkin());

            } catch (Exception e) {
                if (attempt < MAX_RETRIES) {
                    try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ignored) {}
                    fetchSkinAsync(file, attempt + 1, callback);
                } else {
                    callback.accept(fallbackSkin());
                }
            }
        });
    }

    public static File getSkinFile(String fileName) {
        File skinFolder = new File(Main.getInstance().getDataFolder(), "npc_skins");

        if (!skinFolder.exists())
            skinFolder.mkdirs();
        return new File(skinFolder, fileName);
    }

    public static void saveAllSkins() {
        String folderInJar = "npc_skins/";
        File skinFolder = new File(Main.getInstance().getDataFolder(), "npc_skins");
        if (!skinFolder.exists()) skinFolder.mkdirs();

        try {
            String path = Main.getInstance().getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            JarFile jar = new JarFile(path);

            jar.stream()
                    .filter(entry -> entry.getName().startsWith(folderInJar))
                    .forEach(entry -> {
                        try {
                            String relativePath = entry.getName().substring(folderInJar.length());
                            File outFile = new File(skinFolder, relativePath);

                            if (entry.isDirectory()) {
                                outFile.mkdirs();
                                return;
                            }
                            outFile.getParentFile().mkdirs();
                            if (!outFile.exists()) {
                                try (InputStream is = jar.getInputStream(entry)) {
                                    Files.copy(is, outFile.toPath());
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
            jar.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static Optional<Skin> fallbackSkin() {
        try {
            return Skin.fetchSkin("notch");
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
