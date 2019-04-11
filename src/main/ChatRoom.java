package main;

import GUI.BanListGUI;
import GUI.UserListGUI;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.SkullType;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.net.URL;

import org.json.simple.parser.ParseException;

import java.util.*;

import static util.Util.getPlayerSkull;

public class ChatRoom implements Listener {

    public ChatRoom(String name, int maxMembers, String host, Main instance, ItemStack icon, String lore, boolean ispublic, ArrayList<UUID> banlist, RoomManager roomManager, String password) {

        this.roomName = name;
        this.maxMembers = maxMembers;
        this.host = host;
        this.instance = instance;
        this.roomIcon = icon;
        this.lore = lore;
        this.ispublic = ispublic;
        this.banList = banlist;
        this.roomManager = roomManager;

        Random random = new Random();

        if(password == null){
            this.password = String.valueOf(random.nextInt(1000000));
        } else{
            this.password = password;
        }

        Bukkit.getPluginManager().registerEvents(this, instance);

        if (Bukkit.getPlayer(host) != null) {
            Player player = Bukkit.getPlayer(host);

            members.add(player.getUniqueId());
            if (!ispublic) {
                player.sendMessage("§f현재 방의 비밀번호는 §e" + this.password + "§f입니다!");
            }
        }

        userListGUI = new UserListGUI(instance, roomManager, this);
        banListGUI = new BanListGUI(instance, this);
        updateIcon();
    }

    private Main instance;
    private UserListGUI userListGUI;
    private BanListGUI banListGUI;
    private RoomManager roomManager;

    private String roomName;
    private String lore;
    private ItemStack roomIcon;
    private ArrayList<UUID> members = new ArrayList<>();
    private int maxMembers;
    private boolean ispublic;
    private String host;


    private ArrayList<UUID> banList;
    private ArrayList<String> joinWaitList = new ArrayList<>();
    private ArrayList<String> confirmedList = new ArrayList<>();

    private HashMap<String, String> behavior = new HashMap<>();

    private boolean freeze = false;
    private String password;

    public ArrayList<UUID> getBanList() {
        return banList;
    }

    public ArrayList<UUID> getUserList() {
        return members;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getLore() {
        return lore;
    }

    public boolean isPlayerinChatRoom(Player player) {
        return members.contains(player.getUniqueId());
    }

    public boolean isPlayerinChatRoom(UUID uuid) {
        return members.contains(uuid);
    }

    public boolean isRoomFull() {
        return members.size() >= maxMembers;
    }

    public String getPassword() {
        return password;
    }

    public ItemStack getRoomIcon() {
        return roomIcon;
    }

    public boolean getIsPublic() {
        return ispublic;
    }

    public String getHost() {
        return host;
    }

    public int getCurrentMembersize() {
        return members.size();
    }

    public int getMaxmembersize() {
        return maxMembers;
    }

    public boolean isHost(Player player) {
        return host.equals(player.getName());
    }

    public boolean isBanned(UUID uuid) {
        return banList.contains(uuid);
    }

    public void join(Player player) {
        if (isBanned(player.getUniqueId())) {
            player.sendMessage("§e해당 채팅방에서 벤 되어 참가하실 수 없습니다!");
            player.closeInventory();
            return;
        }
        if (!ispublic && !joinWaitList.contains(player.getName()) && !confirmedList.contains(player.getName())) {
            player.closeInventory();
            player.sendMessage("§e채팅방의 비밀번호를 쳐주세요!");
            joinWaitList.add(player.getName());
            return;
        }
        if (joinWaitList.contains(player.getName()) && !confirmedList.contains(player.getName())) {
            player.closeInventory();
            player.sendMessage("§e채팅방의 비밀번호를 쳐주세요!");
            return;
        }

        player.closeInventory();
        members.add(player.getUniqueId());
        updateIcon();

        for (UUID uuid : members) {
            Player receiver = Bukkit.getPlayer(uuid);
            if (receiver != null) {
                receiver.sendMessage("§b[§a+§b] §7" + player.getName());
            }
        }
    }

    public boolean isPlayerInWaitList(Player player) {
        return joinWaitList.contains(player.getName());
    }

    public boolean isPlayerInConfirmedList(Player player) {
        return confirmedList.contains(player.getName());
    }

    public void leave(Player player) {
        if (members.contains(player.getUniqueId())) {
            members.remove(player.getUniqueId());
            player.closeInventory();
            updateIcon();
            for (UUID uuid : members) {
                Player receiver = Bukkit.getPlayer(uuid);
                if (receiver != null) {
                    receiver.sendMessage("§b[§c-§b] §7" + player.getName());
                }
            }
        }
    }

    public boolean ban(Player claimer, Player target) {
        if (!isHost(claimer)) {
            claimer.sendMessage("§f이 §a채팅방§f의 §c호스트§f가 아닙니다!");
            return false;
        }
        if (target.getName().equals(claimer.getName())) {
            claimer.sendMessage("§f자기 자신은 §c벤§f할 수 없습니다!");
            return false;
        }

        if (!banList.contains(target.getUniqueId())) {
            banList.add(target.getUniqueId());
            members.remove(target.getUniqueId());

            claimer.sendMessage("§6" + target.getName() + "§f님을 채팅방에서 벤 하였습니다!");
            target.sendMessage("§6" + roomName + "§f채팅방에서 벤 되었습니다!");

            for(UUID uuid : members){
                if(Bukkit.getPlayer(uuid) != null){
                    Bukkit.getPlayer(uuid).sendMessage("§6" + target.getName() + "§f님이 채팅방에서 벤 되었습니다!");
                }
            }
            return true;
        }
        return false;
    }

    public void unBan(Player claimer, UUID target) {
        if (!isHost(claimer)) {
            claimer.sendMessage("§f이 §a채팅방§f의 §c호스트§f가 아닙니다!");
            return;
        }
        if (!isBanned(target)) {
            claimer.sendMessage("§f해당 유저는 §c벤 상태§f가 아닙니다");
            return;
        }

        banList.remove(target);
        claimer.closeInventory();
        claimer.sendMessage("§f해당 유저를 §6언벤 §f하였습니다!");
        if (Bukkit.getPlayer(target) != null) {
            Bukkit.getPlayer(target).sendMessage("§6" + roomName + "§f채팅방에서 언벤 되었습니다!");
        }
    }

    public void kick(Player claimer, Player target) {
        if (!isHost(claimer)) {
            claimer.sendMessage("§f이 §a채팅방§f의 §c호스트§f가 아닙니다!");
            return;
        }
        if (target.getName().equals(claimer.getName())) {
            claimer.sendMessage("§f자기 자신은 §c킥§f할 수 없습니다!");
            return;
        }
        if (!isPlayerinChatRoom(target)) {
            claimer.sendMessage("§f해당 유저는 §a채팅방§f에 접속중이지 않습니다!");
            return;
        }

        members.remove(target.getUniqueId());
        claimer.sendMessage("§6" + target.getName() + "§f님을 채팅방에서 킥하였습니다!");
        target.sendMessage("§6" + roomName + "§f채팅방에서 강제퇴장 되었습니다!");

        for(UUID uuid : members){
            if(Bukkit.getPlayer(uuid) != null){
                Bukkit.getPlayer(uuid).sendMessage("§6" + target.getName() + "§f님이 채팅방에서 강제퇴장 되었습니다!");
            }
        }
    }


    private void updateIcon() {
        ItemMeta itemMeta = roomIcon.getItemMeta();
        if (itemMeta.getLore() != null) {
            List<String> lore = itemMeta.getLore();
            int index = -1;

            for (String e : lore) {
                if (e.contains("현재 인원")) {
                    index = lore.indexOf(e);
                    lore.remove(e);
                    lore.add(index, "현재 인원: " + getCurrentMembersize() + " / " + getMaxmembersize());
                }
            }
            if (index == -1) {
                lore.add("현재 인원: " + getCurrentMembersize() + " / " + getMaxmembersize());
            }
        }
    }

    public void sendMessage(Player player, String message) {

        if (!behavior.isEmpty() && isHost(player)) {
            return;
        }

        if (!userListGUI.isBehaviorEmpty() && isHost(player)) {
            return;
        }

        if (freeze) {
            if (!isHost(player)) {
                player.sendMessage("§f현재 §a채팅방§f에서 §c채팅§f을 칠 수 없습니다.");
                return;
            }
        }
        for (UUID uuid : members) {
            if (Bukkit.getPlayer(uuid) != null) {
                Player p = Bukkit.getPlayer(uuid);

                p.sendMessage(message);
            }
        }
    }

    public void process(Player player, String identifier) {
        if (!behavior.isEmpty() || !userListGUI.isBehaviorEmpty()) {
            player.sendMessage("§f이미 진행중인 §c작업§f이 있습니다!");
            return;
        }

        player.closeInventory();

        if (identifier.equalsIgnoreCase("킥") || identifier.equalsIgnoreCase("밴")) {
            userListGUI.initializeBehavior(identifier);
            userListGUI.openInventory(player);
        } else if (identifier.equalsIgnoreCase("얼림")) {
            if (freeze) {
                player.sendMessage("§a채팅창§f을 녹였습니다!");
            } else {
                player.sendMessage("§a채팅창§f을 얼렸습니다!");
            }
            freeze = !freeze;
        } else if (identifier.equalsIgnoreCase("삭제")) {
            behavior.put("삭제", roomName);
            player.sendMessage("§f채팅방을 삭제하려면 §a'확인'");
            player.sendMessage("§f취소하려면 §c'취소' §f를 입력 해주세요.");
        } else if(identifier.equalsIgnoreCase("밴리스트")){
            openBanListInventory(player);
        }
    }

    public void openBanListInventory(Player player) {
        banListGUI.openInventory(player);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        if (joinWaitList.contains(player.getName()) || confirmedList.contains(player.getName())) {

            e.setCancelled(true);

            try {
                String assumedPassword = e.getMessage();

                if (assumedPassword.equals(password)) {
                    joinWaitList.remove(player.getName());
                    confirmedList.add(player.getName());
                    join(player);
                    confirmedList.remove(player.getName());
                } else {
                    player.sendMessage("§c패스워드가 불일치합니다. §6채팅방 접속을 다시 시도해주세요!");
                    joinWaitList.remove(player.getName());

                    Bukkit.getScheduler().scheduleSyncDelayedTask(instance, new Runnable() {
                        @Override
                        public void run() {
                            instance.getMainGUI().openMainMenu(player);
                        }
                    });
                }
            } catch (NumberFormatException exception) {
                player.sendMessage("§c패스워드는 0 이상의 정수입니다!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeleteChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        if (!behavior.isEmpty()) {
            if (host.equalsIgnoreCase(player.getName())) {
                e.setCancelled(true);
                if (e.getMessage().equalsIgnoreCase("취소")) {
                    instance.getManagementGUI().openInventory(player);
                    behavior.clear();
                    return;
                }
                if (e.getMessage().equalsIgnoreCase("확인")) {
                    if (behavior.containsKey("삭제")) {
                        roomManager.deleteRoom(roomName);
                        player.sendMessage("§c채팅방을 삭제하였습니다!");
                    }
                    behavior.clear();
                } else {
                    player.sendMessage("§a확인 §7또는 §c취소§7를 입력해주세요!");
                }
            }
        }

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (isHost(e.getPlayer())) {
            behavior.clear();
        }

        if(joinWaitList.contains(e.getPlayer().getName())){
            joinWaitList.remove(e.getPlayer().getName());
        }

        if(confirmedList.contains(e.getPlayer().getName())){
            confirmedList.remove(e.getPlayer().getName());
        }

        if (isPlayerinChatRoom(e.getPlayer())) {
            leave(e.getPlayer());
        }
    }

    public boolean changePassword(String newPassword){
        password = newPassword;
        return true;
    }
}
