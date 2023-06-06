package me.perotin.privatetalk.objects;

/* Created by Perotin on 8/27/19 */


import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.perotin.privatetalk.PrivateTalk;
import me.perotin.privatetalk.objects.inventory.actions.CreateChatroomAction;
import me.perotin.privatetalk.objects.inventory.paging_objects.ChatroomInvitationListPager;
import me.perotin.privatetalk.objects.inventory.paging_objects.MainMenuPaging;
import me.perotin.privatetalk.objects.inventory.static_inventories.PlayerProfileMenu;
import me.perotin.privatetalk.storage.Pair;
import me.perotin.privatetalk.storage.files.FileType;
import me.perotin.privatetalk.storage.files.PrivateFile;
import me.perotin.privatetalk.utils.ItemStackUtils;
import me.perotin.privatetalk.utils.PrivateUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Class for bringing up static parts of inventories like the nav-bar etc.
 */
public class InventoryHelper {

    private PrivateFile file;
    private StaticPane navBar;
    private final StaticPane pagingNavigationBar;
    private StaticPane creationMenu;
    private StaticPane rightSideDecoSlots;
    private StaticPane leftSideDecoSlots;
    /**
     * Consumer for making decoration items cancel the click event and nothing else
     **/
    private Consumer<InventoryClickEvent> doNothing;

    public InventoryHelper() {
        this.file = new PrivateFile(FileType.MENUS);
        this.navBar = new StaticPane(0, 0, 9, 1);
        this.pagingNavigationBar = new StaticPane(0, 5, 9, 1);
        this.doNothing = (event) -> event.setCancelled(true);
        setNavBar();
        setSideDecoSlots();
    }

    //----------------------- Navigation Bar Methods ----------------------------------------------------


    /**
     * @param inventory to set
     * @return sets the nav bar for any given inventory
     */
    public Pair<Gui, StaticPane> setNavigationBar(ChestGui inventory, OfflinePlayer owner) {
        Pair<ItemStack, Integer> playerHead = getItem("nav-bar.player-profile-head", owner);
        ItemStack playerHeadItem = playerHead.getFirst();
        PrivateUtils.replacePlaceHolderInDisplayName(playerHeadItem, "$name$", owner.getName());

        navBar.addItem(new GuiItem(playerHeadItem, clickOnOwnHead(owner.getUniqueId())), getItem( "nav-bar.player-profile-head", owner).getSecond(), 0);
        Pair<ItemStack, Integer> invites = getItem( "nav-bar.manage-invites", null);
        navBar.addItem(new GuiItem(invites.getFirst(), event -> {
            event.setCancelled(true);
            new ChatroomInvitationListPager(PrivatePlayer.getPlayer(owner.getUniqueId()), (Player) owner, new MainMenuPaging((Player) owner, PrivateTalk.getInstance()).getMenu()).show();
        }),  invites.getSecond(), 0);

        inventory.addPane(navBar);
        return new Pair<>(inventory, navBar);
    }




    /**
     * Sets the navigation bar that appears in most menus at the very top
     */
    private void setNavBar() {
        PrivateFile file = new PrivateFile(FileType.MENUS);
        Pair<ItemStack, Integer> head = getItemFrom(Material.PLAYER_HEAD, "nav-bar.player-profile-head", null);
        Pair<ItemStack, Integer> invites = getItemFrom(Material.WRITABLE_BOOK, "nav-bar.manage-invites", null);
        Pair<ItemStack, Integer> createChatroom = getItemFrom(Material.ANVIL, "nav-bar.create-chatroom", null);

        navBar.addItem(new GuiItem(head.getFirst()), head.getSecond(), 0);
        navBar.addItem(new GuiItem(createChatroom.getFirst(), CreateChatroomAction.createChatroomConsumer()), (int) createChatroom.getSecond(), 0);
        navBar.addItem(new GuiItem(invites.getFirst()),  invites.getSecond(), 0);
        GuiItem deco = DECO_ITEM();
        List<Integer> slots = getAsInts(file.getConfiguration().getStringList("nav-bar.deco-item.slots"));
        for (int x : slots) {
            navBar.addItem(deco, x, 0);
        }

    }


    //----------------------- Creation Menu Methods ----------------------------------------------------

    /**
     * Sets the creation menu on an inventory
     */
    private ChestGui setCreationMenu(ChestGui toSet, PreChatroom chatroom) {
        setCreationMenu(chatroom);
        toSet.addPane(creationMenu);
        StaticPane bottomRow =  new StaticPane(0, 5, 9, 1);
        bottomRow.addItem(BACK_ITEM(), 0, 0);
        bottomRow.fillWith(DECO_ITEM().getItem(), doNothing);
        toSet.addPane(bottomRow);
        return toSet;
    }


    /**
     * Sets the creation menu items
     */
    private void setCreationMenu(PreChatroom chatroom) {
        PrivateFile menus = new PrivateFile(FileType.MENUS);
        PrivateFile messages = new PrivateFile(FileType.MESSAGES);

        this.creationMenu = new StaticPane(1, 1, 7, 4);
        Pair<ItemStack, Integer> name = getItemFrom(Material.valueOf(menus.getString("creation-menu.name.material")), "creation-menu.name", null);
        Pair<ItemStack, Integer> description = getItemFrom(Material.valueOf(menus.getString("creation-menu.description.material")), "creation-menu.description", null);
        Pair<ItemStack, Integer> status = getItemFrom(Material.valueOf(menus.getString("creation-menu.status.material")), "creation-menu.status", null);
        Pair<ItemStack, Integer> saved = getItemFrom(Material.valueOf(menus.getString("creation-menu.saved.material")), "creation-menu.saved", null);
        Pair<ItemStack, Integer> createButton = getItemFrom(Material.valueOf(menus.getString("creation-menu.create-button.material")), "creation-menu.create-button", null);
        // setting all the pair values to use the values from the PreChatroom
        ItemStack nameItem = name.getFirst();
        if (chatroom.getName() != null) {
            ItemMeta meta = name.getFirst().getItemMeta();
            meta.setDisplayName(name.getFirst().getItemMeta().getDisplayName() + " " + chatroom.getName());
            nameItem.setItemMeta(meta);

        }
        name = new Pair<>(nameItem, name.getSecond());

        ItemStack descItem = description.getFirst();
        if (chatroom.getDescription() != null) {
            ItemMeta meta = description.getFirst().getItemMeta();
            meta.setDisplayName(description.getFirst().getItemMeta().getDisplayName() + " " + chatroom.getDescription());
            descItem.setItemMeta(meta);

        }
        description = new Pair<>(descItem, description.getSecond());

        ItemStack statusItem = status.getFirst();
        ItemMeta sMeta = statusItem.getItemMeta();
        if(chatroom.isPublic()) {
            sMeta.setDisplayName(sMeta.getDisplayName() + " " +messages.getString("public") );
        } else {
            sMeta.setDisplayName(sMeta.getDisplayName() + " " + messages.getString("private"));
        }
        statusItem.setItemMeta(sMeta);

        status = new Pair<>(statusItem, status.getSecond());

        ItemStack savedItem = saved.getFirst();
        ItemMeta savedMeta = savedItem.getItemMeta();
        if(chatroom.isSaved()) {
            savedMeta.setDisplayName(savedMeta.getDisplayName() + " " +messages.getString("true") );
        } else {
            savedMeta.setDisplayName(savedMeta.getDisplayName() + " " + messages.getString("false"));
        }
        savedItem.setItemMeta(savedMeta);

        status = new Pair<>(statusItem, status.getSecond());
        saved = new Pair<>(savedItem, saved.getSecond());


        creationMenu.addItem(new GuiItem(name.getFirst(), CreateChatroomAction.setNameConsumer()), name.getSecond(), 1);
        creationMenu.addItem(new GuiItem(description.getFirst(), CreateChatroomAction.setDescriptionConsumer()), description.getSecond(), 1);
        creationMenu.addItem(new GuiItem(status.getFirst(), CreateChatroomAction.toggleStatusConsumer()), status.getSecond(), 2);
        creationMenu.addItem(new GuiItem(saved.getFirst(), CreateChatroomAction.toggleSavedConsumer()), saved.getSecond(), 2);
        creationMenu.addItem(new GuiItem(createButton.getFirst(), CreateChatroomAction.clickCreateButtonConsumer()), createButton.getSecond(), 3);


    }

    /**
     * Gets the GUI for any designed PreChatroom object
     */
    public ChestGui getCreationMenu(PreChatroom chatroom) {
        PrivateFile file = new PrivateFile(FileType.MENUS);
        ChestGui gui = new ChestGui( 6, file.getString("creation-menu.display-name"));
        setSideDecorationSlots(gui);
        gui = setCreationMenu(gui, chatroom);

        return gui;
    }
    //----------------------- Decoration Methods ----------------------------------------------------

    /**
     * @param inv to set
     * @return inventory set with side decoration slotsss
     */
    public Gui setSideDecorationSlots(ChestGui inv) {
        inv.addPane(leftSideDecoSlots);
        inv.addPane(rightSideDecoSlots);
        return inv;

    }

    /**
     * @apiNote sets the side decoration slots
     */
    private void setSideDecoSlots() {
        this.rightSideDecoSlots = new StaticPane(8, 1, 1, 4);
        this.leftSideDecoSlots = new StaticPane(0, 1, 1, 4);
        rightSideDecoSlots.fillWith(DECO_ITEM().getItem(), doNothing);
        leftSideDecoSlots.fillWith(DECO_ITEM().getItem(), doNothing);

    }
    //----------------------- Paging Buttons Methods ----------------------------------------------------


    /**
     * @param inventory to set paging-nav-bar, next is whether a next button should appear
     *         and back is whether a back button should appear
     * @return inventory with paging-navar bar
     */
    public StaticPane setPagingNavBar(ChestGui inventory) {

        PrivateFile file = new PrivateFile(FileType.MENUS);
        List<Integer> decoSlots = getAsInts(file.getConfiguration().getStringList("paging-nav-bar.deco-item.slots"));
        int nextSlot = file.getConfiguration().getInt("paging-nav-bar.next-item.slot");
        int backSlot = file.getConfiguration().getInt("paging-nav-bar.back-item.slot");

        pagingNavigationBar.addItem(BACK_ITEM(), backSlot, 0);
        pagingNavigationBar.addItem(new GuiItem(NEXT_ITEM()), nextSlot, 0);

        GuiItem decoItem = DECO_ITEM();

        //Bukkit.broadcastMessage("SetPagingNav decoItem action: " + decoItem);
        for (int x : decoSlots) {
          //  Bukkit.broadcastMessage("adding deco item for " + x);

            pagingNavigationBar.addItem(decoItem, x, 0);

        }
        inventory.addPane(pagingNavigationBar);
        return pagingNavigationBar;
    }



    //----------------------- Utility Methods ----------------------------------------------------


    /**
     * @param material path to the item in menus.yml, for example, "nav-bar.player-profile-head" will retrieve said path
     * @deprecated
     * @return itemstack with integer slot
     */
    public Pair<ItemStack, Integer> getItemFrom(Material material, String path, OfflinePlayer owner) {
        PrivateFile file = new PrivateFile(FileType.MENUS);
        ItemStackUtils builder = new ItemStackUtils(material, owner);
        builder.setName(file.getString(path + ".display"));
        if (file.getConfiguration().isSet(path + ".lore")) {
            List<String> lores = file.getConfiguration().getStringList(path + ".lore");
            List<String> loresColored = new ArrayList<>();
            for (String s : lores) {
                loresColored.add(ChatColor.translateAlternateColorCodes('&', s));
            }
            builder.setLore(loresColored);
        }
        int slot = -1;
        if (file.getConfiguration().isSet(path +".slot")){
            slot = file.getConfiguration().getInt(path + ".slot");
        }
        return new Pair<>(builder.build(), slot);

    }


    /**
     * @param stringList to convert
     * @return converted int list
     * @apiNote will break if stringList doesn't meet parsing conditions
     */
    private List<Integer> getAsInts(List<String> stringList) {
        return stringList.stream().map(Integer::parseInt).collect(Collectors.toList());
    }


    // STATIC ITEMS ------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * @return blank decoration item used to fill white-space
     */
    public static GuiItem DECO_ITEM() {
        PrivateFile items = new PrivateFile(FileType.MENUS);
        ItemStackUtils item = new ItemStackUtils(Material.getMaterial(items.getString("global-items.deco-item.material")));
        String displayName = items.getString("global-items.deco-item.display");


        item.setName(displayName);
        return new GuiItem(item.build(), inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
        });
    }


    // Probably need to refactor, kind of duplicate code with what is in
    // PagingMenu. For now, setting action to send to main menu which will no longer keep this a
    // generalized back button.

    /**
     * @return item used to navigate backwards in a menu
     */
    public static GuiItem BACK_ITEM() {
        PrivateFile items = new PrivateFile(FileType.MENUS);
        ItemStackUtils item = new ItemStackUtils(Material.getMaterial(items.getString("global-items.back-item.material")));
        item.setName(items.getString("global-items.back-item.display"));

        return new GuiItem(item.build(), inventoryClickEvent -> {
            new MainMenuPaging((Player) inventoryClickEvent.getWhoClicked(), PrivateTalk.getInstance()).show();
        });
    }

    /**
     * @return item used to navigate forwards in a menu
     */
    public static ItemStack NEXT_ITEM() {
        PrivateFile items = new PrivateFile(FileType.MENUS);
        ItemStackUtils item = new ItemStackUtils(Material.getMaterial(items.getString("global-items.next-item.material")));
        item.setName(items.getString("global-items.next-item.display"));
        return item.build();
    }

    /**
     *
     * @param path to item in the menus.yml to grab
     * @param owner if a skull set owner here
     * @return item generated from yml
     */
    public static Pair<ItemStack, Integer> getItem(String path, OfflinePlayer owner) {
        PrivateFile file = new PrivateFile(FileType.MENUS);
        ItemStackUtils builder = new ItemStackUtils(Material.valueOf(file.getString(path+".material")), owner);
        builder.setName(file.getString(path + ".display"));
        if (file.getConfiguration().isSet(path + ".lore")) {
            List<String> lores = file.getConfiguration().getStringList(path + ".lore");
            List<String> loresColored = new ArrayList<>();
            for (String s : lores) {
                loresColored.add(ChatColor.translateAlternateColorCodes('&', s));
            }
            builder.setLore(loresColored);
        }
        int slot = -1;
        if (file.getConfiguration().isSet(path + ".slot")){
            slot = file.getConfiguration().getInt(path + ".slot");
        }
        return new Pair<>(builder.build(), slot);

    }

    private Consumer<InventoryClickEvent> clickOnOwnHead(UUID uuid) {
        return event -> {
            PrivatePlayer player = PrivatePlayer.getPlayer(uuid);
            Player viewer = Bukkit.getPlayer(uuid);
            event.setCancelled(true);
            new PlayerProfileMenu(viewer, player, new MainMenuPaging(viewer, PrivateTalk.getInstance()).getMenu()).show();
        };
    }

}
