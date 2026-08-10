package com.day.dayedit.gui;

import com.day.dayedit.DayEdit;
import com.day.dayedit.utils.ItemUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MenuListener implements Listener {

    private final DayEdit plugin;

    public MenuListener(DayEdit plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String menuActual = MenuManager.getMenuAbierto(player);
        if (menuActual == null) return;

        String rutaConfig = menuActual;
        ConfigurationSection seccion = plugin.getConfig().getConfigurationSection(rutaConfig);
        if (seccion == null) return;

        String tituloEsperado = ItemUtils.colorize(seccion.getString("titulo", ""));
        if (!event.getView().getTitle().equals(tituloEsperado)) return;

        event.setCancelled(true);

        if (event.getClickedInventory() == null
                || !event.getClickedInventory().equals(event.getView().getTopInventory())) {
            return;
        }

        int slot = event.getSlot();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        switch (menuActual) {
            case "menu-principal":
                manejarMenuPrincipal(player, seccion, slot);
                break;
            case "menu-lores":
                manejarMenuLores(player, seccion, slot);
                break;
            case "menu-tematicas":
                manejarMenuTematicas(player, seccion, slot);
                break;
            default:
                if (menuActual.startsWith("tematica-")) {
                    manejarSubmenuTematica(player, seccion, slot, menuActual.substring("tematica-".length()));
                }
                break;
        }
    }

    private void manejarMenuPrincipal(Player player, ConfigurationSection seccion, int slot) {
        ConfigurationSection items = seccion.getConfigurationSection("items");
        if (items == null) return;

        for (String clave : items.getKeys(false)) {
            ConfigurationSection item = items.getConfigurationSection(clave);
            if (item == null) continue;
            if (item.getInt("slot", -1) != slot) continue;

            if (clave.equalsIgnoreCase("lores")) {
                MenuManager.abrirMenuLores(player);
            } else if (clave.equalsIgnoreCase("tematicas")) {
                MenuManager.abrirMenuTematicas(player);
            }
            return;
        }
    }

    private void manejarMenuLores(Player player, ConfigurationSection seccion, int slot) {
        int volverSlot = seccion.getInt("volver-slot", -1);
        if (slot == volverSlot) {
            MenuManager.abrirMenuPrincipal(player);
            return;
        }

        List<Map<?, ?>> lista = seccion.getMapList("lista");
        for (Map<?, ?> entrada : lista) {
            int slotEntrada = (int) entrada.get("slot");
            if (slotEntrada != slot) continue;

            String texto = String.valueOf(entrada.get("texto"));
            aplicarLoreAItemEnMano(player, texto);
            return;
        }
    }

    private void manejarMenuTematicas(Player player, ConfigurationSection seccion, int slot) {
        int volverSlot = seccion.getInt("volver-slot", -1);
        if (slot == volverSlot) {
            MenuManager.abrirMenuPrincipal(player);
            return;
        }

        ConfigurationSection lista = seccion.getConfigurationSection("lista");
        if (lista == null) return;

        for (String clave : lista.getKeys(false)) {
            ConfigurationSection tematica = lista.getConfigurationSection(clave);
            if (tematica == null) continue;
            if (tematica.getInt("slot", -1) != slot) continue;

            MenuManager.abrirSubmenuTematica(player, clave);
            return;
        }
    }

    private void manejarSubmenuTematica(Player player, ConfigurationSection seccion, int slot, String claveTematica) {
        int volverSlot = seccion.getInt("volver-slot", -1);
        if (slot == volverSlot) {
            MenuManager.abrirMenuTematicas(player);
            return;
        }

        ConfigurationSection personajes = seccion.getConfigurationSection("personajes");
        if (personajes == null) return;

        for (String clave : personajes.getKeys(false)) {
            ConfigurationSection personaje = personajes.getConfigurationSection(clave);
            if (personaje == null) continue;
            if (personaje.getInt("slot", -1) != slot) continue;

            String nombre = personaje.getString("nombre", clave);
            aplicarNombreAItemEnMano(player, nombre, clave);
            return;
        }
    }

    private void aplicarLoreAItemEnMano(Player player, String texto) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(ItemUtils.colorize(plugin.getMsg("sin-item")));
            return;
        }

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add(ItemUtils.colorize(texto));
        meta.setLore(lore);
        item.setItemMeta(meta);

        player.sendMessage(ItemUtils.colorize(plugin.getMsg("lore-menu-agregado")));
    }

    private void aplicarNombreAItemEnMano(Player player, String nombre, String claveVisible) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(ItemUtils.colorize(plugin.getMsg("sin-item")));
            return;
        }

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ItemUtils.colorize(nombre));
        item.setItemMeta(meta);

        String mensaje = plugin.getMsg("tematica-aplicada").replace("%personaje%", claveVisible);
        player.sendMessage(ItemUtils.colorize(mensaje));
    }
          }
