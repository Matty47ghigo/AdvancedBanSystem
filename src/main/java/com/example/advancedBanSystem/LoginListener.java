package com.example.advancedBanSystem;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.ChatColor;

import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginListener implements Listener {

    private final AdvancedBanSystem plugin;

    public LoginListener(AdvancedBanSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        String playerName = event.getPlayer().getName();

        // Ottieni l'indirizzo IP correttamente
        String ipAddress;
        try {
            InetAddress address = InetAddress.getByAddress(event.getAddress().getAddress());
            ipAddress = address.getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            ipAddress = "Unknown";
        }

        // Verifica se il giocatore è nella tabella BannedPlayers
        if (isBanned(playerName)) {
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, ChatColor.RED + "Sei stato bannato dal server.");
            return;
        }

        // Verifica se l'IP del giocatore è nella tabella BannedIPs
        if (isIpBanned(ipAddress)) {
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, ChatColor.RED + "Il tuo IP è bannato dal server.");
            return;
        }

        // Verifica se il giocatore è nella tabella BlacklistedPlayers
        if (isBlacklisted(playerName)) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Non sei consentito su questo server.");
            return;
        }
    }

    private boolean isBanned(String playerName) {
        try (PreparedStatement stmt = plugin.getDatabaseConnection().prepareStatement("SELECT * FROM BannedPlayers WHERE playerName = ?")) {
            stmt.setString(1, playerName.toLowerCase());
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Restituisce true se il giocatore è bannato
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isIpBanned(String ipAddress) {
        try (PreparedStatement stmt = plugin.getDatabaseConnection().prepareStatement("SELECT * FROM BannedIPs WHERE ipAddress = ?")) {
            stmt.setString(1, ipAddress);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Restituisce true se l'IP è bannato
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isBlacklisted(String playerName) {
        try (PreparedStatement stmt = plugin.getDatabaseConnection().prepareStatement("SELECT * FROM BlacklistedPlayers WHERE playerName = ?")) {
            stmt.setString(1, playerName.toLowerCase());
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Restituisce true se il giocatore è blacklisted
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}