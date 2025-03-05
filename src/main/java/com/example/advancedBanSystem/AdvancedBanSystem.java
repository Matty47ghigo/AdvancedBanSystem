package com.example.advancedBanSystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

public class AdvancedBanSystem extends JavaPlugin {

    private final Set<String> bannedPlayers = new HashSet<>();
    private final Set<String> blacklistedPlayers = new HashSet<>();
    private final String logFilePath = getDataFolder() + File.separator + "banlog.txt";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadBannedPlayers();
        loadBlacklistedPlayers();
        getLogger().info("AdvancedBanSystem è stato attivato!");
    }

    @Override
    public void onDisable() {
        saveBannedPlayers();
        saveBlacklistedPlayers();
        getLogger().info("AdvancedBanSystem è stato disattivato!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("ban")) {
            return handleBanCommand(sender, args);
        } else if (command.getName().equalsIgnoreCase("unban")) {
            return handleUnbanCommand(sender, args);
        } else if (command.getName().equalsIgnoreCase("ipban")) {
            return handleIpBanCommand(sender, args);
        } else if (command.getName().equalsIgnoreCase("ipunban")) {
            return handleIpUnbanCommand(sender, args);
        } else if (command.getName().equalsIgnoreCase("blacklist")) {
            return handleBlacklistCommand(sender, args);
        } else if (command.getName().equalsIgnoreCase("banlist")) {
            return handleBanListCommand(sender);
        } else if (command.getName().equalsIgnoreCase("blacklistlist")) {
            return handleBlacklistListCommand(sender);
        }
        return false;
    }

    private boolean handleBanCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Uso: /ban <player> [reason]");
            return true;
        }
        String player = args[0];
        String reason = String.join(" ", args).replaceFirst(args[0], "").trim();
        Bukkit.getPlayer(player).kickPlayer(ChatColor.RED + "Sei stato bannato dal server!" + (reason.isEmpty() ? "" : " Motivo: " + reason));
        bannedPlayers.add(player.toLowerCase());
        saveBannedPlayers();
        logAction("Banned player: " + player + (reason.isEmpty() ? "" : " (Reason: " + reason + ")"));
        sender.sendMessage(ChatColor.GREEN + "Giocatore bannato: " + player);
        return true;
    }

    private boolean handleUnbanCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Uso: /unban <player>");
            return true;
        }
        String player = args[0].toLowerCase();
        if (!bannedPlayers.contains(player)) {
            sender.sendMessage(ChatColor.RED + "Il giocatore non è bannato.");
            return true;
        }
        bannedPlayers.remove(player);
        saveBannedPlayers();
        logAction("Unbanned player: " + player);
        sender.sendMessage(ChatColor.GREEN + "Giocatore sbannato: " + player);
        return true;
    }

    private boolean handleIpBanCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Uso: /ipban <player> [reason]");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Giocatore non trovato.");
            return true;
        }
        String ip = target.getAddress().getAddress().getHostAddress();
        String reason = String.join(" ", args).replaceFirst(args[0], "").trim();
        Bukkit.getBanList(org.bukkit.BanList.Type.IP).addBan(ip, reason, null, sender.getName());
        logAction("IP banned: " + ip + (reason.isEmpty() ? "" : " (Reason: " + reason + ")"));
        sender.sendMessage(ChatColor.GREEN + "IP bannato: " + ip);
        return true;
    }

    private boolean handleIpUnbanCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Uso: /ipunban <ip>");
            return true;
        }
        String ip = args[0];
        if (!Bukkit.getBanList(org.bukkit.BanList.Type.IP).isBanned(ip)) {
            sender.sendMessage(ChatColor.RED + "L'IP non è bannato.");
            return true;
        }
        Bukkit.getBanList(org.bukkit.BanList.Type.IP).pardon(ip);
        logAction("IP unbanned: " + ip);
        sender.sendMessage(ChatColor.GREEN + "IP sbannato: " + ip);
        return true;
    }

    private boolean handleBlacklistCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Uso: /blacklist add|remove <player/ip>");
            return true;
        }
        String action = args[0].toLowerCase();
        String target = args[1];
        if (action.equals("add")) {
            blacklistedPlayers.add(target.toLowerCase());
            saveBlacklistedPlayers();
            logAction("Added to blacklist: " + target);
            sender.sendMessage(ChatColor.GREEN + "Aggiunto alla blacklist: " + target);
        } else if (action.equals("remove")) {
            if (!blacklistedPlayers.contains(target.toLowerCase())) {
                sender.sendMessage(ChatColor.RED + "Non presente nella blacklist.");
                return true;
            }
            blacklistedPlayers.remove(target.toLowerCase());
            saveBlacklistedPlayers();
            logAction("Removed from blacklist: " + target);
            sender.sendMessage(ChatColor.GREEN + "Rimosso dalla blacklist: " + target);
        } else {
            sender.sendMessage(ChatColor.RED + "Azione non valida.");
        }
        return true;
    }

    private boolean handleBanListCommand(CommandSender sender) {
        if (bannedPlayers.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Nessun giocatore bannato.");
            return true;
        }
        sender.sendMessage(ChatColor.YELLOW + "Lista dei giocatori bannati:");
        bannedPlayers.forEach(player -> sender.sendMessage("- " + player));
        return true;
    }

    private boolean handleBlacklistListCommand(CommandSender sender) {
        if (blacklistedPlayers.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "La blacklist è vuota.");
            return true;
        }
        sender.sendMessage(ChatColor.YELLOW + "Lista nera:");
        blacklistedPlayers.forEach(player -> sender.sendMessage("- " + player));
        return true;
    }

    private void loadBannedPlayers() {
        File file = new File(getDataFolder(), "banned_players.txt");
        if (!file.exists()) return;
        try {
            Files.readAllLines(file.toPath()).forEach(line -> bannedPlayers.add(line.toLowerCase()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveBannedPlayers() {
        File file = new File(getDataFolder(), "banned_players.txt");
        try (FileWriter writer = new FileWriter(file)) {
            bannedPlayers.forEach(player -> {
                try {
                    writer.write(player + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBlacklistedPlayers() {
        File file = new File(getDataFolder(), "blacklisted_players.txt");
        if (!file.exists()) return;
        try {
            Files.readAllLines(file.toPath()).forEach(line -> blacklistedPlayers.add(line.toLowerCase()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveBlacklistedPlayers() {
        File file = new File(getDataFolder(), "blacklisted_players.txt");
        try (FileWriter writer = new FileWriter(file)) {
            blacklistedPlayers.forEach(player -> {
                try {
                    writer.write(player + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logAction(String message) {
        try (FileWriter writer = new FileWriter(logFilePath, true)) {
            writer.write(message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}