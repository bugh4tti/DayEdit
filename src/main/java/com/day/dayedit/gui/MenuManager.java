package com.day.dayedit.gui;

import com.day.dayedit.DayEdit;
import com.day.dayedit.utils.HeadUtils;
import com.day.dayedit.utils.ItemUtils;
import com.day.dayedit.utils.TematicaNombres;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MenuManager {

    private static final Map<UUID, String> menuAbierto = new HashMap<>();
    private static final Map<UUID, Integer> paginaActual = new HashMap<>();

    private static final Map<UUID, List<String>> ordenCamposJugador = new HashMap<>();
    private static final Map<UUID, Integer> indiceCampoJugador = new HashMap<>();
    private static final Map<UUID, Map<String, String>> seleccionCamposJugador = new HashMap<>();

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

    public static List<String> getOrdenCampos(Player player) {
        return ordenCamposJugador.get(player.getUniqueId());
    }

    public static int getIndiceCampoActual(Player player) {
        return indiceCampoJugador.getOrDefault(player.getUniqueId(), 0);
    }

    public static Map<String, String> getSeleccionCampos(Player player) {
        return seleccionCamposJugador.getOrDefault(player.getUniqueId(), new LinkedHashMap<>());
    }

    public static void guardarSeleccionCampo(Player player, String campo, String valor) {
        seleccionCamposJugador.computeIfAbsent(player.getUniqueId(), k -> new LinkedHashMap<>()).put(campo, valor);
    }

    public static void limpiarEstadoCampos(Player player) {
        ordenCamposJugador.remove(player.getUniqueId());
        indiceCampoJugador.remove(player.getUniqueId());
        seleccionCamposJugador.remove(player.getUniqueId());
    }

    public static void limpiarEstado(Player player) {
        menuAbierto.remove(player.getUniqueId());
        paginaActual.remove(player.getUniqueId());
        limpiarEstadoCampos(player);
    }

    public static int indiceEnGrid(int slot) {
        for (int i = 0; i < SLOTS_GRID_54.length; i++) {
            if (SLOTS_GRID_54[i] == slot) return i;
        }
        return -1;
    }

    public static double calcularValor(double minimo, double paso, int indice) {
        BigDecimal bMin = BigDecimal.valueOf(minimo);
        BigDecimal bPaso = BigDecimal.valueOf(paso);
        return bMin.add(bPaso.multiply(BigDecimal.valueOf(indice))).doubleValue();
    }

    public static String formatearValor(double valor, int decimales) {
        return String.format(Locale.ROOT, "%." + decimales + "f", valor);
    }

    public static int totalValores(double minimo, double maximo, double paso) {
        return (int) Math.round((maximo - minimo) / paso) + 1;
    }

    public static void abrirMenuPrincipal(Player player) {
        DayEdit plugin = DayEdit.getInstance();
        ConfigurationSection seccion = plugin.getConfig().getConfigurationSection("menu-principal");
        if (seccion == null) return;

        String titulo = ItemUtils.colorize(seccion.getString("titulo", "&#00DAFF&lDayEdit"));
        int tamano = seccion.getInt("tamano", 54);
        Material materialRelleno = materialSeguro(seccion.getString("material-relleno", "GRAY_STAINED_GLASS_PANE"));
        Inventory inv = Bukkit.createInventory(null, tamano, titulo);

        rellenarBorde(inv, tamano, materialRelleno);

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
        int tamano = seccion.getInt("tamano", 54);
        Material materialRelleno = materialSeguro(seccion.getString("material-relleno", "GRAY_STAINED_GLASS_PANE"));
        int volverSlot = seccion.getInt("volver-slot", -1);
        Inventory inv = Bukkit.createInventory(null, tamano, titulo);

        rellenarBorde(inv, tamano, materialRelleno, volverSlot);

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

        if (volverSlot >= 0) {
            inv.setItem(volverSlot, crearBotonVolver());
        }

        menuAbierto.put(player.getUniqueId(), "menu-lores");
        limpiarEstadoCampos(player);
        player.openInventory(inv);
    }

    public static void abrirCategoriaLore(Player player, String claveCategoria, int pagina) {
        DayEdit plugin = DayEdit.getInstance();
        ConfigurationSection seccion = plugin.getConfig().getConfigurationSection("categoria-lore-" + claveCategoria);
        if (seccion == null) {
            player.sendMessage(ItemUtils.colorize(plugin.getMsg("categoria-invalida")));
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
        player.openInventory(inv);
    }

    public static void abrirCategoriaLoreCampos(Player player, String claveCategoria) {
        DayEdit plugin = DayEdit.getInstance();
        ConfigurationSection seccion = plugin.getConfig().getConfigurationSection("categoria-lore-" + claveCategoria);
        if (seccion == null) {
            player.sendMessage(ItemUtils.colorize(plugin.getMsg("categoria-invalida")));
            return;
        }
        ConfigurationSection campos = seccion.getConfigurationSection("campos");
        if (campos == null) {
            player.sendMessage(ItemUtils.colorize(plugin.getMsg("categoria-invalida")));
            return;
        }

        List<String> orden = new ArrayList<>(campos.getKeys(false));
        ordenCamposJugador.put(player.getUniqueId(), orden);
        indiceCampoJugador.put(player.getUniqueId(), 0);
        seleccionCamposJugador.put(player.getUniqueId(), new LinkedHashMap<>());

        abrirCampoLore(player, claveCategoria, 0, 0);
    }

    public static void abrirCampoLore(Player player, String claveCategoria, int indiceCampo, int pagina) {
        DayEdit plugin = DayEdit.getInstance();
        ConfigurationSection seccion = plugin.getConfig().getConfigurationSection("categoria-lore-" + claveCategoria);
        if (seccion == null) return;

        List<String> orden = ordenCamposJugador.get(player.getUniqueId());
        if (orden == null || indiceCampo < 0 || indiceCampo >= orden.size()) return;
        String campoKey = orden.get(indiceCampo);

        ConfigurationSection campos = seccion.getConfigurationSection("campos");
        if (campos == null) return;
        ConfigurationSection campo = campos.getConfigurationSection(campoKey);
        if (campo == null) return;

        String tituloBase = ItemUtils.colorize(seccion.getString("titulo", "&#00DAFF&lDayEdit"));
        String nombreCampo = ItemUtils.colorize(campo.getString("nombre", campoKey));
        String titulo = tituloBase + " &7- " + nombreCampo;

        int tamano = seccion.getInt("tamano", 54);
        Material materialRelleno = materialSeguro(seccion.getString("material-relleno", "GRAY_STAINED_GLASS_PANE"));
        Material materialValor = materialSeguro(seccion.getString("material-valor", "PAPER"));

        double minimo = campo.getDouble("minimo", 0);
        double maximo = campo.getDouble("maximo", 100);
        double paso = campo.getDouble("paso", 1);
        int decimales = campo.getInt("decimales", 0);

        int anteriorSlot = seccion.getInt("anterior-slot", -1);
        int volverSlot = seccion.getInt("volver-slot", -1);
        int siguienteSlot = seccion.getInt("siguiente-slot", -1);

        Inventory inv = Bukkit.createInventory(null, tamano, titulo);
        rellenarBorde(inv, tamano, materialRelleno, anteriorSlot, volverSlot, siguienteSlot);

        int totalValores = totalValores(minimo, maximo, paso);
        int itemsPorPagina = SLOTS_GRID_54.length;
        int inicioIndice = pagina * itemsPorPagina;

        for (int i = 0; i < SLOTS_GRID_54.length; i++) {
            int indiceValor = inicioIndice + i;
            if (indiceValor >= totalValores) break;

            double valor = calcularValor(minimo, paso, indiceValor);
            String valorTexto = formatearValor(valor, decimales);

            List<String> loreItem = new ArrayList<>();
            loreItem.add("&7Click para elegir " + nombreCampo + " &7= &f" + valorTexto);

            ItemStack item = crearItem(materialValor, "&b&l" + valorTexto, loreItem);
            inv.setItem(SLOTS_GRID_54[i], item);
        }

        boolean hayAnterior = pagina > 0;
        boolean haySiguiente = (inicioIndice + itemsPorPagina) < totalValores;

        if (anteriorSlot >= 0 && hayAnterior) {
            inv.setItem(anteriorSlot, crearItem(MATERIAL_ANTERIOR, "&e&l« Pagina anterior", new ArrayList<>()));
        }
        if (volverSlot >= 0) {
            inv.setItem(volverSlot, crearBotonVolver());
        }
        if (siguienteSlot >= 0 && haySiguiente) {
            inv.setItem(siguienteSlot, crearItem(MATERIAL_SIGUIENTE, "&e&lPagina siguiente »", new ArrayList<>()));
        }

        menuAbierto.put(player.getUniqueId(), "campo-lore-" + claveCategoria);
        paginaActual.put(player.getUniqueId(), pagina);
        indiceCampoJugador.put(player.getUniqueId(), indiceCampo);
        player.openInventory(inv);
    }

    public static void abrirMenuTematicas(Player player) {
        DayEdit plugin = DayEdit.getInstance();
        ConfigurationSection seccion = plugin.getConfig().getConfigurationSection("menu-tematicas");
        if (seccion == null) return;

        String titulo = ItemUtils.colorize(seccion.getString("titulo", "&#00DAFF&lDayEdit &f- Tematicas"));
        int tamano = seccion.getInt("tamano", 54);
        Material materialRelleno = materialSeguro(seccion.getString("material-relleno", "GRAY_STAINED_GLASS_PANE"));
        int volverSlot = seccion.getInt("volver-slot", -1);
        Inventory inv = Bukkit.createInventory(null, tamano, titulo);

        rellenarBorde(inv, tamano, materialRelleno, volverSlot);

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

        if (volverSlot >= 0) {
            inv.setItem(volverSlot, crearBotonVolver());
        }

        menuAbierto.put(player.getUniqueId(), "menu-tematicas");
        player.openInventory(inv);
    }

    public static void abrirSubmenuTematica(Player player, String claveTematica) {
        DayEdit plugin = DayEdit.getInstance();
        ConfigurationSection seccion = plugin.getConfig().getConfigurationSection("tematica-" + claveTematica);
        if (seccion == null) {
            player.sendMessage(ItemUtils.colorize(plugin.getMsg("tematica-invalida")));
            return;
        }

        String titulo = ItemUtils.colorize(seccion.getString("titulo", "&#00DAFF&lDayEdit"));
        int tamano = seccion.getInt("tamano", 54);
        Material materialRelleno = materialSeguro(seccion.getString("material-relleno", "GRAY_STAINED_GLASS_PANE"));
        int volverSlot = seccion.getInt("volver-slot", -1);
        Inventory inv = Bukkit.createInventory(null, tamano, titulo);

        rellenarBorde(inv, tamano, materialRelleno, volverSlot);

        ConfigurationSection personajes = seccion.getConfigurationSection("personajes");
        if (personajes != null) {
            for (String clave : personajes.getKeys(false)) {
                ConfigurationSection personaje = personajes.getConfigurationSection(clave);
                if (personaje == null) continue;

                int slot = personaje.getInt("slot", 0);
                Material material = materialSeguro(personaje.getString("material", "PLAYER_HEAD"));
                String nombre = TematicaNombres.getNombre(claveTematica, clave, clave);

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

        if (volverSlot >= 0) {
            inv.setItem(volverSlot, crearBotonVolver());
        }

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
