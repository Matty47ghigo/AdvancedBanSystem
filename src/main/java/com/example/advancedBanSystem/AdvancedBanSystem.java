package com.example.advancedBanSystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class AdvancedBanSystem extends JavaPlugin {

    private Connection connection;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        setupDatabase();

        // Registra il listener per l'evento di login
        Bukkit.getPluginManager().registerEvents(new LoginListener(this), this);

        getLogger().info("AdvancedBanSystem è stato attivato!");
    }

    @Override
    public void onDisable() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        getLogger().info("AdvancedBanSystem è stato disattivato!");
    }

    private void setupDatabase() {
        try {
            File dbFile = new File(getDataFolder(), "bans.db");
            if (!dbFile.exists()) {
                dbFile.createNewFile();
            }

            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

            createTable("BannedPlayers", "playerName TEXT PRIMARY KEY, reason TEXT, bannedBy TEXT, timestamp TEXT");
            createTable("BannedIPs", "ipAddress TEXT PRIMARY KEY, reason TEXT, bannedBy TEXT, timestamp TEXT");
            createTable("BlacklistedPlayers", "playerName TEXT PRIMARY KEY");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTable(String tableName, String columns) {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS " + tableName + " (" + columns + ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "ban":
                return handleBanCommand(sender, args);
            case "unban":
                return handleUnbanCommand(sender, args);
            case "ipban":
                return handleIpBanCommand(sender, args);
            case "ipunban":
                return handleIpUnbanCommand(sender, args);
            case "blacklist":
                return handleBlacklistCommand(sender, args);
            case "banlist":
                return handleBanListCommand(sender);
            case "blacklistlist":
                return handleBlacklistListCommand(sender);
            default:
                return false;
        }
    }

    private boolean checkPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }

    private boolean handleBanCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Uso: /ban <player> [reason]");
            return true;
        }

        String targetPlayer = args[0];
        String reason = String.join(" ", args).replaceFirst(args[0], "").trim();

        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR IGNORE INTO BannedPlayers (playerName, reason, bannedBy, timestamp) VALUES (?, ?, ?, ?)"
        )) {
            stmt.setString(1, targetPlayer.toLowerCase());
            stmt.setString(2, reason.isEmpty() ? "No reason" : reason);
            stmt.setString(3, getSenderName(sender));
            stmt.setString(4, String.valueOf(System.currentTimeMillis()));
            stmt.executeUpdate();

            Player target = Bukkit.getPlayer(targetPlayer);
            if (target != null) {
                target.kickPlayer(ChatColor.RED + "Sei stato bannato dal server!" + (reason.isEmpty() ? "" : " Motivo: " + reason));
            }
            sender.sendMessage(ChatColor.GREEN + "Giocatore bannato: " + targetPlayer);

        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "Si è verificato un errore durante il ban.");
        }
        return true;
    }

    private boolean handleUnbanCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Uso: /unban <player>");
            return true;
        }

        String targetPlayer = args[0].toLowerCase();

        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM BannedPlayers WHERE playerName = ?")) {
            stmt.setString(1, targetPlayer);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                sender.sendMessage(ChatColor.GREEN + "Giocatore sbannato: " + targetPlayer);
            } else {
                sender.sendMessage(ChatColor.RED + "Il giocatore non è bannato.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "Si è verificato un errore durante lo sbancimento.");
        }
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

        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR IGNORE INTO BannedIPs (ipAddress, reason, bannedBy, timestamp) VALUES (?, ?, ?, ?)"
        )) {
            stmt.setString(1, ip);
            stmt.setString(2, args.length > 1 ? String.join(" ", args).replaceFirst(args[0], "").trim() : "No reason");
            stmt.setString(3, getSenderName(sender));
            stmt.setString(4, String.valueOf(System.currentTimeMillis()));
            stmt.executeUpdate();

            sender.sendMessage(ChatColor.GREEN + "IP bannato: " + ip);

        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "Si è verificato un errore durante il ban dell'IP.");
        }
        return true;
    }

    private boolean handleIpUnbanCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Uso: /ipunban <ip>");
            return true;
        }

        String ip = args[0];

        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM BannedIPs WHERE ipAddress = ?")) {
            stmt.setString(1, ip);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                sender.sendMessage(ChatColor.GREEN + "IP sbannato: " + ip);
            } else {
                sender.sendMessage(ChatColor.RED + "L'IP non è bannato.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "Si è verificato un errore durante lo sbancimento dell'IP.");
        }
        return true;
    }

    private boolean handleBlacklistCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Uso: /blacklist add|remove <player/ip>");
            return true;
        }

        String action = args[0].toLowerCase();
        String target = args[1];

        try {
            if (action.equals("add")) {
                try (PreparedStatement stmt = connection.prepareStatement(
                        "INSERT OR IGNORE INTO BlacklistedPlayers (playerName) VALUES (?)"
                )) {
                    stmt.setString(1, target.toLowerCase());
                    stmt.executeUpdate();
                    sender.sendMessage(ChatColor.GREEN + "Aggiunto alla blacklist: " + target);
                }
            } else if (action.equals("remove")) {
                try (PreparedStatement stmt = connection.prepareStatement(
                        "DELETE FROM BlacklistedPlayers WHERE playerName = ?"
                )) {
                    stmt.setString(1, target.toLowerCase());
                    int rowsAffected = stmt.executeUpdate();

                    if (rowsAffected > 0) {
                        sender.sendMessage(ChatColor.GREEN + "Rimosso dalla blacklist: " + target);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Non presente nella blacklist.");
                    }
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Azione non valida.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "Si è verificato un errore durante la gestione della blacklist.");
        }
        return true;
    }

    private boolean handleBanListCommand(CommandSender sender) {
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM BannedPlayers")) {
            if (!rs.next()) {
                sender.sendMessage(ChatColor.YELLOW + "Nessun giocatore bannato.");
                return true;
            }

            sender.sendMessage(ChatColor.YELLOW + "Lista dei giocatori bannati:");
            do {
                sender.sendMessage("- " + rs.getString("playerName") + " (Motivo: " + rs.getString("reason") + ")");
            } while (rs.next());
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "Si è verificato un errore durante il recupero della lista dei bannati.");
        }
        return true;
    }

    private boolean handleBlacklistListCommand(CommandSender sender) {
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM BlacklistedPlayers")) {
            if (!rs.next()) {
                sender.sendMessage(ChatColor.YELLOW + "La blacklist è vuota.");
                return true;
            }

            sender.sendMessage(ChatColor.YELLOW + "Lista nera:");
            do {
                sender.sendMessage("- " + rs.getString("playerName"));
            } while (rs.next());
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "Si è verificato un errore durante il recupero della blacklist.");
        }
        return true;
    }

    // Metodo per ottenere il nome del mittente (giocatore o console)
    private String getSenderName(CommandSender sender) {
        if (sender instanceof Player player) {
            return player.getName();
        } else {
            return "Console";
        }
    }

    // Getter per la connessione al database
    public Connection getDatabaseConnection() {
        return connection;
    }
}