package listener;

import main.RoomManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class OnQuit implements Listener {
    private RoomManager roomManager;

    public OnQuit(RoomManager roomManager){
        this.roomManager = roomManager;
    }

    @EventHandler
    public void onQuitServer(PlayerQuitEvent e){
        Player player = e.getPlayer();
        roomManager.leaveRoom(player);
        roomManager.deleteMaking(player);
    }
}
