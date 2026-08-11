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

        ConfigurationSection seccion = plugin.getConfig().getConfigurationSection(menuActual);
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

        if (menuActual.equals("menu-principal")) {
            manejarMenuPrincipal(player, seccion, slot);
        } else if (menuActual.equals("menu-lores")) {
            manejarMenuLores(player, seccion, slot);
        } else if (menuActual.equals("menu-tematicas")) {
            manejarMenuTematicas(player, seccion, slot);
        } else if (menuActual.startsWith("tematica-")) {
            manejarSubmenuTematica(player, seccion, slot, menuActual.substring("tematica-".length()));
        } else if (menuActual.startsWith("categoria-lore-")) {
            manejarCategoriaLore(player, seccion, slot, menuActual.substring("categoria-lore-".length()));
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

        ConfigurationSection categorias = seccion.getConfigurationSection("categorias");
        if (categorias == null) return;

        for (String clave : categorias.getKeys(false)) {
            ConfigurationSection categoria = categorias.getConfigurationSection(clave);
            if (categoria == null) continue;
            if (categoria.getInt("slot", -1) != slot) continue;

            MenuManager.abrirCategoriaLore(player, clave, 0);
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

    private void manejarCategoriaLore(Player player, ConfigurationSection seccion, int slot, String claveCategoria) {
        int anteriorSlot = seccion.getInt("anterior-slot", -1);
        int volverSlot = seccion.getInt("volver-slot", -1);
        int siguienteSlot = seccion.getInt("siguiente-slot", -1);
        int pagina = MenuManager.getPaginaActual(player);

        if (slot == volverSlot) {
            MenuManager.abrirMenuLores(player);
            return;
        }
        if (slot == anteriorSlot) {
            if (pagina > 0) {
                MenuManager.abrirCategoriaLore(player, claveCategoria, pagina - 1);
            }
            return;
        }
        if (slot == siguienteSlot) {
            MenuManager.abrirCategoriaLore(player, claveCategoria, pagina + 1);
            return;
        }

        int indiceEnGrid = -1;
        int[] grid = MenuManager.SLOTS_GRID_54;
        for (int i = 0; i < grid.length; i++) {
            if (grid[i] == slot) {
                indiceEnGrid = i;
                break;
            }
        }
        if (indiceEnGrid == -1) return;

        int minimo = seccion.getInt("minimo", 1);
        int maximo = seccion.getInt("maximo", 255);
        int valor = minimo + (pagina * grid.length) + indiceEnGrid;
        if (valor > maximo) return;

        List<String> plantilla = seccion.getStringList("plantilla");
        aplicarLoreCategoriaAItemEnMano(player, claveCategoria, valor, plantilla);
    }

    private void aplicarLoreCategoriaAItemEnMano(Player player, String claveCategoria, int valor, List<String> plantilla) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(ItemUtils.colorize(plugin.getMsg("sin-item")));
            return;
        }

        List<String> loreProcesado = new ArrayList<>();
        for (String linea : plantilla) {
            loreProcesado.add(ItemUtils.colorize(linea.replace("%valor%", String.valueOf(valor))));
        }

        ItemMeta meta = item.getItemMeta();
        meta.setLore(loreProcesado);
        item.setItemMeta(meta);

        String mensaje = plugin.getMsg("categoria-lore-aplicado")
                .replace("%categoria%", claveCategoria)
                .replace("%valor%", String.valueOf(valor));
        player.sendMessage(ItemUtils.colorize(mensaje));
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
