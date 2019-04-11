package listener;

import logger.ChatLogger;
import main.ChatRoom;
import main.Main;
import main.RoomManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.UUID;

public class OnPlayerChat implements Listener {
    private RoomManager roomManager;
    private Main instance;
    private ChatLogger logger;

    public OnPlayerChat(Main instance, RoomManager roomManager){
        this.roomManager = roomManager;
        this.instance = instance;

        logger = instance.getChatLogger();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e){
        if(roomManager.getPlayerInChatRoom(e.getPlayer()) != null){
            if(roomManager.isOutChat(e.getPlayer())){
                return;
            }

            e.setCancelled(true);

            ChatRoom chatRoom = roomManager.getPlayerInChatRoom(e.getPlayer());

            try{
                String pass = e.getMessage();
                if(chatRoom.getPassword().equals(pass)){
                    return;
                } else{
                    String message = "§a■ §7[ §6"+ chatRoom.getRoomName() +"§7 ] " +e.getPlayer().getDisplayName() +" : " + e.getMessage();

                    chatRoom.sendMessage(e.getPlayer(), message);
                    logger.log(message);
                    if(!e.getPlayer().isOp()){
                        roomManager.sendToSpy(message);
                    }
                }
            }catch (NumberFormatException ex){
                String message = "§a■ §7[ §6"+ chatRoom.getRoomName() +"§7 ] " +e.getPlayer().getDisplayName() +" : " + e.getMessage();

                chatRoom.sendMessage(e.getPlayer(), message);
                logger.log(message);
                if(!e.getPlayer().isOp()){
                    roomManager.sendToSpy(message);
                }
            }
        }
    }

    /**@EventHandler(priority = EventPriority.LOWEST)
    public void onGeneralChat(AsyncPlayerChatEvent e){

        for(Player player : Bukkit.getOnlinePlayers()){
            if(roomManager.getPlayerInChatRoom(player) != null){

                if(roomManager.isOutChat(player)){
                    continue;
                }
                e.getRecipients().remove(player);
            }
        }
    } */
}
