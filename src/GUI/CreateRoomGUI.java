package GUI;

import com.mysql.fabric.xmlrpc.base.Array;
import main.Main;
import main.RoomManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CreateRoomGUI implements Listener {
    private Main instance;
    private RoomManager roomManager;
    private Inventory createInventory;
    private HashMap<String, String> createRoomElement = new HashMap<>();

    public CreateRoomGUI(Main instance, RoomManager roomManager) {
        this.instance = instance;
        this.roomManager = roomManager;

        createInventory = Bukkit.createInventory(null, InventoryType.HOPPER, "§0채팅방 생성");
        createRoomElement.put("공개여부", "true");
        createRoomElement.put("초대여부", "true");
    }

    public void retrieveIcon(Material material) {
        createRoomElement.put("아이콘", material.name());
    }

    public void openInventory(Player player) {
        setItem();
        if(roomManager.isMaking(player)){
            roomManager.deleteMaking(player);
        }
        Bukkit.getPluginManager().registerEvents(this, instance);
        player.openInventory(createInventory);
        AsyncPlayerChatEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void invClose(InventoryCloseEvent e) {
        if(!(e.getPlayer() instanceof Player)){
            return;
        }
        Player player = (Player)e.getPlayer();
        if (!(e.getPlayer() instanceof Player)) {
            return;
        }

        if (e.getInventory().equals(createInventory)) {
            Bukkit.getPluginManager().registerEvents(this, instance);
            InventoryCloseEvent.getHandlerList().unregister(this);
            InventoryClickEvent.getHandlerList().unregister(this);

            if(createRoomElement.containsKey(e.getPlayer().getName())){
                roomManager.addMaking(player);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        if(!createRoomElement.isEmpty()){
            if(createRoomElement.containsKey(e.getPlayer().getName())){
                createRoomElement.clear();
            }
        }
    }


    public void setItem() {
        ItemStack name = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = name.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        lore.add("");
        meta.setDisplayName("§6채팅방 이름 §f설정");

        if (createRoomElement.containsKey("이름")) {
            lore.add("§7채팅방 이름 : §a" + createRoomElement.get("이름"));
        } else {
            lore.add("§7채팅방의 §6이름§7을 지정해주세요!");
        }
        meta.setLore(lore);
        name.setItemMeta(meta);
        createInventory.setItem(0, name);

        ItemStack isVisible = new ItemStack(Material.WOOD_DOOR);
        meta = isVisible.getItemMeta();
        meta.setDisplayName("§a채팅방 공개 여부 §f설정");
        lore.clear();
        lore.add("");
        if (createRoomElement.get("공개여부").equalsIgnoreCase("true")) {
            lore.add("§a공개§7로 설정합니다.");
        } else {
            lore.add("§c비공개§7로 설정합니다.");
        }
        meta.setLore(lore);
        isVisible.setItemMeta(meta);
        createInventory.setItem(3, isVisible);

        ItemStack maxMember = new ItemStack(Material.SKULL_ITEM);
        meta = maxMember.getItemMeta();
        meta.setDisplayName("§c채팅방 최대 인원 §f설정");
        lore.clear();
        lore.add("");
        if (createRoomElement.containsKey("최대인원")) {
            lore.add("§7채팅방 인원 : §c" + createRoomElement.get("최대인원") + "§f명");
        } else {
            lore.add("§7채팅방의 §6최대인원§7을 지정해주세요!");
        }
        meta.setLore(lore);
        maxMember.setItemMeta(meta);
        createInventory.setItem(2, maxMember);

        ItemStack icon = new ItemStack(Material.LEASH);
        lore.clear();
        lore.add("");
        if (createRoomElement.containsKey("설명")) {
            lore.add("§7채팅방 설명 : §e" + createRoomElement.get("설명"));
        } else {
            lore.add("§7채팅방의 §e설명§7을 지정해주세요!");
        }
        meta = icon.getItemMeta();
        meta.setDisplayName("§e채팅방 설명 §f설정");
        meta.setLore(lore);
        icon.setItemMeta(meta);
        createInventory.setItem(1, icon);

        ItemStack confirm = new ItemStack(Material.STONE_BUTTON);
        meta = confirm.getItemMeta();
        meta.setDisplayName("§f채팅방 생성");
        lore.clear();
        lore.add("");
        lore.add("§a채팅방§7을 생성합니다.");
        meta.setLore(lore);
        confirm.setItemMeta(meta);
        createInventory.setItem(4, confirm);
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

        if (e.getInventory().equals(createInventory)) {
            e.setCancelled(true);

            if (e.getClick().equals(ClickType.NUMBER_KEY)) {
                e.setCancelled(true);
            }

            if (e.getSlot() == 0) {
                createRoomElement.put(player.getName(), "이름");
                player.closeInventory();
                player.sendMessage("§e채팅방의 이름을 입력해주세요!");
            } else if (e.getSlot() == 3) {
                switchVisible();
            } else if (e.getSlot() == 1) {
                createRoomElement.put(player.getName(), "설명");
                player.closeInventory();
                player.sendMessage("§e채팅방의 설명을 입력해주세요!");
            } else if (e.getSlot() == 2) {
                createRoomElement.put(player.getName(), "최대인원");
                player.closeInventory();
                player.sendMessage("§e채팅방의 최대인원을 입력해주세요!(2 ~ 50)");
            } else if (e.getSlot() == 4) {
                if (!createRoomElement.containsKey("이름")) {
                    player.sendMessage("§c채팅방의 이름을 아직 등록하지 않았습니다!");
                    return;
                }

                if (!createRoomElement.containsKey("최대인원")) {
                    player.sendMessage("§c채팅방의 최대인원을 아직 등록하지 않았습니다!");
                    return;
                }

                createRoomElement.put("호스트", player.getName());
                if(createRoomElement.get("설명") == null){
                    createRoomElement.put("설명", "");
                }

                if (roomManager.createNewRoom(createRoomElement.get("이름"), Integer.valueOf(createRoomElement.get("최대인원")),
                        createRoomElement.get("호스트"), instance, makePlayerSkull(player), createRoomElement.get("설명"), Boolean.valueOf(createRoomElement.get("공개여부")))) {
                    player.closeInventory();
                    player.sendMessage("§a정상적으로 채팅방을 생성하였습니다!");
                    createRoomElement.clear();
                } else {
                    player.sendMessage("§c이미 동일한 이름의 채팅방이 있습니다!");
                }
            }
        }
    }

    public ItemStack makePlayerSkull(Player player) {
        ItemStack playerSkull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta meta = (SkullMeta) playerSkull.getItemMeta();

        meta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
        meta.setDisplayName(createRoomElement.get("이름"));

        List<String> lore = new ArrayList<>();

        if (createRoomElement.containsKey("설명")) {
            lore.add("§7" + createRoomElement.get("설명"));
            lore.add("");
        }

        lore.add("§f호스트: §7" + player.getName());
        lore.add("§f인원: §a" + 0 + " §7/ §c" + createRoomElement.get("최대인원"));
        lore.add("");

        if (createRoomElement.get("공개여부").equalsIgnoreCase("true")) {
            lore.add("§f공개여부: §a공개");
        } else {
            lore.add("§f공개여부: §c비공개");
        }

        playerSkull.setItemMeta(meta);
        return playerSkull;
    }

    public void switchVisible() {
        String isVisible;

        if (createRoomElement.get("공개여부").equalsIgnoreCase("true")) {
            isVisible = "false";
        } else {
            isVisible = "true";
        }

        createRoomElement.put("공개여부", isVisible);

        applyVisible();
    }

    public void applyVisible() {
        ItemStack item = createInventory.getItem(1);
        ItemMeta meta = item.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();

        lore.add("");
        if (createRoomElement.get("공개여부").equalsIgnoreCase("true")) {
            lore.add("§a공개§7로 설정합니다.");
        } else {
            lore.add("§c비공개§7로 설정합니다.");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        createInventory.setItem(3, item);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (createRoomElement.containsKey(e.getPlayer().getName())) {
            e.setCancelled(true);
            switch (createRoomElement.get(e.getPlayer().getName())) {
                case "이름":
                    if (e.getMessage().length() > 15) {
                        e.getPlayer().sendMessage("§c채팅방 이름은 최대 15글자입니다!");
                        e.getPlayer().sendMessage("§f채팅방의 이름을 다시 입력해주세요!");
                        return;
                    }
                    createRoomElement.put("이름", e.getMessage());
                    openInventory(e.getPlayer());
                    createRoomElement.remove(e.getPlayer().getName());
                    break;
                case "최대인원":
                    try {
                        int maxMember = Integer.parseInt(e.getMessage());

                        if (maxMember < 2) {
                            e.getPlayer().sendMessage("§c채팅방 최소인원은 2명입니다!");
                            e.getPlayer().sendMessage("§f채팅방의 최대인원 수를 다시 입력해주세요!");
                            return;
                        }

                        if (maxMember > 50) {
                            e.getPlayer().sendMessage("§c채팅방 최대인원은 50명입니다!");
                            e.getPlayer().sendMessage("§f채팅방의 최대인원 수를 다시 입력해주세요!");
                            return;
                        }

                        createRoomElement.put("최대인원", e.getMessage());
                        createRoomElement.remove(e.getPlayer().getName());
                        openInventory(e.getPlayer());
                        break;
                    } catch (NumberFormatException ex) {
                        e.getPlayer().sendMessage("§e정수를 입력해주세요!");
                        break;
                    }
                case "설명":
                    if (e.getMessage().length() > 20) {
                        e.getPlayer().sendMessage("§c채팅방 설명은 최대 20글자입니다!");
                        e.getPlayer().sendMessage("§f채팅방 설명을 다시 엽력해주세요!");
                        return;
                    }
                    createRoomElement.put("설명", e.getMessage());
                    openInventory(e.getPlayer());
                    createRoomElement.remove(e.getPlayer().getName());
                    break;
            }
        }
    }
}
