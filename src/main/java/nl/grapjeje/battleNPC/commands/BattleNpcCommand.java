package nl.grapjeje.battleNPC.commands;

import nl.grapjeje.battleNPC.BattleNpc;
import nl.grapjeje.battleNPC.Main;
import nl.grapjeje.core.Framework;
import nl.grapjeje.core.command.CommandSourceStack;
import nl.grapjeje.core.command.CreditCommand;
import nl.grapjeje.core.player.CorePlayer;
import nl.grapjeje.core.tasks.Task;
import org.bukkit.entity.Player;

public class BattleNpcCommand implements CreditCommand {

    @Override
    public boolean extraExecute(CommandSourceStack source, String[] args) {
        if (args.length == 0) return false;

        Player player = source.getPlayer();
        if (player == null) {
            source.getSender().sendMessage("Dit command kan alleen door een speler gebruikt worden.");
            return true;
        }

        if (args[0].toLowerCase().trim().equals("spawn")) {
            new BattleNpc(player.getLocation());
            return true;
        }

        CorePlayer corePlayer = CorePlayer.get(player);
        new Task().sync().runLater(() ->
                corePlayer.sendMessage("<gray><italic>If you want to spawn the boss, do /battlenpc spawn!"), 5L);
        return false;
    }

    @Override
    public String getColor() {
        return "<red>";
    }

    @Override
    public String getDescription() {
        return "A custom boss npc plugin";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public Framework getFramework() {
        return Main.getFramework();
    }

    @Override
    public String getName() {
        return "BattleNpc";
    }
}
