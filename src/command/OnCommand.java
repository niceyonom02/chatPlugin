package command;

import GUI.MainGUI;
import main.ChatRoom;
import main.Main;
import main.RoomManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.UUID;

public class OnCommand implements CommandExecutor {
    private Main instance;
    private RoomManager roomManager;

    public OnCommand(Main instance, RoomManager roomManager){
        this.instance = instance;
        this.roomManager = roomManager;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] arg){
        if(!(sender instanceof Player)){
            return false;
        }

        Player player = (Player) sender;

        if(label.equalsIgnoreCase("채팅방")){
            if(arg.length < 1){
                MainGUI mainGUI = instance.getMainGUI();
                mainGUI.openMainMenu(player);
                return true;
            }

            if(arg[0].equalsIgnoreCase("도움말") || arg[0].equalsIgnoreCase("명령어")){
                player.sendMessage("§f- §e/채팅방 §f: 채팅방의 목록을 확인합니다\n" +
                        "§f- §e/채팅방 채팅전환 §f: 명령어 입력 시 채팅방에 입장한 상태로 일반채팅을 사용합니다. (재입력 시 현재 속한 채팅방의 채팅으로 전환합니다.)\n" +
                        "§f- §e/채팅방 비밀번호변경 §6<비밀번호> §f: 현재 관리하고있는 채팅방의 비밀번호를 변경합니다.");
            }

            if(arg[0].equalsIgnoreCase("채팅전환")){
                if(roomManager.getPlayerInChatRoom(player) == null){
                    player.sendMessage("§c채팅방에 접속하여 있지 않습니다!");
                    return false;
                }
                if(roomManager.isOutChat(player)){
                    roomManager.removeOutChat(player);
                    player.sendMessage("§f전체채팅 모드가 §c비활성화 §f되었습니다!");
                } else{
                    roomManager.addOutChat(player);
                    player.sendMessage("§f전체채팅 모드가 §a활성화 §f되었습니다!");
                }
            }

            if(arg[0].equalsIgnoreCase("비밀번호")){
                if(roomManager.getPlayerHostingRoom(player) == null){
                    player.sendMessage("§c운영 중인 채팅방이 없습니다!");
                    return false;
                }

                ChatRoom chatRoom = roomManager.getPlayerHostingRoom(player);
                player.sendMessage("§e호스팅§f중인 채팅방의 §c비밀번호§f는 §a" + chatRoom.getPassword() + "§f입니다!");
            }

            if(arg[0].equalsIgnoreCase("비밀번호변경")){
                if(roomManager.getPlayerHostingRoom(player) == null){
                    player.sendMessage("§c운영 중인 채팅방이 없습니다!");
                    return false;
                }

                if(arg.length < 2){
                    player.sendMessage("§c/채팅방 비밀번호변경 새비밀번호");
                    return false;
                }

                ChatRoom chatRoom = roomManager.getPlayerHostingRoom(player);

                if(chatRoom.changePassword(arg[1])){
                    player.sendMessage("§e호스팅§f중인 채팅방의 변경 후 §c비밀번호§f는 §a" + chatRoom.getPassword() + "§f입니다!");
                }
            }

            if(arg[0].equalsIgnoreCase("퇴장")){
                if(roomManager.getPlayerInChatRoom(player) == null){
                    player.sendMessage("§c채팅방에 접속하여 있지 않습니다!");
                    return false;
                }

                roomManager.leaveRoom(player);
            }

            if(arg[0].equalsIgnoreCase("스파이")){
                if(player.isOp()){
                    roomManager.toggleSpying(player);
                    return true;
                } else{
                    player.sendMessage("§fUnknown command. Type \"/help\" for help.");
                    return false;
                }
            }

            if(arg[0].equalsIgnoreCase("삭제")){
                if(player.isOp()){
                    if(arg.length < 2){
                        player.sendMessage("/채팅방 삭제 [방이름]");
                        return false;
                    }

                    StringBuilder name = new StringBuilder();

                    for(int i = 1; i < arg.length; i++){
                        name.append(arg[i] + " ");
                    }

                    if(roomManager.deleteRoom(name.toString())){
                        player.sendMessage("성공적으로 채팅방을 삭제하였습니다!");
                    } else{
                        player.sendMessage("해당 이름에 해당하는 채팅방이 없습니다!");
                    }
                    return true;
                } else{
                    player.sendMessage("§fUnknown command. Type \"/help\" for help.");
                    return false;
                }
            }

            if(arg[0].equals("정보")){
                if(player.isOp()){
                    if(arg.length < 2){
                        player.sendMessage("/채팅방 정보 [방이름]");
                        return false;
                    }

                    StringBuilder name = new StringBuilder();

                    for(int i = 1; i < arg.length; i++){
                        name.append(arg[i] + " ");
                    }

                    ChatRoom chatRoom = roomManager.searchRoomByName(name.toString());

                    if(chatRoom != null){
                        player.sendMessage("채팅방 이름: " + chatRoom.getRoomName());
                        player.sendMessage("채팅방 호스트: " + chatRoom.getHost());
                        player.sendMessage("채팅방 비밀번호: " + chatRoom.getPassword());
                        player.sendMessage("밴 리스트:");

                        for(UUID uuid : chatRoom.getBanList()){
                            player.sendMessage("- " + uuid.toString());
                        }
                    } else{
                        player.sendMessage("해당 이름에 해당하는 채팅방이 없습니다!");
                    }

                } else{
                    player.sendMessage("§fUnknown command. Type \"/help\" for help.");
                    return false;
                }
            }
        }
        return false;
    }
}
