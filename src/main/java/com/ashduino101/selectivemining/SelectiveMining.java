package com.ashduino101.selectivemining;

import com.ashduino101.selectivemining.commands.*;
import com.ashduino101.selectivemining.listener.*;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import dev.jorel.commandapi.*;
import dev.jorel.commandapi.arguments.*;

public final class SelectiveMining extends JavaPlugin {
    public static SelectiveMining plugin;

    @Override
    public void onLoad() {
        plugin = this;
        CommandAPI.onLoad(new CommandAPIBukkitConfig(plugin));
    }

    @Override
    public void onEnable() {
        final PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(new EventListener(), plugin);

        CommandAPI.onEnable();

        new CommandAPICommand("selectivemining")
                .withFullDescription("Manages selective mining for a tool or item.")
                .withPermission("selectivemining.select")
                .withAliases("sm")
                .withArguments(
                        new MultiLiteralArgument("action", "enable", "disable", "allow", "deny", "list")
                )
                .withOptionalArguments(
                        new BlockStateArgument("material")
                )
                .executes(CommandSelectiveMining::onCommand)
                .register();
    }
}