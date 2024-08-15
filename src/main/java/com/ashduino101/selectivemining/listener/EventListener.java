package com.ashduino101.selectivemining.listener;

import static com.ashduino101.selectivemining.SelectiveMining.plugin;

import com.ashduino101.selectivemining.PluginUtil;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EventListener implements Listener {
    boolean shouldAllowBreak(ItemMeta meta, @NotNull BlockData data) {
        if (!PluginUtil.enabledForItem(meta)) return true;

        List<String> whitelist = PluginUtil.getWhitelist(meta);
        List<String> blacklist = PluginUtil.getBlacklist(meta);

        // TODO: match properties and allow tags
        // Should be lossless from the original data -- we store it as a string on the item,
        // so this makes it easier to compare the whitelist/blacklist values with this block
//        String blockData = data.getAsString(true);
        String blockData = data.getMaterial().getKey().toString();

        boolean blockIsAllowed = false;
        if (!whitelist.isEmpty()) {
            // If the whitelist contains entries, use that first
            if (whitelist.contains(blockData)) {
                blockIsAllowed = true;
            }
        } else {
            // Empty whitelist, allow all by default
            blockIsAllowed = true;
        }
        // Apply the blacklist (note: this will cancel out any values in the whitelist)
        if (!blacklist.isEmpty()) {
            if (blacklist.contains(blockData)) {
                blockIsAllowed = false;
            }
        }
        return blockIsAllowed;
    }

    void removeAndRestorePreviousFatigue(@NotNull Player player) {
        player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
        PersistentDataContainer container = player.getPersistentDataContainer();
        container.set(new NamespacedKey(plugin, "fatigued"), PersistentDataType.BOOLEAN, false);
        // TODO: if the player previously had a different level of mining fatigue, we should re-apply it after
        //  clearing this one
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onSlotChanged(@NotNull PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        if (newItem == null || !PluginUtil.enabledForItem(newItem.getItemMeta())) {
            this.removeAndRestorePreviousFatigue(player);
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onBlockBroken(@NotNull BlockBreakEvent event) {
        Player player = event.getPlayer();

        ItemStack tool = player.getInventory().getItemInMainHand();
        ItemMeta meta = tool.getItemMeta();
        if (meta == null) return;

        Block block = event.getBlock();
        BlockData data = block.getBlockData();

        if (!shouldAllowBreak(meta, data)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        ItemMeta meta = tool.getItemMeta();
        if (meta == null) return;
        if (PluginUtil.enabledForItem(meta)) {
            if (event.getClickedBlock() == null) return;

            BlockData target = event.getClickedBlock().getBlockData();

            // Process based on if this block is allowed
            if (shouldAllowBreak(meta, target)) {
                if (player.getPersistentDataContainer()
                        .getOrDefault(new NamespacedKey(plugin, "fatigued"),
                                PersistentDataType.BOOLEAN, false)) {
                    this.removeAndRestorePreviousFatigue(player);
                }
            } else {
                PersistentDataContainer container = player.getPersistentDataContainer();

                container.set(new NamespacedKey(plugin, "fatigued"), PersistentDataType.BOOLEAN, true);

                player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, PotionEffect.INFINITE_DURATION, 255,
                        false, false, false));
            }
        }
    }
}
