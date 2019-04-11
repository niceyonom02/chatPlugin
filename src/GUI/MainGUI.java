package GUI;

import main.ChatRoom;
import main.Main;
import main.RoomManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MainGUI implements Listener {
    private Main instance;
    private RoomManager roomManager;
    private List<ChatRoom> chatRooms;
    private ArrayList<Inventory> inventories;
    private String mainName = "§0채팅방 로비";


    private int updateTask;

    public MainGUI(Main instance, RoomManager roomManager) {
        this.roomManager = roomManager;
        this.instance = instance;
        inventories = new ArrayList<>();
        chatRooms = roomManager.getRoomList();

        makeFirstPage();
        update();
    }

    public void makeFirstPage(){
        Inventory inventory = Bukkit.createInventory(null, 54, mainName);
        setCommonItems(inventory);
        inventories.add(inventory);
    }

    public void setItems() {
        List<ChatRoom> roomList = roomManager.getRoomList();

        int page = 0;
        int countInPage = 0;

        for(Inventory inventory : inventories){
            for(int i = 0; i < 45; i++){
                inventory.setItem(i, null);
            }
        }

        for (int i = 0; i < roomList.size(); i++) {
            if (i % 45 == 0 && i != 0) {
                page++;
                countInPage = 0;
            }

            ArrayList<String> lore = new ArrayList<>();
            ItemStack item = roomList.get(i).getRoomIcon();
            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName("§6" + roomList.get(i).getRoomName());
            lore.add("§7" + roomList.get(i).getLore());
            lore.add("");
            lore.add("§f호스트: §7" + roomList.get(i).getHost());
            lore.add("§f인원: §a" + roomList.get(i).getCurrentMembersize() + " §7/ §c" + roomList.get(i).getMaxmembersize());
            lore.add("");
            if(chatRooms.get(i).getIsPublic()){
                lore.add("§f공개여부: §a공개");
            } else{
                lore.add("§f공개여부: §c비공개");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);

            inventories.get(page).setItem(countInPage, item);
            countInPage++;
        }
    }

    public void setPreviousItem(Inventory inventory) {
        ItemStack previousPage = new ItemStack(Material.PAPER);
        ItemMeta previousMeta = previousPage.getItemMeta();
        previousMeta.setDisplayName("§a이전 §f페이지");
        previousPage.setItemMeta(previousMeta);

        inventory.setItem(48, previousPage);
    }

    public void setCommonItems(Inventory inventory) {

        ItemStack leave = new ItemStack(Material.BARRIER);
        ItemMeta leaveMeta = leave.getItemMeta();
        leaveMeta.setDisplayName("§c채팅방 §f나가기");
        leave.setItemMeta(leaveMeta);
        inventory.setItem(45, leave);

        ItemStack createChatRoom = new ItemStack(Material.NETHER_STAR);
        ItemMeta createMeta = createChatRoom.getItemMeta();
        createMeta.setDisplayName("§6채팅방 §f생성");
        createChatRoom.setItemMeta(createMeta);
        inventory.setItem(49, createChatRoom);


        ItemStack manageChatRoom = new ItemStack(Material.BOOK);
        ItemMeta manageMeta = manageChatRoom.getItemMeta();
        manageMeta.setDisplayName("§e채팅방 §f관리");
        manageChatRoom.setItemMeta(manageMeta);
        inventory.setItem(53, manageChatRoom);
    }

    public void setNextItem(Inventory inventory) {
        ItemStack nextPage = new ItemStack(Material.PAPER);
        ItemMeta nextMeta = nextPage.getItemMeta();
        nextMeta.setDisplayName("§c다음 §f페이지");
        nextPage.setItemMeta(nextMeta);

        inventory.setItem(50, nextPage);
    }

    public void addInventory() {
        Inventory beforeLastPage = inventories.get(inventories.size() - 1);
        setNextItem(beforeLastPage);

        Inventory currentLastpage = Bukkit.createInventory(null, 54, mainName);
        inventories.add(currentLastpage);
        setPreviousItem(currentLastpage);
        setCommonItems(currentLastpage);
    }

    public void removeInventory() {
        inventories.remove(inventories.size() - 1);
        inventories.get(inventories.size() - 1).setItem(53, null);
    }

    private int calcPage() {
        if (chatRooms.isEmpty()) {
            return 1;
        }

        int inventorySize = 45;

        int pages = (chatRooms.size() % inventorySize == 0 ? (chatRooms.size() / inventorySize) : (chatRooms.size() / inventorySize) + 1);
        return pages;
    }

    private void update() {
        updateTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, new Runnable() {
            @Override
            public void run() {
                chatRooms = roomManager.getRoomList();

                if (calcPage() > inventories.size()) {
                    for (int i = inventories.size(); i < calcPage(); i++) {
                        addInventory();
                    }
                } else if (calcPage() < inventories.size()) {
                    for (int i = (inventories.size() - 1); i > calcPage(); i--) {
                        removeInventory();
                    }
                }
                setItems();
            }
        }, 0L, 10L);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if(!(e.getWhoClicked() instanceof Player)){
            return;
        }

        if(e.getCurrentItem() == null){
            return;
        }

        Player player = (Player) e.getWhoClicked();

        if (e.getInventory().getName().equalsIgnoreCase(mainName)) {
            e.setCancelled(true);

            if (e.getClick().equals(ClickType.NUMBER_KEY)) {
                e.setCancelled(true);
            }

            if(roomManager.isMaking(player)){
                player.closeInventory();
                player.sendMessage("§6채팅방 제작§f을 먼저 완료해주세요!");
                return;
            }

            if(e.getSlot() == 45 && e.getCurrentItem().getType().equals(Material.BARRIER)){
                roomManager.leaveRoom(player);
            } else if(e.getSlot() == 48 && e.getCurrentItem().getType().equals(Material.PAPER)){
                Inventory inventory = e.getClickedInventory();
                if(inventories.get(inventories.indexOf(inventory) - 1) != null){
                    player.closeInventory();
                    player.openInventory(inventories.get(inventories.indexOf(inventory) - 1));
                }
            } else if (e.getSlot() == 49 && e.getCurrentItem().getType().equals(Material.NETHER_STAR)) {
                if(roomManager.getPlayerInChatRoom(player) != null){
                    player.closeInventory();
                    player.sendMessage("§c이미 참여 중인 채팅방이 있습니다!");
                    return;
                }

                if(roomManager.isPlayerWaiting(player)){
                    player.closeInventory();
                    player.sendMessage("§c이미 접속대기 중인 채팅방이 있습니다!");
                    return;
                }

                if(roomManager.getPlayerHostingRoom(player) != null){
                    player.closeInventory();
                    player.sendMessage("§c이미 호스팅 중인 채팅방이 있습니다!");
                    return;
                }

                CreateRoomGUI cg = new CreateRoomGUI(instance, roomManager);
                player.closeInventory();
                cg.openInventory(player);
            } else if(e.getSlot() == 50 && e.getCurrentItem().getType().equals(Material.PAPER)){
                Inventory inventory = e.getClickedInventory();
                if(inventories.get(inventories.indexOf(inventory) + 1) != null){
                    player.closeInventory();
                    player.openInventory(inventories.get(inventories.indexOf(inventory) + 1));
                }
            } else if (e.getSlot() == 53 && e.getCurrentItem().getType().equals(Material.BOOK)) {
                if(roomManager.getPlayerHostingRoom(player) != null){

                    if(roomManager.isPlayerWaiting(player)){
                        player.closeInventory();
                        player.sendMessage("§c이미 접속대기 중인 채팅방이 있습니다!");
                        return;
                    }

                    player.closeInventory();
                    instance.getManagementGUI().openInventory(player);
                } else{
                    player.sendMessage("§c호스팅 중인 채팅방이 없습니다!");
                }
            }  else{
                if(e.getCurrentItem().getItemMeta() != null){
                    if(e.getCurrentItem().getItemMeta().getDisplayName() != null){
                        String name = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());

                        if(player.isOp()){
                            if(e.isShiftClick()){
                                roomManager.deleteRoom(name);
                                player.sendMessage("§6" + name + "§7채팅방을 삭제하였습니다!");
                                return;
                            }
                        }
                        roomManager.joinRoom(player, name);
                    }
                }

            }
        }
    }

    public void openMainMenu(Player player){
        player.openInventory(inventories.get(0));
    }

}
