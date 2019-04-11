package GUI;

import main.ChatRoom;
import main.Main;
import main.RoomManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static util.Util.getPlayerSkull;

public class UserListGUI implements Listener{
    private Main instance;
    private ChatRoom chatRoom;
    private Inventory currentMemberInventory;
    private ArrayList<UUID> userList;
    private HashMap<String, String> behavior = new HashMap<>();
    private RoomManager roomManager;

    public UserListGUI(Main instance, RoomManager roomManager, ChatRoom chatRoom){
        this.instance = instance;
        this.roomManager = roomManager;
        this.chatRoom = chatRoom;
        currentMemberInventory = Bukkit.createInventory(null, 54, chatRoom.getRoomName());

        Bukkit.getPluginManager().registerEvents(this, instance);

        userList = chatRoom.getUserList();
        updateInventory();
    }

    public void openInventory(Player player){
        player.openInventory(currentMemberInventory);
    }

    public void initializeBehavior(String identifier){
        behavior.put(identifier, "없음");
    }

    public boolean isBehaviorEmpty(){
        return behavior.isEmpty();
    }


    private void updateInventory() {
        userList = chatRoom.getUserList();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, new Runnable() {
            @Override
            public void run() {
                currentMemberInventory.clear();
                for (int i = 0; i < userList.size(); i++) {
                    ItemStack skullPlayer = getPlayerSkull(userList.get(i));
                    currentMemberInventory.setItem(i, skullPlayer);
                }
            }
        }, 0L, 10L);
    }

    @EventHandler
    public void onDeleteChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        if (!behavior.isEmpty()) {
            if(chatRoom.getHost().equalsIgnoreCase(player.getName())){
                e.setCancelled(true);

                if(behavior.containsKey("킥")){
                    if(behavior.get("킥").equalsIgnoreCase("없음")){
                        return;
                    }
                } else if(behavior.containsKey("밴")){
                    if(behavior.get("밴").equalsIgnoreCase("없음")){
                        return;
                    }
                }

                if (e.getMessage().equalsIgnoreCase("취소")) {
                    instance.getManagementGUI().openInventory(player);
                    behavior.clear();
                    return;
                }

                if (e.getMessage().equalsIgnoreCase("확인")) {
                    if (behavior.containsKey("삭제")) {
                        roomManager.deleteRoom(chatRoom.getRoomName());
                        player.sendMessage("§6채팅방§f을 §c삭제§f하였습니다!");
                    } else if (behavior.containsKey("킥")) {
                        if(Bukkit.getPlayer(behavior.get("킥")) != null){
                            chatRoom.kick(player, Bukkit.getPlayer(behavior.get("킥")));
                            player.sendMessage("§6" + behavior.get("킥") + "§f님을 킥하였습니다!");
                        } else{
                            player.sendMessage("§c해당 유저는 오프라인입니다!");
                        }
                    } else if (behavior.containsKey("밴")) {
                        if(Bukkit.getPlayer(behavior.get("밴")) != null){
                            chatRoom.ban(player, Bukkit.getPlayer(behavior.get("밴")));
                        } else{
                            player.sendMessage("§c해당 유저는 오프라인입니다!");
                        }
                    }
                    behavior.clear();
                } else {
                    player.sendMessage("§a'확인' §f또는 §c'취소' §f를 입력해주세요!");
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e){
        if(e.getInventory().equals(currentMemberInventory)){
            if(behavior.containsKey("킥")){
                if(behavior.get("킥").equalsIgnoreCase("없음")){
                    behavior.clear();
                }
            } else if(behavior.containsKey("밴")){
                if(behavior.get("밴").equalsIgnoreCase("없음")){
                    behavior.clear();
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        if(chatRoom.isHost(e.getPlayer())){
            behavior.clear();
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }

        if (e.getCurrentItem() == null) {
            return;
        }

        Player player = (Player) e.getWhoClicked();

        if (e.getClickedInventory().getName().equalsIgnoreCase(chatRoom.getRoomName())) {
            e.setCancelled(true);

            if (e.getClick().equals(ClickType.NUMBER_KEY)) {
                e.setCancelled(true);
            }

            if(e.getCurrentItem().getItemMeta() != null){
                if(e.getCurrentItem().getItemMeta().getDisplayName() != null){
                    String name = e.getCurrentItem().getItemMeta().getDisplayName();

                    if(name.equalsIgnoreCase(player.getName())){
                        player.sendMessage("§f자기자신은 §6킥§f하거나 §c밴§f할 수 없습니다!");
                        behavior.clear();
                        player.closeInventory();
                        return;
                    }

                    Player target = Bukkit.getPlayer(name);

                    if(target != null){
                        UUID uuid = target.getUniqueId();

                        if(chatRoom.isPlayerinChatRoom(uuid)){
                            if(behavior.containsKey("킥")){
                                behavior.put("킥", name);
                                player.closeInventory();
                                player.sendMessage("§f해당 유저를 킥하려면 §a'확인'");
                                player.sendMessage("§f취소하려면 '취소'를 쳐주세요.");
                            } else if(behavior.containsKey("밴")){
                                behavior.put("밴", name);
                                player.closeInventory();
                                player.sendMessage("§f해당 유저를 벤하려면 §a'확인'");
                                player.sendMessage("§f취소하려면 '취소'를 쳐주세요.");
                            }
                        } else{
                            player.sendMessage("§f해당 유저는 §a채팅방§f에 접속중이지 않습니다!");
                        }
                    } else{
                        player.sendMessage("§c해당 유저는 오프라인입니다!");
                    }
                }
            }
        }
    }



}
