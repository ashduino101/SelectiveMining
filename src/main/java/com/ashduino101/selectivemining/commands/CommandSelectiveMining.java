package com.ashduino101.selectivemining.commands;

import com.ashduino101.selectivemining.PluginUtil;
import org.bukkit.ChatColor;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;

public class CommandSelectiveMining {
    public static void onCommand(CommandSender sender, @SuppressWarnings("unused") CommandArguments args) {
        Player player = (Player) sender;

        int slot = player.getInventory().getHeldItemSlot();
        ItemStack item = player.getInventory().getItem(slot);
        if (item == null) {
            sender.sendMessage(ChatColor.RED + "You are not holding an item!");
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            sender.sendMessage(ChatColor.RED + "Item does not have any metadata!");
            return;
        }

        String action = (String) args.get("action");
        switch (Objects.requireNonNull(action)) {
            case "enable":
                PluginUtil.setEnabled(meta, true);
                sender.sendMessage(ChatColor.YELLOW + "Enabled selective mining");
                break;
            case "disable":
                PluginUtil.setEnabled(meta, false);
                sender.sendMessage(ChatColor.YELLOW + "Disabled selective mining");
                break;
            case "allow":
            case "deny":
                BlockData data = (BlockData) args.get("material");
                if (data == null) {
                    sender.sendMessage(ChatColor.RED + "No material provided!");
                    return;
                }
                String material = data.getMaterial().getKey().toString();
                if (action.equals("allow")) {
                    boolean added = PluginUtil.toggleWhitelist(meta, material);
                    String addedRemoved = added ? "Added" : "Removed";
                    String toFrom = added ? "to" : "from";
                    sender.sendMessage(ChatColor.YELLOW + addedRemoved + " material " +
                            ChatColor.LIGHT_PURPLE + material +
                            ChatColor.YELLOW + " " + toFrom + " allow list");
                } else {
                    boolean added = PluginUtil.toggleBlacklist(meta, material);
                    String addedRemoved = added ? "Added" : "Removed";
                    String toFrom = added ? "to" : "from";
                    sender.sendMessage(ChatColor.YELLOW + addedRemoved + " material " +
                            ChatColor.LIGHT_PURPLE + material +
                            ChatColor.YELLOW + " " + toFrom + " deny list");
                }
                break;
            case "list":
                StringBuilder msg = new StringBuilder();
                List<String> whitelisted = PluginUtil.getWhitelist(meta);
                if (!whitelisted.isEmpty()) {
                    msg.append(ChatColor.YELLOW).append("This item allows the following materials:\n");
                }
                for (String mat : whitelisted) {
                    msg.append(" - ").append(ChatColor.GOLD).append(mat).append(ChatColor.YELLOW).append("\n");
                }
                List<String> blacklisted = PluginUtil.getBlacklist(meta);
                if (!blacklisted.isEmpty()) {
                    msg.append(ChatColor.YELLOW).append("This item denies the following materials:\n");
                }
                for (String mat : blacklisted) {
                    msg.append(" - ").append(ChatColor.GOLD).append(mat).append(ChatColor.YELLOW).append("\n");
                }
                if (whitelisted.isEmpty() && blacklisted.isEmpty()) {
                    msg.append(ChatColor.YELLOW).append("This item does not specify any materials\n");
                }
                sender.sendMessage(msg.toString().trim());
                break;
        }

        item.setItemMeta(meta);
        player.getInventory().setItem(slot, item);
    }
}