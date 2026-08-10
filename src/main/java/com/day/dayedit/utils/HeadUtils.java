package com.day.dayedit.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeadUtils {

    private static final Pattern URL_PATTERN = Pattern.compile("\"url\"\\s*:\\s*\"(.*?)\"");

    public static ItemStack crearCabeza(String textura, String jugador) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);

        if (textura != null && !textura.trim().isEmpty()) {
            ItemStack conTextura = aplicarTextura(head, textura.trim());
            if (conTextura != null) {
                return conTextura;
            }
        }

        if (jugador != null && !jugador.trim().isEmpty()) {
            aplicarJugador(head, jugador.trim());
        }

        return head;
    }

    private static ItemStack aplicarTextura(ItemStack head, String base64Textura) {
        try {
            String decodificado = new String(Base64.getDecoder().decode(base64Textura));
            Matcher matcher = URL_PATTERN.matcher(decodificado);
            if (!matcher.find()) {
                return null;
            }
            String urlTextura = matcher.group(1);

            SkullMeta meta = (SkullMeta) head.getItemMeta();
            PlayerProfile perfil = Bukkit.createPlayerProfile(UUID.randomUUID());
            PlayerTextures texturas = perfil.getTextures();
            texturas.setSkin(new URL(urlTextura));
            perfil.setTextures(texturas);
            meta.setOwnerProfile(perfil);
            head.setItemMeta(meta);
            return head;
        } catch (IllegalArgumentException | MalformedURLException | NullPointerException e) {
            return null;
        }
    }

    private static void aplicarJugador(ItemStack head, String nombreJugador) {
        try {
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(nombreJugador);
            meta.setOwningPlayer(offlinePlayer);
            head.setItemMeta(meta);
        } catch (Exception ignored) {
        }
    }
}
