/*
 * TheBridge - Defend your base and try to wipe out the others
 * Copyright (C)  2021  Plugily Projects - maintained by Tigerpanzer_02, 2Wild4You and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package plugily.projects.thebridge.commands.arguments.game;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.plajerlair.commonsbox.minecraft.compat.XMaterial;
import plugily.projects.thebridge.Main;
import plugily.projects.thebridge.arena.Arena;
import plugily.projects.thebridge.arena.ArenaManager;
import plugily.projects.thebridge.arena.ArenaRegistry;
import plugily.projects.thebridge.commands.arguments.ArgumentsRegistry;
import plugily.projects.thebridge.commands.arguments.data.CommandArgument;
import plugily.projects.thebridge.commands.arguments.data.LabelData;
import plugily.projects.thebridge.commands.arguments.data.LabeledCommandArgument;
import plugily.projects.thebridge.handlers.ChatManager;
import plugily.projects.thebridge.handlers.language.LanguageManager;
import plugily.projects.thebridge.utils.Utils;

import java.util.ArrayList;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 23.11.2020
 */
public class ArenaSelectorArgument implements Listener {

  private final ChatManager chatManager;

  public ArenaSelectorArgument(ArgumentsRegistry registry, ChatManager chatManager) {
    this.chatManager = chatManager;
    registry.getPlugin().getServer().getPluginManager().registerEvents(this, registry.getPlugin());
    registry.mapArgument("thebridge", new LabeledCommandArgument("arenas", "thebridge.arenas", CommandArgument.ExecutorType.PLAYER,
      new LabelData("/tb arenas", "/tb arenas", "&7Select an arena\n&6Permission: &7thebridge.arenas")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (ArenaRegistry.getArenas().size() == 0) {
          player.sendMessage(chatManager.colorMessage("Validator.No-Instances-Created"));
          return;
        }
        Inventory inventory = Bukkit.createInventory(player, Utils.serializeInt(ArenaRegistry.getArenas().size()), chatManager.colorMessage("Arena-Selector.Inv-Title"));
        for (Arena arena : ArenaRegistry.getArenas()) {
          ItemStack itemStack;
          switch (arena.getArenaState()) {
            case WAITING_FOR_PLAYERS:
              itemStack = XMaterial.LIME_CONCRETE.parseItem();
              break;
            case STARTING:
              itemStack = XMaterial.YELLOW_CONCRETE.parseItem();
              break;
            default:
              itemStack = XMaterial.RED_CONCRETE.parseItem();
              break;
          }
          ItemMeta itemMeta = itemStack.getItemMeta();
          itemMeta.setDisplayName(arena.getId());

          ArrayList<String> lore = new ArrayList<>();
          for (String string : LanguageManager.getLanguageList("Arena-Selector.Item.Lore")) {
            lore.add(formatItem(string, arena, registry.getPlugin()));
          }

          itemMeta.setLore(lore);
          itemStack.setItemMeta(itemMeta);
          inventory.addItem(itemStack);
        }
        player.openInventory(inventory);
      }
    });

  }

  private String formatItem(String string, Arena arena, Main plugin) {
    String formatted = string;
    formatted = StringUtils.replace(formatted, "%mapname%", arena.getMapName());
    if (arena.getPlayers().size() >= arena.getMaximumPlayers()) {
      formatted = StringUtils.replace(formatted, "%state%", chatManager.colorMessage("Signs.Game-States.Full-Game"));
    } else {
      formatted = StringUtils.replace(formatted, "%state%", plugin.getSignManager().getGameStateToString().get(arena.getArenaState()));
    }
    formatted = StringUtils.replace(formatted, "%playersize%", String.valueOf(arena.getPlayers().size()));
    formatted = StringUtils.replace(formatted, "%maxplayers%", String.valueOf(arena.getMaximumPlayers()));
    formatted = chatManager.colorRawMessage(formatted);
    return formatted;
  }

  @EventHandler
  public void onArenaSelectorMenuClick(InventoryClickEvent e) {
    if (!e.getView().getTitle().equals(chatManager.colorMessage("Arena-Selector.Inv-Title"))) {
      return;
    }
    if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) {
      return;
    }
    Player player = (Player) e.getWhoClicked();
    player.closeInventory();

    Arena arena = ArenaRegistry.getArena(e.getCurrentItem().getItemMeta().getDisplayName());
    if (arena != null) {
      ArenaManager.joinAttempt(player, arena);
    } else {
      player.sendMessage(chatManager.getPrefix() + chatManager.colorMessage("Commands.No-Arena-Like-That"));
    }
  }

}
