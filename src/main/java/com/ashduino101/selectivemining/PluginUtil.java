package com.ashduino101.selectivemining;

import static com.ashduino101.selectivemining.SelectiveMining.plugin;

import com.google.common.collect.Lists;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class PluginUtil {
    public static void setEnabled(ItemMeta im, boolean isEnabled) {
        im.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "enabled"), PersistentDataType.BOOLEAN, isEnabled);
    }

    public static boolean enabledForItem(ItemMeta im) {
        if (im == null) return false;
        return im.getPersistentDataContainer().getOrDefault(
                new NamespacedKey(plugin, "enabled"),
                PersistentDataType.BOOLEAN,
                false
        );
    }

    public static boolean toggleWhitelist(ItemMeta im, String block) {
        // We need to create a new ArrayList for it to be mutable
        List<String> container = Lists.newArrayList(im.getPersistentDataContainer().getOrDefault(
                new NamespacedKey(plugin, "allow"),
                PersistentDataType.LIST.strings(),
                new ArrayList<>()
        ));
        boolean removed = container.contains(block);
        if (removed) {
            container.remove(block);
        } else {
            container.add(block);
        }

        im.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "allow"), PersistentDataType.LIST.strings(), container);

        return !removed;
    }

    public static boolean toggleBlacklist(ItemMeta im, String block) {
        List<String> container = Lists.newArrayList(im.getPersistentDataContainer().getOrDefault(
                new NamespacedKey(plugin, "deny"),
                PersistentDataType.LIST.strings(),
                new ArrayList<>()
        ));
        boolean removed = container.contains(block);
        if (removed) {
            container.remove(block);
        } else {
            container.add(block);
        }

        im.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "deny"), PersistentDataType.LIST.strings(), container);

        return !removed;
    }

    public static List<String> getWhitelist(ItemMeta im) {
        return im.getPersistentDataContainer().getOrDefault(
                new NamespacedKey(plugin, "allow"),
                PersistentDataType.LIST.strings(),
                new ArrayList<>()
        );
    }

    public static List<String> getBlacklist(ItemMeta im) {
        return im.getPersistentDataContainer().getOrDefault(
                new NamespacedKey(plugin, "deny"),
                PersistentDataType.LIST.strings(),
                new ArrayList<>()
        );
    }
}