package me.perotin.playerchannels.events.chat_events;

import me.perotin.playerchannels.PlayerChannels;
import me.perotin.playerchannels.objects.InventoryHelper;
import me.perotin.playerchannels.objects.PreChatroom;
import me.perotin.playerchannels.storage.files.ChannelFile;
import me.perotin.playerchannels.storage.files.FileType;
import me.perotin.playerchannels.utils.ChannelUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

/* Created by Perotin on 9/19/19 */
public class CreateChatroomInputEvent implements Listener {

    private static CreateChatroomInputEvent instance;
    private HashSet<UUID> setName = new HashSet<>();
    private HashSet<UUID> setDescription = new HashSet<>();
    private Map<UUID, PreChatroom> inCreation;
    private PlayerChannels plugin;


    public CreateChatroomInputEvent(PlayerChannels plugin) {
        this.inCreation = new HashMap<>();
        instance = this;
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player chatter = event.getPlayer();
        ChannelFile messages = new ChannelFile(FileType.MESSAGES);

        if (inCreation.containsKey(chatter.getUniqueId())) {
            PreChatroom preChatroom = inCreation.get(chatter.getUniqueId());
            event.setCancelled(true);
            if (setName.contains(chatter.getUniqueId())) {
                String name = event.getMessage();
                // check if name is more than 1 word
                if (name.split(" ").length > 1) {
                    //too many words TODO come up with way to show messages, maybe big text on screen y'know what I mean
                    chatter.sendTitle(messages.getString("only-one-word"), "", 0, 20 * 3, 20);
                    return;
                }
                if (isNameTaken(name)) {
                    // send message saying name is taken, tell them to say it again
                    chatter.sendTitle(messages.getString("taken-name"), "", 0, 20 * 3, 20);
                    return;
                }
                // success condition, set name in PreChatroom
                name = ChannelUtils.addColor(name);
                preChatroom.setName(name);
                showUpdatedMenu(chatter, preChatroom);
                setName.remove(chatter.getUniqueId());


            } else if (setDescription.contains(chatter.getUniqueId())) {
                String description = ChannelUtils.addColor(event.getMessage());
                // no error conditions so continue

                preChatroom.setDescription(description);
                showUpdatedMenu(chatter, preChatroom);
                setDescription.remove(chatter.getUniqueId());
            }
        }
    }

    public static CreateChatroomInputEvent getInstance() {
        return instance;
    }

    public HashSet<UUID> getSetName() {
        return setName;
    }

    public HashSet<UUID> getSetDescription() {
        return setDescription;
    }

    public Map<UUID, PreChatroom> getInCreation() {
        return inCreation;
    }


    /**
     * Shows most updated menu of a chatroom in progress of being created
     */
    public void showUpdatedMenu(Player toShow, PreChatroom view) {
        new BukkitRunnable() {
            @Override
            public void run() {
                InventoryHelper helper = plugin.getHelper();
                helper.setNavigationBar(helper.getCreationMenu(view), toShow).getFirst().show(toShow);
            }
        }.runTask(plugin);
    }


    /**
     * @param name
     * @return true if name is used by another chatroom, false if not
     */
    public static boolean isNameTaken(String name) {
        return ChannelUtils.getChatroomWith(name) != null;
    }
}
