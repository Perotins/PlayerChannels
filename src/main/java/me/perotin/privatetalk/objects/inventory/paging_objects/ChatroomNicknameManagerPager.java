package me.perotin.privatetalk.objects.inventory.paging_objects;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.events.chat_events.ChatroomSetNicknameEvent;
import me.perotin.privatetalk.objects.Chatroom;
import me.perotin.privatetalk.objects.PrivatePlayer;
import me.perotin.privatetalk.storage.Pair;
import me.perotin.privatetalk.storage.files.FileType;
import me.perotin.privatetalk.storage.files.PrivateFile;
import me.perotin.privatetalk.utils.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatroomNicknameManagerPager extends PagingMenu {

    private Chatroom chatroom;

    private PrivateFile messages;
    public ChatroomNicknameManagerPager(Chatroom chatroom, Player viewer){
        super(chatroom.getName()+"-nickname-manager", 6, viewer, new MainMenuPaging(viewer, PrivateTalk.getInstance()).getMenu());
        this.chatroom = chatroom;
        this.messages = new PrivateFile(FileType.MESSAGES);
        PrivateTalk.getInstance().getHelper().setSideDecorationSlots(getMenu());
        PrivateTalk.getInstance().getHelper().setSideDecorationSlots(getMenu());
        // potentially change this to be all black deco items except for the middle which will be the chatroom itemstack
        // with just the name
        PrivateTalk.getInstance().getHelper().setNavigationBar(getMenu(), viewer);


        getPaginatedPane().populateWithGuiItems(generatePages());
    }

    /**
     *
     * @return list of guis with heads that have an inventory event attached to them that
     *  lets the player set a nickname depending on the role of the person clicking
     */
    @Override
    protected List<GuiItem> generatePages() {
        List<GuiItem> items = new ArrayList<>();
        for(UUID uuid: chatroom.getMembers()){
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            PrivatePlayer privatePlayer = PrivatePlayer.getPlayer(uuid);

            String nickname = "";
            if (chatroom.getNickNames().containsKey(player.getUniqueId())){
                nickname = chatroom.getNickNames().get(player.getUniqueId());
            }
            List<String> lores = new ArrayList<>();
            ItemStackUtils playerHeadUtils = new ItemStackUtils(Material.PLAYER_HEAD)
                    .setOwner(player)
                    .setName(ChatColor.YELLOW + player.getName());
                   lores.add(ChatColor.GRAY + "Nickname: " + nickname);
            if (chatroom.hasModeratorPermissions(getViewer().getUniqueId())
            || ChatColor.stripColor(playerHeadUtils.getDisplayName()).equals(getViewer().getName())) {
                lores.add(ChatColor.GRAY + "Click to change");
            }
            playerHeadUtils.setLore(lores);
            ItemStack playerHead = playerHeadUtils.build();


            GuiItem guiItem = new GuiItem(playerHead, headsInNicknameMenu(player, uuid, playerHead));

            items.add(guiItem);
        }
        return items;
    }


    private Consumer<InventoryClickEvent> headsInNicknameMenu(OfflinePlayer player, UUID uuid, ItemStack playerHead){

        return event -> {
            event.setCancelled(true);
                /*
                Criteria:
                Nicknames menu will only appear if nicknames are enabled so it is a redundant check here actually
                Owners and moderaators can set a nickname for any given player
                Member can only set a nickname for themselves
                 */
            // if they are, check who is setting a nickname
            String setNicknameMsg = messages.getString("set-nickname")
                    .replace("$name$", player.getName())
                    .replace("$cancel$", messages.getString("cancel"));
            if (chatroom.hasModeratorPermissions(getViewer().getUniqueId())) {
                getViewer().closeInventory();
                getViewer().sendMessage(setNicknameMsg);
                ChatroomSetNicknameEvent.setNickname.put(getViewer(), new Pair<>(chatroom, uuid));

            } else {
                // member setting so only allow for them to click their own
                if (ChatColor.stripColor(playerHead.getItemMeta().getDisplayName()).equals(getViewer().getName())){
                    // allow to set nickname
                    getViewer().closeInventory();
                    getViewer().sendMessage(setNicknameMsg);
                    ChatroomSetNicknameEvent.setNickname.put(getViewer(), new Pair<>(chatroom, uuid));
                }
            }



        };
    }



    public Chatroom getChatroom() {
        return chatroom;
    }
}