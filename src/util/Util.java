package util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.SkullType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.UUID;

public class Util {

    public static ItemStack getPlayerSkull(UUID uuid) {
        ItemStack playerSkull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta meta = (SkullMeta) playerSkull.getItemMeta();

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

        String name = offlinePlayer.getName();
        String id = offlinePlayer.getUniqueId().toString();
        ArrayList<String> lore = new ArrayList<>();
        lore.add(id);

        meta.setOwningPlayer(offlinePlayer);
        meta.setDisplayName(name);
        meta.setLore(lore);

        playerSkull.setItemMeta(meta);

        return playerSkull;
    }
}
