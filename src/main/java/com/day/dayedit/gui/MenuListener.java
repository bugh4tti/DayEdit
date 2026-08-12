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

        if (menuActual.startsWith("campo-lore-")) {
            String claveCategoria = menuActual.substring("campo-lore-".length());
            ConfigurationSection seccionCategoria = plugin.getConfig().getConfigurationSection("categoria-lore-" + claveCategoria);
            if (seccionCategoria == null) return;

            String tituloBase = ItemUtils.colorize(seccionCategoria.getString("titulo", ""));
            if (!event.getView().getTitle().startsWith(tituloBase)) return;

            event.setCancelled(true);

            if (event.getClickedInventory() == null
                    || !event.getClickedInventory().equals(event.getView().getTopInventory())) {
                return;
            }

            ItemStack clickedCampo = event.getCurrentItem();
            if (clickedCampo == null || clickedCampo.getType().isAir()) return;

            manejarCampoLore(player, claveCategoria, seccionCategoria, event.getSlot());
            return;
        }

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

            ConfigurationSection categoriaLoreSeccion = plugin.getConfig().getConfigurationSection("categoria-lore-" + clave);
            boolean tieneCampos = categoriaLoreSeccion != null && categoriaLoreSeccion.getConfigurationSection("campos") != null;

            if (tieneCampos) {
                MenuManager.abrirCategoriaLoreCampos(player, clave);
            } else {
                MenuManager.abrirCategoriaLore(player, clave, 0);
            }
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

            String nombre = com.day.dayedit.utils.TematicaNombres.getNombre(claveTematica, clave, clave);
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

        int indiceEnGrid = MenuManager.indiceEnGrid(slot);
        if (indiceEnGrid == -1) return;

        int minimo = seccion.getInt("minimo", 1);
        int maximo = seccion.getInt("maximo", 255);
        int valor = minimo + (pagina * MenuManager.SLOTS_GRID_54.length) + indiceEnGrid;
        if (valor > maximo) return;

        List<String> plantilla = seccion.getStringList("plantilla");
        aplicarLoreCategoriaAItemEnMano(player, claveCategoria, valor, plantilla);
    }

    private void manejarCampoLore(Player player, String claveCategoria, ConfigurationSection seccionCategoria, int slot) {
        int anteriorSlot = seccionCategoria.getInt("anterior-slot", -1);
        int volverSlot = seccionCategoria.getInt("volver-slot", -1);
        int siguienteSlot = seccionCategoria.getInt("siguiente-slot", -1);
        int pagina = MenuManager.getPaginaActual(player);
        int indiceCampo = MenuManager.getIndiceCampoActual(player);

        List<String> orden = MenuManager.getOrdenCampos(player);
        if (orden == null || indiceCampo >= orden.size()) return;

        if (slot == volverSlot) {
            if (indiceCampo > 0) {
                MenuManager.abrirCampoLore(player, claveCategoria, indiceCampo - 1, 0);
            } else {
                MenuManager.limpiarEstadoCampos(player);
                MenuManager.abrirMenuLores(player);
            }
            return;
        }
        if (slot == anteriorSlot) {
            if (pagina > 0) {
                MenuManager.abrirCampoLore(player, claveCategoria, indiceCampo, pagina - 1);
            }
            return;
        }
        if (slot == siguienteSlot) {
            MenuManager.abrirCampoLore(player, claveCategoria, indiceCampo, pagina + 1);
            return;
        }

        int indiceEnGrid = MenuManager.indiceEnGrid(slot);
        if (indiceEnGrid == -1) return;

        String campoKey = orden.get(indiceCampo);
        ConfigurationSection campos = seccionCategoria.getConfigurationSection("campos");
        if (campos == null) return;
        ConfigurationSection campo = campos.getConfigurationSection(campoKey);
        if (campo == null) return;

        double minimo = campo.getDouble("minimo", 0);
        double maximo = campo.getDouble("maximo", 100);
        double paso = campo.getDouble("paso", 1);
        int decimales = campo.getInt("decimales", 0);

        int indiceValor = (pagina * MenuManager.SLOTS_GRID_54.length) + indiceEnGrid;
        int totalValores = MenuManager.totalValores(minimo, maximo, paso);
        if (indiceValor >= totalValores) return;

        double valor = MenuManager.calcularValor(minimo, paso, indiceValor);
        String valorTexto = MenuManager.formatearValor(valor, decimales);

        MenuManager.guardarSeleccionCampo(player, campoKey, valorTexto);

        if (indiceCampo + 1 < orden.size()) {
            MenuManager.abrirCampoLore(player, claveCategoria, indiceCampo + 1, 0);
        } else {
            finalizarCategoriaConCampos(player, claveCategoria, seccionCategoria);
        }
    }

    private void finalizarCategoriaConCampos(Player player, String claveCategoria, ConfigurationSection seccionCategoria) {
        Map<String, String> seleccion = MenuManager.getSeleccionCampos(player);
        MenuManager.limpiarEstadoCampos(player);

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(ItemUtils.colorize(plugin.getMsg("sin-item")));
            MenuManager.abrirMenuLores(player);
            return;
        }

        List<String> plantilla = seccionCategoria.getStringList("plantilla");
        List<String> loreProcesado = new ArrayList<>();
        for (String linea : plantilla) {
            String procesada = linea;
            for (Map.Entry<String, String> entry : seleccion.entrySet()) {
                procesada = procesada.replace("%" + entry.getKey() + "%", entry.getValue());
            }
            loreProcesado.add(ItemUtils.colorize(procesada));
        }

        ItemMeta meta = item.getItemMeta();
        meta.setLore(loreProcesado);
        item.setItemMeta(meta);

        String mensaje = plugin.getMsg("categoria-lore-personalizado-aplicado")
                .replace("%categoria%", claveCategoria);
        player.sendMessage(ItemUtils.colorize(mensaje));

        MenuManager.abrirMenuLores(player);
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
