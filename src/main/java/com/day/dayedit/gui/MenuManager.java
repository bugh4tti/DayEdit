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
    private static final Map<UUID, Integer> paginaActual = new HashMap<>();
    private static final Map<UUID, String> categoriaLoreActual = new HashMap<>();

    private static final Material MATERIAL_VOLVER = Material.ARROW;
    private static final Material MATERIAL_ANTERIOR = Material.SPECTRAL_ARROW;
    private static final Material MATERIAL_SIGUIENTE = Material.SPECTRAL_ARROW;

    public static final int[] SLOTS_GRID_54 = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public static String getMenuAbierto(Player player) {
        return menuAbierto.get(player.getUniqueId());
    }

    public static int getPaginaActual(Player player) {
        return paginaActual.getOrDefault(player.getUniqueId(), 0);
    }

    public static String getCategoriaLoreActual(Player player) {
        return categoriaLoreActual.get(player.getUniqueId());
    }

    public static void limpiarEstado(Player player) {
        menuAbierto.remove(player.getUniqueId());
        paginaActual.remove(player.getUniqueId());
        categoriaLoreActual.remove(player.getUniqueId());
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

        ConfigurationSection categorias = seccion.getConfigurationSection("categorias");
        if (categorias != null) {
            for (String clave : categorias.getKeys(false)) {
                ConfigurationSection categoria = categorias.getConfigurationSection(clave);
                if (categoria == null) continue;

                int slot = categoria.getInt("slot", 0);
                Material material = materialSeguro(categoria.getString("material", "PAPER"));
                String nombre = categoria.getString("nombre", clave);
                List<String> lore = categoria.getStringList("lore");

                inv.setItem(slot, crearItem(material, nombre, lore));
            }
        }

        int volverSlot = seccion.getInt("volver-slot", tamano - 5);
        inv.setItem(volverSlot, crearBotonVolver());

        menuAbierto.put(player.getUniqueId(), "menu-lores");
        player.openInventory(inv);
    }

    public static void abrirCategoriaLore(Player player, String claveCategoria, int pagina) {
        DayEdit plugin = DayEdit.getInstance();
        ConfigurationSection seccion = plugin.getConfig().getConfigurationSection("categoria-lore-" + claveCategoria);
        if (seccion == null) {
            player.sendMessage(ItemUtils.colorize("&cEsa categoria de lore no existe en la config."));
            return;
        }

        String titulo = ItemUtils.colorize(seccion.getString("titulo", "&#00DAFF&lDayEdit"));
        int tamano = seccion.getInt("tamano", 54);
        Material materialRelleno = materialSeguro(seccion.getString("material-relleno", "GRAY_STAINED_GLASS_PANE"));
        Material materialValor = materialSeguro(seccion.getString("material-valor", "PAPER"));
        int minimo = seccion.getInt("minimo", 1);
        int maximo = seccion.getInt("maximo", 255);
        List<String> plantilla = seccion.getStringList("plantilla");

        int anteriorSlot = seccion.getInt("anterior-slot", -1);
        int volverSlot = seccion.getInt("volver-slot", -1);
        int siguienteSlot = seccion.getInt("siguiente-slot", -1);

        Inventory inv = Bukkit.createInventory(null, tamano, titulo);

        rellenarBorde(inv, tamano, materialRelleno, anteriorSlot, volverSlot, siguienteSlot);

        int itemsPorPagina = SLOTS_GRID_54.length;
        int inicio = minimo + (pagina * itemsPorPagina);

        for (int i = 0; i < SLOTS_GRID_54.length; i++) {
            int valor = inicio + i;
            if (valor > maximo) break;

            List<String> loreProcesado = new ArrayList<>();
            for (String linea : plantilla) {
                loreProcesado.add(linea.replace("%valor%", String.valueOf(valor)));
            }

            ItemStack item = crearItem(materialValor, "&b&l" + valor, loreProcesado);
            inv.setItem(SLOTS_GRID_54[i], item);
        }

        boolean hayAnterior = pagina > 0;
        boolean haySiguiente = (inicio + itemsPorPagina) <= maximo;

        if (anteriorSlot >= 0 && hayAnterior) {
            inv.setItem(anteriorSlot, crearItem(MATERIAL_ANTERIOR, "&e&l« Pagina anterior", new ArrayList<>()));
        }
        if (volverSlot >= 0) {
            inv.setItem(volverSlot, crearBotonVolver());
        }
        if (siguienteSlot >= 0 && haySiguiente) {
            inv.setItem(siguienteSlot, crearItem(MATERIAL_SIGUIENTE, "&e&lPagina siguiente »", new ArrayList<>()));
        }

        menuAbierto.put(player.getUniqueId(), "categoria-lore-" + claveCategoria);
        paginaActual.put(player.getUniqueId(), pagina);
        categoriaLoreActual.put(player.getUniqueId(), claveCategoria);
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

    private static void rellenarBorde(Inventory inv, int tamano, Material relleno, int... slotsReservados) {
        int filas = tamano / 9;
        ItemStack panel = crearItem(relleno, " ", new ArrayList<>());

        for (int slot = 0; slot < tamano; slot++) {
            int fila = slot / 9;
            int columna = slot % 9;

            boolean esBorde = fila == 0 || fila == filas - 1 || columna == 0 || columna == 8;
            if (!esBorde) continue;

            if (esSlotReservado(slot, slotsReservados)) continue;

            inv.setItem(slot, panel);
        }
    }

    private static boolean esSlotReservado(int slot, int[] slotsReservados) {
        for (int reservado : slotsReservados) {
            if (reservado == slot) return true;
        }
        return false;
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
