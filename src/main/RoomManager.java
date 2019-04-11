package main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class RoomManager {
    private Main instance;
    private List<ChatRoom> roomList;
    private ArrayList<UUID> spyList = new ArrayList<>();
    private ArrayList<UUID> makingList = new ArrayList<>();
    private ArrayList<UUID> outChat = new ArrayList<>();

    public RoomManager(Main instance){
        this.instance = instance;

        roomList = loadRooms();
    }

    public boolean isOutChat(Player player){
        if(outChat.contains(player.getUniqueId())){
            return true;
        } else{
            return false;
        }
    }

    public void addOutChat(Player player){
        outChat.add(player.getUniqueId());
    }

    public void removeOutChat(Player player){
        outChat.remove(player.getUniqueId());
    }

    public boolean isMaking(Player player){
        return makingList.contains(player.getUniqueId());
    }

    public void addMaking(Player player){
        makingList.add(player.getUniqueId());
    }

    public void deleteMaking(Player player){
        makingList.remove(player.getUniqueId());
    }

    public void toggleSpying(Player player){
        if(spyList.contains(player.getUniqueId())){
            spyList.remove(player.getUniqueId());
            player.sendMessage("§b채팅방 스파이 기능을 비활성화 하였습니다!");
        } else{
            spyList.add(player.getUniqueId());
            player.sendMessage("§b채팅방 스파이 기능을 활성화 하였습니다!");
        }
    }

    public void sendToSpy(String message){
        for(UUID uuid : spyList){
            if(Bukkit.getPlayer(uuid) != null){
                Player player = Bukkit.getPlayer(uuid);
                player.sendMessage(message);
            }
        }
    }

    public void saveRooms(){
        instance.getConfig().set("chatRoom", null);

        for(ChatRoom chat : roomList){
            String name = chat.getRoomName();
            int maxMember = chat.getMaxmembersize();
            String host = chat.getHost();
            String password = chat.getPassword();
            ItemStack icon = chat.getRoomIcon();
            String lore = chat.getLore();
            boolean isPublic = chat.getIsPublic();
            ArrayList<UUID> banList = chat.getBanList();
            ArrayList<String> converted = new ArrayList<>();


            instance.getConfig().set("chatRoom." + name + ".maxMember", maxMember);
            instance.getConfig().set("chatRoom." + name + ".host", host);
            instance.getConfig().set("chatRoom." + name + ".icon", icon);
            instance.getConfig().set("chatRoom." + name + ".lore", lore);
            instance.getConfig().set("chatRoom." + name + ".isPublic", isPublic);
            instance.getConfig().set("chatRoom." + name + ".password", password);

            for(UUID uuid : banList){
                converted.add(uuid.toString());
            }
            instance.getConfig().set("chatRoom." + name + ".banList", converted);
        }
        instance.saveConfig();
    }

    public void cleanUp(){
        roomList.clear();
    }

    public boolean createNewRoom(String name, int maxMembers, String host, Main instance, ItemStack icon, String lore, boolean isPublic){


        if(searchRoomByName(name) == null){
            ChatRoom chat;

            ArrayList<UUID> banList = new ArrayList<>();

            if(host == null){
                chat = new ChatRoom(name, maxMembers, "server", instance, icon, lore, isPublic, banList, this, null);
            } else{
                chat = new ChatRoom(name, maxMembers, host, instance, icon, lore, isPublic, banList, this, null);
            }

            roomList.add(chat);
            return true;
        } else{
            return false;
        }
    }

    public boolean deleteRoom(String name){
        if(searchRoomByName(name) != null){
            roomList.remove(searchRoomByName(name));
            return true;
        } else{
            return false;
        }
    }

    public ChatRoom getPlayerInChatRoom(Player player){
        for(ChatRoom chatRoom : roomList){
            if(chatRoom.isPlayerinChatRoom(player)){
                return chatRoom;
            }
        }
        return null;
    }

    public ChatRoom getPlayerHostingRoom(Player player){
        for(ChatRoom chatRoom : roomList){
            if(chatRoom.getHost().equalsIgnoreCase(player.getName())){
                return chatRoom;
            }
        }
        return null;
    }

    public void joinRoom(Player player, String roomName){
        if(searchRoomByName(roomName) != null){
            ChatRoom chatRoom = searchRoomByName(roomName);

            if(isPlayerWaiting(player)){
                player.sendMessage("§c이미 접속대기 중인 채팅방이 있습니다!");
                return;
            }

            if(chatRoom.isRoomFull()){
                player.sendMessage("§c채팅방이 꽉찼습니다!");
                return;
            }

            if(chatRoom.isPlayerinChatRoom(player)){
                player.sendMessage("§c이미 해당 채팅방에 접속중입니다!");
                return;
            }

            if(getPlayerInChatRoom(player) != null){
                player.sendMessage("§c이미 다른 채팅방에 접속중입니다!");
                player.sendMessage("§a/채팅방 퇴장 §f을 통해 먼저 채팅방에서 나와주세요!");
                return;
            }

            chatRoom.join(player);
        }
    }

    public void leaveRoom(Player player){
        boolean left = false;

        for(ChatRoom chatRoom : roomList){
            if(chatRoom.isPlayerinChatRoom(player)){
                chatRoom.leave(player);
                left = true;

                if(player.isOnline()){
                    player.sendMessage("§a채팅방§f에서 §c퇴장§f하였습니다!");
                }
                break;
            }
        }

        if(!left){
            if(player.isOnline()){
                player.sendMessage("§c채팅방에 접속해있지 않습니다!");
            }
        }
    }

    public ChatRoom searchRoomByName(String name){
        for(ChatRoom chat : roomList){
            if(ChatColor.stripColor(chat.getRoomName()).equalsIgnoreCase(name)){
                return chat;
            }
        }
        return null;
    }

    public boolean isPlayerWaiting(Player player){
        for(ChatRoom chatRoom : roomList){
            if(chatRoom.isPlayerInWaitList(player) || chatRoom.isPlayerInConfirmedList(player)){
                return true;
            }
        }
        return false;
    }

    public List<ChatRoom> getRoomList(){
        return Collections.unmodifiableList(roomList);
    }

    public ArrayList<ChatRoom> loadRooms(){
        ArrayList<ChatRoom> roomList = new ArrayList<>();

        if(instance.getConfig().getConfigurationSection("chatRoom") != null){
            for(String chatRoom : instance.getConfig().getConfigurationSection("chatRoom").getKeys(false)){
                String name;
                int maxMember;
                String host;
                ItemStack icon;
                String lore;
                String password;
                boolean isPublic;
                ArrayList<String> banList;
                ArrayList<UUID> converted = new ArrayList<>();

                name = chatRoom;
                maxMember = instance.getConfig().getInt("chatRoom." + chatRoom + ".maxMember");
                host = instance.getConfig().getString("chatRoom." + chatRoom + ".host");
                icon = (ItemStack) instance.getConfig().get("chatRoom." + chatRoom + ".icon");
                lore  = instance.getConfig().getString("chatRoom." + chatRoom + ".lore");
                isPublic = instance.getConfig().getBoolean("chatRoom." + chatRoom + ".isPublic");
                banList = (ArrayList<String>) instance.getConfig().getStringList("chatRoom." + chatRoom + ".banList");
                password = instance.getConfig().getString("chatRoom." + chatRoom + ".password");


                for(String e : banList){
                    converted.add(UUID.fromString(e));
                }

                if(lore == null){
                    lore = "";
                }

                ChatRoom chat = new ChatRoom(name, maxMember, host, instance, icon, lore, isPublic, converted, this, password);
                roomList.add(chat);
            }
        }
        return roomList;
    }
}
