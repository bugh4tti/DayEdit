package com.day.dayedit.gui;

import com.day.dayedit.DayEdit;
import com.day.dayedit.utils.HeadUtils;
import com.day.dayedit.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MenuManager {

    private static final Map<UUID, String> menuAbierto = new HashMap<>();
    private static final Material MATERIAL_VOLVER = Material.ARROW;

    public static String getMenuAbierto(Player player) {
        return menuAbierto.get(player.getUniqueId());
    }

    public static void limpiarEstado(Player player) {
        menuAbierto.remove(player.getUniqueId());
    }

    public static void abrirMenuPrincipal(Player player) {
        DayEdit plugin = DayEdit.getInstance();
        ConfigurationSection seccion = plugin.getConfig().getConfigurationSection("menu-principal");
        if (seccion == null) return;

        String titulo = ItemUtils.colorize(seccion.getString("titulo", "&#00DAFF&lDayEdit"));
        int tamano = seccion.getInt("tamano", 27);
        Inventory inv = Bukkit.createInventory(null, tamano, titulo);

        ConfigurationSection items = seccion.getConfigurationSection("items");
        if (items != null) {
            for (String clave : items.getKeys(false)) {
                ConfigurationSection item = items.getConfigurationSection(clave);
                if (item == null) continue;

                int slot = item.getInt("slot", 0);
                Material material = materialSeguro(item.getString("material", "STONE"));
                String nombre = item.getString("nombre", clave);
                List<String> lore = item.getStringList("lore");

                inv.setItem(slot, crearItem(material, nombre, lore));
            }
        }

        menuAbierto.put(player.getUniqueId(), "menu-principal");
        player.openInventory(inv);
    }

    public static void abrirMenuLores(Player player) {
        DayEdit plugin = DayEdit.getInstance();
        ConfigurationSection seccion = plugin.getConfig().getConfigurationSection("menu-lores");
        if (seccion == null) return;

        String titulo = ItemUtils.colorize(seccion.getString("titulo", "&#00DAFF&lDayEdit &f- Lores"));
        int tamano = seccion.getInt("tamano", 27);
        Inventory inv = Bukkit.createInventory(null, tamano, titulo);

        List<Map<?, ?>> lista = seccion.getMapList("lista");
        for (Map<?, ?> entrada : lista) {
            int slot = (int) entrada.get("slot");
            Material material = materialSeguro(String.valueOf(entrada.get("material")));
            String texto = String.valueOf(entrada.get("texto"));

            ItemStack item = crearItem(material, texto, new ArrayList<>());
            inv.setItem(slot, item);
        }

        int volverSlot = seccion.getInt("volver-slot", tamano - 5);
        inv.setItem(volverSlot, crearBotonVolver());

        menuAbierto.put(player.getUniqueId(), "menu-lores");
        player.openInventory(inv);
    }

    public static void abrirMenuTematicas(Player player) {
        DayEdit plugin = DayEdit.getInstance();
        ConfigurationSection seccion = plugin.getConfig().getConfigurationSection("menu-tematicas");
        if (seccion == null) return;

        String titulo = ItemUtils.colorize(seccion.getString("titulo", "&#00DAFF&lDayEdit &f- Tematicas"));
        int tamano = seccion.getInt("tamano", 27);
        Inventory inv = Bukkit.createInventory(null, tamano, titulo);

        ConfigurationSection lista = seccion.getConfigurationSection("lista");
        if (lista != null) {
            for (String clave : lista.getKeys(false)) {
                ConfigurationSection tematica = lista.getConfigurationSection(clave);
                if (tematica == null) continue;

                int slot = tematica.getInt("slot", 0);
                Material material = materialSeguro(tematica.getString("material", "PLAYER_HEAD"));
                String nombre = tematica.getString("nombre", clave);
                List<String> lore = tematica.getStringList("lore");

                ItemStack item;
                if (material == Material.PLAYER_HEAD) {
                    item = HeadUtils.crearCabeza(tematica.getString("textura", ""), tematica.getString("jugador", ""));
                    aplicarNombreYLore(item, nombre, lore);
                } else {
                    item = crearItem(material, nombre, lore);
                }

                inv.setItem(slot, item);
            }
        }

        int volverSlot = seccion.getInt("volver-slot", tamano - 5);
        inv.setItem(volverSlot, crearBotonVolver());

        menuAbierto.put(player.getUniqueId(), "menu-tematicas");
        player.openInventory(inv);
    }

    public static void abrirSubmenuTematica(Player player, String claveTematica) {
        DayEdit plugin = DayEdit.getInstance();
        ConfigurationSection seccion = plugin.getConfig().getConfigurationSection("tematica-" + claveTematica);
        if (seccion == null) {
            player.sendMessage(ItemUtils.colorize("&cEsa tematica no tiene personajes configurados."));
            return;
        }

        String titulo = ItemUtils.colorize(seccion.getString("titulo", "&#00DAFF&lDayEdit"));
        int tamano = seccion.getInt("tamano", 54);
        Inventory inv = Bukkit.createInventory(null, tamano, titulo);

        ConfigurationSection personajes = seccion.getConfigurationSection("personajes");
        if (personajes != null) {
            for (String clave : personajes.getKeys(false)) {
                ConfigurationSection personaje = personajes.getConfigurationSection(clave);
                if (personaje == null) continue;

                int slot = personaje.getInt("slot", 0);
                Material material = materialSeguro(personaje.getString("material", "PLAYER_HEAD"));
                String nombre = personaje.getString("nombre", clave);

                ItemStack item;
                if (material == Material.PLAYER_HEAD) {
                    item = HeadUtils.crearCabeza(personaje.getString("textura", ""), personaje.getString("jugador", ""));
                    aplicarNombreYLore(item, nombre, new ArrayList<>());
                } else {
                    item = crearItem(material, nombre, new ArrayList<>());
                }

                inv.setItem(slot, item);
            }
        }

        int volverSlot = seccion.getInt("volver-slot", tamano - 5);
        inv.setItem(volverSlot, crearBotonVolver());

        menuAbierto.put(player.getUniqueId(), "tematica-" + claveTematica);
        player.openInventory(inv);
    }

    private static ItemStack crearItem(Material material, String nombre, List<String> lore) {
        ItemStack item = new ItemStack(material);
        aplicarNombreYLore(item, nombre, lore);
        return item;
    }

    private static void aplicarNombreYLore(ItemStack item, String nombre, List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ItemUtils.colorize(nombre));
        if (lore != null && !lore.isEmpty()) {
            List<String> loreColoreado = new ArrayList<>();
            for (String linea : lore) {
                loreColoreado.add(ItemUtils.colorize(linea));
            }
            meta.setLore(loreColoreado);
        }
        item.setItemMeta(meta);
    }

    private static ItemStack crearBotonVolver() {
        return crearItem(MATERIAL_VOLVER, "&c&lVolver", new ArrayList<>());
    }

    private static Material materialSeguro(String nombre) {
        try {
            return Material.valueOf(nombre.toUpperCase());
        } catch (Exception e) {
            return Material.STONE;
        }
    }
          }
