package GUI;

import main.ChatRoom;
import main.RoomManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ManagementGUI implements Listener {
    private Inventory manageInventory;
    private RoomManager roomManager;

    public ManagementGUI(RoomManager roomManager){
        this.roomManager = roomManager;
        manageInventory = Bukkit.createInventory(null, 27, "채팅방 관리");
        setItem();
    }

    public void openInventory(Player player){
        player.openInventory(manageInventory);
    }

    public void setItem(){
        ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE);
        item.setDurability((short) 4);
        for(int i = 0; i < 9; i++){
            manageInventory.setItem(i, item);
        }

        for(int i = 1; i < 27; i++){
            manageInventory.setItem(i, item);
        }

        item.setDurability((short) 0);

        for(int i = 10; i <= 16; i += 2){
            manageInventory.setItem(i, item);
        }

        item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§d킥 §f하기");
        item.setItemMeta(meta);
        manageInventory.setItem(9, item);

        item = new ItemStack(Material.FIREBALL);
        meta = item.getItemMeta();
        meta.setDisplayName("§c벤 §f하기");
        item.setItemMeta(meta);
        manageInventory.setItem(11, item);

        item = new ItemStack(Material.PAPER);
        meta = item.getItemMeta();
        meta.setDisplayName("§c벤 §f리스트 관리");
        item.setItemMeta(meta);
        manageInventory.setItem(13, item);

        item = new ItemStack(Material.ICE);
        meta = item.getItemMeta();
        meta.setDisplayName("§f채팅방 §b얼리기 §f/ §6녹이기");
        item.setItemMeta(meta);
        manageInventory.setItem(15, item);

        item = new ItemStack(Material.BARRIER);
        meta = item.getItemMeta();
        meta.setDisplayName("§c채팅방 §f삭제하기");
        item.setItemMeta(meta);
        manageInventory.setItem(17, item);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player)){
            return;
        }

        if(e.getCurrentItem() == null){
            return;
        }

        Player player = (Player) e.getWhoClicked();

        if(e.getInventory().equals(manageInventory)){
            e.setCancelled(true);

            if(e.getClick().equals(ClickType.NUMBER_KEY)){
                e.setCancelled(true);
            }

            if(roomManager.getPlayerHostingRoom(player) != null){
                ChatRoom chatRoom = roomManager.getPlayerHostingRoom(player);

                if(e.getSlot() == 9){
                    chatRoom.process(player, "킥");
                } else if(e.getSlot() == 11){
                    chatRoom.process(player, "밴");
                } else if(e.getSlot() == 13){
                    chatRoom.process(player, "밴리스트");
                } else if(e.getSlot() == 15){
                    chatRoom.process(player, "얼림");
                } else if(e.getSlot() == 17){
                    chatRoom.process(player, "삭제");
                }
            }
        }
    }
}
