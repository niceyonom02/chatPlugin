package main;

import GUI.MainGUI;
import GUI.ManagementGUI;
import command.OnCommand;
import listener.OnPlayerChat;
import listener.OnQuit;
import logger.ChatLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Main extends JavaPlugin {
    private Main instance;
    private RoomManager roomManager;
    private MainGUI mainGUI;
    private OnCommand onCommand;
    private OnQuit onQuit;
    private OnPlayerChat onPlayerChat;
    private ManagementGUI managementGUI;
    private ChatLogger chatLogger;

    @Override
    public void onEnable(){
        if(!this.getDataFolder().mkdir()) {
            this.getDataFolder().mkdir();
        }

        File file = new File(getDataFolder() + File.separator + "config.yml");

        if (!file.exists()){
            getConfig().options().copyDefaults(true);
            saveConfig();
        }

        Bukkit.getLogger().info("채팅방 플러그인을 시작하는 중입니다!");

        instance = this;
        roomManager = new RoomManager(instance);
        mainGUI = new MainGUI(instance, roomManager);
        managementGUI = new ManagementGUI(roomManager);
        onCommand = new OnCommand(instance, roomManager);
        onQuit = new OnQuit(roomManager);
        chatLogger = new ChatLogger(instance);
        onPlayerChat = new OnPlayerChat(instance, roomManager);


        Bukkit.getPluginManager().registerEvents(mainGUI, instance);
        Bukkit.getPluginManager().registerEvents(onQuit, instance);
        Bukkit.getPluginManager().registerEvents(onPlayerChat, instance);
        Bukkit.getPluginManager().registerEvents(managementGUI, instance);

        getCommand("채팅방").setExecutor(onCommand);
    }

    @Override
    public void onDisable(){
        Bukkit.getLogger().info("채팅방 플러그인을 종료하는 중입니다!");
        roomManager.saveRooms();
    }

    public MainGUI getMainGUI(){
        return mainGUI;
    }

    public ManagementGUI getManagementGUI(){
        return managementGUI;
    }

    public ChatLogger getChatLogger(){
        return chatLogger;
    }
}
