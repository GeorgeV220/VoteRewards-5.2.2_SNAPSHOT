package com.georgev22.voterewards.playerdata;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.configmanager.FileManager;
import com.georgev22.voterewards.utilities.MaterialUtil;
import com.georgev22.voterewards.utilities.MessagesUtil;
import com.georgev22.voterewards.utilities.SoundUtil;
import com.georgev22.voterewards.utilities.Utils;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class VotePartyUtils {

	private static VotePartyUtils instance;

	public static VotePartyUtils getInstance() {
		return instance == null ? instance = new VotePartyUtils() : instance;
	}

	private VoteRewardPlugin voteRewardPlugin = VoteRewardPlugin.getInstance();

	private Set<OfflinePlayer> players = Sets.newHashSet();

	public void run(OfflinePlayer player) {

		if (!VoteOptions.VOTE_PARTY.isEnabled()) {
			return;
		}

		final FileManager fm = FileManager.getInstance();

		final FileConfiguration dataFile = fm.getData().getFileConfiguration();

		dataFile.set("VoteParty-Votes", dataFile.getInt("VoteParty-Votes", 0) + 1);
		fm.getData().saveFile();

		int maxVotes = fm.getConfig().getFileConfiguration().getInt("VoteParty.votes", 2);
		int currentVotes = dataFile.getInt("VoteParty-Votes", 0);

		final Map<String, String> placeholders = Maps.newHashMap();
		if (maxVotes - currentVotes > 0) {
			placeholders.put("%votes%", Utils.formatNumber(maxVotes - currentVotes));
			MessagesUtil.VOTEPARTY_VOTES_NEED.msgAll(placeholders, true);
			placeholders.clear();
		}

		if (PartyOptions.PARTICIPATE.isEnabled()) {
			players.add(player);
		} else {
			this.players.addAll(Bukkit.getOnlinePlayers());
		}

		if (currentVotes < maxVotes) {
			return;

		}
		if (PartyOptions.COOLDOWN.isEnabled()) {
			placeholders.put("%secs%",
					String.valueOf(voteRewardPlugin.getConfig().getInt("VoteParty.cooldown.seconds")));
			MessagesUtil.VOTEPARTY_START.msgAll(placeholders, true);
			placeholders.clear();

			for (Player all : Bukkit.getOnlinePlayers())
				if (!players.contains(all)) {
					MessagesUtil.VOTEPARTY_NOT_PARTICIPATED.msg(all);
				}
			new BukkitRunnable() {
				@Override
				public void run() {
					for (OfflinePlayer offlinePlayer : players) {
						if (PartyOptions.CRATE.isEnabled()) {
							if (offlinePlayer != null && offlinePlayer.isOnline()) {
								offlinePlayer.getPlayer().getInventory().addItem(crate(1));

							}
						} else {
							chooseRandom(PartyOptions.RANDOM.isEnabled(), offlinePlayer);
						}
						if (offlinePlayer.isOnline() && PartyOptions.SOUND_START.isEnabled()) {
							offlinePlayer.getPlayer().playSound(offlinePlayer.getPlayer().getLocation(), SoundUtil
									.getSound(voteRewardPlugin.getConfig().getString("Sounds.VotePartyStart")).get(),
									1000, 1);
						}
					}
					players.clear();
				}
			}.runTaskLaterAsynchronously(voteRewardPlugin,
					fm.getConfig().getFileConfiguration().getLong("VoteParty.cooldown.seconds") * 20l);

		} else {

			for (OfflinePlayer offlinePlayer : players) {

				if (PartyOptions.CRATE.isEnabled()) {
					final Player off = offlinePlayer.getPlayer();
					if (off != null) {
						off.getInventory().addItem(crate(1));
					}

				} else {
					chooseRandom(PartyOptions.RANDOM.isEnabled(), offlinePlayer);
				}

			}

			players.clear();

		}

		dataFile.set("VoteParty-Votes", 0);
		fm.getData().saveFile();
	}

	/**
	 * Choose random voteparty rewards
	 */
	public void chooseRandom(boolean enable, OfflinePlayer offlinePlayer) {
		List<String> list = voteRewardPlugin.getConfig().getStringList("VoteParty.rewards");
		if (enable) {
			Random random = new Random();
			int selector = random.nextInt(list.size());
			runCommands(list.get(selector).replace("%player%", offlinePlayer.getName()));
		} else {
			for (String s : list) {
				runCommands(s.replace("%player%", offlinePlayer.getName()));
			}
		}
	}

	/**
	 * Start the VoteParty
	 */
	public void start() {
		final FileManager fm = FileManager.getInstance();

		final Map<String, String> placeholders = Maps.newHashMap();

		if (PartyOptions.COOLDOWN.isEnabled()) {
			placeholders.put("%secs%",
					String.valueOf(voteRewardPlugin.getConfig().getInt("VoteParty.cooldown.seconds")));
			MessagesUtil.VOTEPARTY_START.msgAll(placeholders, true);
			placeholders.clear();

			new BukkitRunnable() {
				@Override
				public void run() {
					for (OfflinePlayer offlinePlayer : Bukkit.getOnlinePlayers()) {
						if (PartyOptions.CRATE.isEnabled()) {
							if (offlinePlayer != null && offlinePlayer.isOnline()) {
								offlinePlayer.getPlayer().getInventory().addItem(crate(1));

							}
						} else {
							chooseRandom(PartyOptions.RANDOM.isEnabled(), offlinePlayer);
						}
						if (offlinePlayer.isOnline() && PartyOptions.SOUND_START.isEnabled()) {
							offlinePlayer.getPlayer().playSound(offlinePlayer.getPlayer().getLocation(), SoundUtil
									.getSound(voteRewardPlugin.getConfig().getString("Sounds.VotePartyStart")).get(),
									1000, 1);
						}
					}
				}
			}.runTaskLaterAsynchronously(voteRewardPlugin,
					fm.getConfig().getFileConfiguration().getLong("VoteParty.cooldown.seconds") * 20l);

		}

	}

	public ItemStack crate(int amount) {
		ItemStack itemStack = new ItemStack(
				MaterialUtil.valueOf(voteRewardPlugin.getConfig().getString("VoteParty.crate.item")).parseMaterial());
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName(Utils.colorize(voteRewardPlugin.getConfig().getString("VoteParty.crate.name")));
		itemMeta.setLore(Utils.colorize(voteRewardPlugin.getConfig().getStringList("VoteParty.crate.lores")));
		itemStack.setItemMeta(itemMeta);
		itemStack.setAmount(amount);
		return itemStack;
	}

	/**
	 * Run the commands from config
	 *
	 * @param s
	 */
	private void runCommands(String s) {
		if (s == null)
			return;
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Utils.colorize(s));
	}
}
