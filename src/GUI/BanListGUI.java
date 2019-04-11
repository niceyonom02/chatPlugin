package GUI;


import main.ChatRoom;
import main.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static util.Util.getPlayerSkull;


public class BanListGUI implements Listener {
    private Main instance;
    private ChatRoom chatRoom;
    private ArrayList<UUID> banList;
    private Inventory banListInventory;
    public BanListGUI(Main instance, ChatRoom chatRoom){
        this.instance = instance;
        this.chatRoom = chatRoom;

        Bukkit.getPluginManager().registerEvents(this, instance);
        banList = chatRoom.getBanList();
        banListInventory = Bukkit.createInventory(null, 54, "§0벤 리스트");
        updateInventory();
    }

    public void openInventory(Player player){
        player.openInventory(banListInventory);
    }

    private void updateInventory() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, new Runnable() {
            @Override
            public void run() {
                if(banList.isEmpty()){
                    return;
                }

                for (int i = 0; i < banList.size(); i++) {
                    ItemStack skullPlayer = getPlayerSkull(banList.get(i));
                    banListInventory.setItem(i, skullPlayer);
                }
            }
        }, 0L, 10L);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player)){
            return;
        }

        if(e.getCurrentItem() == null){
            return;
        }

        if(e.getCurrentItem().getItemMeta() == null){
            return;
        }

        if(e.getCurrentItem().getItemMeta().getDisplayName() == null){
            return;
        }

        if(e.getCurrentItem().getItemMeta().getLore() == null){
            return;
        }

        Player player = (Player) e.getWhoClicked();

        if(e.getInventory().equals(banListInventory)){
            e.setCancelled(true);
            if(e.getClick().equals(ClickType.NUMBER_KEY)){
                e.setCancelled(true);
            }

            List<String> lore = e.getCurrentItem().getItemMeta().getLore();
            UUID uuid = UUID.fromString(lore.get(0));
            if(banList.contains(uuid)){
                chatRoom.unBan(player, uuid);
            } else{
                player.sendMessage("§f해당 유저는 §c벤 상태§f가 아닙니다!");
            }
            player.closeInventory();
        }
    }
}
