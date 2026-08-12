package com.day.dayedit.commands;

import com.day.dayedit.DayEdit;
import com.day.dayedit.gui.MenuManager;
import com.day.dayedit.utils.ItemUtils;
import com.day.dayedit.utils.TematicaNombres;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class DayEditCommand implements CommandExecutor, TabCompleter {

    private final DayEdit plugin;

    public DayEditCommand(DayEdit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando solo puede ser usado por jugadores.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            MenuManager.abrirMenuPrincipal(player);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        switch (sub) {
            case "lore":
                handleLore(player, args);
                break;
            case "name":
                handleName(player, args);
                break;
            case "enchant":
                handleEnchant(player, args);
                break;
            case "setlore":
                handleSetLore(player, args);
                break;
            case "settematica":
                handleSetTematica(player, args);
                break;
            case "gui":
                MenuManager.abrirMenuPrincipal(player);
                break;
            case "reload":
                if (!player.hasPermission("dayedit.command.reload")) {
                    player.sendMessage(ItemUtils.colorize(plugin.getMsg("sin-permiso")));
                    return true;
                }
                plugin.reloadConfig();
                player.sendMessage(ItemUtils.colorize(plugin.getMsg("reload-exitoso")));
                break;
            case "help":
                sendHelp(player);
                break;
            default:
                player.sendMessage(ItemUtils.colorize(plugin.getMsg("comando-desconocido")));
                break;
        }

        return true;
    }

    private void handleLore(Player player, String[] args) {
        if (!player.hasPermission("dayedit.command.lore")) {
            player.sendMessage(ItemUtils.colorize(plugin.getMsg("sin-permiso")));
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(ItemUtils.colorize(plugin.getMsg("sin-item")));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ItemUtils.colorize("&cUso: /dayedit lore <add|remove|clear|set> [args]"));
            return;
        }

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        String action = args[1].toLowerCase(Locale.ROOT);

        switch (action) {
            case "add": {
                if (args.length < 3) {
                    player.sendMessage(ItemUtils.colorize("&cUso: /dayedit lore add <texto>"));
                    return;
                }
                String texto = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                lore.add(ItemUtils.colorize(texto));
                meta.setLore(lore);
                item.setItemMeta(meta);
                player.sendMessage(ItemUtils.colorize(plugin.getMsg("lore-agregado")));
                break;
            }
            case "remove": {
                if (lore.isEmpty()) {
                    player.sendMessage(ItemUtils.colorize(plugin.getMsg("lore-linea-invalida")));
                    return;
                }
                lore.remove(lore.size() - 1);
                meta.setLore(lore);
                item.setItemMeta(meta);
                player.sendMessage(ItemUtils.colorize(plugin.getMsg("lore-eliminado")));
                break;
            }
            case "clear": {
                meta.setLore(new ArrayList<>());
                item.setItemMeta(meta);
                player.sendMessage(ItemUtils.colorize(plugin.getMsg("lore-limpiado")));
                break;
            }
            case "set": {
                if (args.length < 4) {
                    player.sendMessage(ItemUtils.colorize("&cUso: /dayedit lore set <linea> <texto>"));
                    return;
                }
                int linea;
                try {
                    linea = Integer.parseInt(args[2]) - 1;
                } catch (NumberFormatException e) {
                    player.sendMessage(ItemUtils.colorize(plugin.getMsg("lore-linea-invalida")));
                    return;
                }
                if (linea < 0 || linea >= lore.size()) {
                    player.sendMessage(ItemUtils.colorize(plugin.getMsg("lore-linea-invalida")));
                    return;
                }
                String texto = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                lore.set(linea, ItemUtils.colorize(texto));
                meta.setLore(lore);
                item.setItemMeta(meta);
                player.sendMessage(ItemUtils.colorize(plugin.getMsg("lore-linea-set").replace("%linea%", String.valueOf(linea + 1))));
                break;
            }
            default:
                player.sendMessage(ItemUtils.colorize("&cUso: /dayedit lore <add|remove|clear|set> [args]"));
                break;
        }
    }

    private void handleName(Player player, String[] args) {
        if (!player.hasPermission("dayedit.command.name")) {
            player.sendMessage(ItemUtils.colorize(plugin.getMsg("sin-permiso")));
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(ItemUtils.colorize(plugin.getMsg("sin-item")));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ItemUtils.colorize("&cUso: /dayedit name <texto>"));
            return;
        }

        String texto = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ItemUtils.colorize(texto));
        item.setItemMeta(meta);

        player.sendMessage(ItemUtils.colorize(plugin.getMsg("nombre-cambiado")));
    }

    private void handleEnchant(Player player, String[] args) {
        if (!player.hasPermission("dayedit.command.enchant")) {
            player.sendMessage(ItemUtils.colorize(plugin.getMsg("sin-permiso")));
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(ItemUtils.colorize(plugin.getMsg("sin-item")));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ItemUtils.colorize("&cUso: /dayedit enchant <add|remove|clear> [args]"));
            return;
        }

        String action = args[1].toLowerCase(Locale.ROOT);

        switch (action) {
            case "add": {
                if (args.length < 3) {
                    player.sendMessage(ItemUtils.colorize("&cUso: /dayedit enchant add <encantamiento> [nivel]"));
                    return;
                }
                Enchantment enchant = buscarEncantamiento(args[2]);
                if (enchant == null) {
                    player.sendMessage(ItemUtils.colorize(plugin.getMsg("encantamiento-invalido").replace("%encant%", args[2])));
                    return;
                }
                int nivel = 1;
                if (args.length >= 4) {
                    try {
                        nivel = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        nivel = 1;
                    }
                }
                ItemMeta meta = item.getItemMeta();
                meta.addEnchant(enchant, nivel, true);
                item.setItemMeta(meta);
                player.sendMessage(ItemUtils.colorize(plugin.getMsg("encantamiento-agregado")
                        .replace("%encant%", args[2])
                        .replace("%nivel%", String.valueOf(nivel))));
                break;
            }
            case "remove": {
                if (args.length < 3) {
                    player.sendMessage(ItemUtils.colorize("&cUso: /dayedit enchant remove <encantamiento>"));
                    return;
                }
                Enchantment enchant = buscarEncantamiento(args[2]);
                if (enchant == null) {
                    player.sendMessage(ItemUtils.colorize(plugin.getMsg("encantamiento-invalido").replace("%encant%", args[2])));
                    return;
                }
                ItemMeta meta = item.getItemMeta();
                if (!meta.hasEnchant(enchant)) {
                    player.sendMessage(ItemUtils.colorize(plugin.getMsg("encantamiento-no-tiene")));
                    return;
                }
                meta.removeEnchant(enchant);
                item.setItemMeta(meta);
                player.sendMessage(ItemUtils.colorize(plugin.getMsg("encantamiento-quitado").replace("%encant%", args[2])));
                break;
            }
            case "clear": {
                ItemMeta meta = item.getItemMeta();
                for (Enchantment enchant : new ArrayList<>(meta.getEnchants().keySet())) {
                    meta.removeEnchant(enchant);
                }
                item.setItemMeta(meta);
                player.sendMessage(ItemUtils.colorize(plugin.getMsg("encantamientos-limpiados")));
                break;
            }
            default:
                player.sendMessage(ItemUtils.colorize("&cUso: /dayedit enchant <add|remove|clear> [args]"));
                break;
        }
    }

    private void handleSetLore(Player player, String[] args) {
        if (!player.hasPermission("dayedit.command.lore")) {
            player.sendMessage(ItemUtils.colorize(plugin.getMsg("sin-permiso")));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(ItemUtils.colorize(plugin.getMsg("uso-setlore")));
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(ItemUtils.colorize(plugin.getMsg("sin-item")));
            return;
        }

        String claveCategoria = args[1].toLowerCase(Locale.ROOT);
        ConfigurationSection seccion = plugin.getConfig().getConfigurationSection("categoria-lore-" + claveCategoria);
        if (seccion == null) {
            player.sendMessage(ItemUtils.colorize(plugin.getMsg("categoria-invalida")));
            return;
        }

        ConfigurationSection campos = seccion.getConfigurationSection("campos");
        List<String> plantilla = seccion.getStringList("plantilla");

        if (campos != null) {
            List<String> orden = new ArrayList<>(campos.getKeys(false));
            String[] valores = Arrays.copyOfRange(args, 2, args.length);

            if (valores.length != orden.size()) {
                player.sendMessage(ItemUtils.colorize("&cEsta categoria necesita " + orden.size()
                        + " valor(es) en este orden: " + String.join(", ", orden)));
                return;
            }

            List<String> loreProcesado = new ArrayList<>();
            for (String linea : plantilla) {
                String procesada = linea;
                for (int i = 0; i < orden.size(); i++) {
                    procesada = procesada.replace("%" + orden.get(i) + "%", valores[i]);
                }
                loreProcesado.add(ItemUtils.colorize(procesada));
            }

            ItemMeta meta = item.getItemMeta();
            meta.setLore(loreProcesado);
            item.setItemMeta(meta);

            player.sendMessage(ItemUtils.colorize(plugin.getMsg("categoria-lore-personalizado-aplicado")
                    .replace("%categoria%", claveCategoria)));
            return;
        }

        int minimo = seccion.getInt("minimo", 1);
        int maximo = seccion.getInt("maximo", 255);
        int valor;
        try {
            valor = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ItemUtils.colorize(plugin.getMsg("valor-fuera-de-rango")));
            return;
        }
        if (valor < minimo || valor > maximo) {
            player.sendMessage(ItemUtils.colorize(plugin.getMsg("valor-fuera-de-rango")));
            return;
        }

        List<String> loreProcesado = new ArrayList<>();
        for (String linea : plantilla) {
            loreProcesado.add(ItemUtils.colorize(linea.replace("%valor%", String.valueOf(valor))));
        }

        ItemMeta meta = item.getItemMeta();
        meta.setLore(loreProcesado);
        item.setItemMeta(meta);

        player.sendMessage(ItemUtils.colorize(plugin.getMsg("categoria-lore-aplicado")
                .replace("%categoria%", claveCategoria)
                .replace("%valor%", String.valueOf(valor))));
    }

    private void handleSetTematica(Player player, String[] args) {
        if (!player.hasPermission("dayedit.command.name")) {
            player.sendMessage(ItemUtils.colorize(plugin.getMsg("sin-permiso")));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(ItemUtils.colorize(plugin.getMsg("uso-settematica")));
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(ItemUtils.colorize(plugin.getMsg("sin-item")));
            return;
        }

        String claveTematica = args[1].toLowerCase(Locale.ROOT);
        String clavePersonaje = args[2].toLowerCase(Locale.ROOT);

        ConfigurationSection seccion = plugin.getConfig().getConfigurationSection("tematica-" + claveTematica);
        if (seccion == null || seccion.getConfigurationSection("personajes." + clavePersonaje) == null) {
            player.sendMessage(ItemUtils.colorize(plugin.getMsg("tematica-invalida")));
            return;
        }

        String nombre = TematicaNombres.getNombre(claveTematica, clavePersonaje, clavePersonaje);

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ItemUtils.colorize(nombre));
        item.setItemMeta(meta);

        player.sendMessage(ItemUtils.colorize(plugin.getMsg("tematica-aplicada").replace("%personaje%", clavePersonaje)));
    }

    private Enchantment buscarEncantamiento(String nombre) {
        String clave = nombre.toLowerCase(Locale.ROOT).replace(" ", "_");
        Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(clave));
        if (enchant != null) return enchant;

        for (Enchantment e : Enchantment.values()) {
            if (e.getKey().getKey().equalsIgnoreCase(clave)) {
                return e;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void sendHelp(Player player) {
        String autor = plugin.getConfig().getString("author", "SoyBughatti");

        player.sendMessage(ItemUtils.colorize("&#00DAFF&lDayEdit &f- Comandos disponibles:"));

        List<?> comandos = plugin.getConfig().getList("ayuda.comandos");
        if (comandos != null) {
            for (Object obj : comandos) {
                if (!(obj instanceof Map)) continue;
                Map<String, Object> entrada = (Map<String, Object>) obj;
                String comando = String.valueOf(entrada.get("comando"));
                String descripcion = String.valueOf(entrada.get("descripcion"));
                player.sendMessage(ItemUtils.colorize("&b" + comando + " &7- " + descripcion));
            }
        }

        player.sendMessage(ItemUtils.colorize("&7Plugin creado por &f" + autor));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> opciones = new ArrayList<>();

        if (args.length == 1) {
            opciones.addAll(Arrays.asList("lore", "name", "enchant", "setlore", "settematica", "gui", "reload", "help"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("lore")) {
                opciones.addAll(Arrays.asList("add", "remove", "clear", "set"));
            } else if (args[0].equalsIgnoreCase("enchant")) {
                opciones.addAll(Arrays.asList("add", "remove", "clear"));
            } else if (args[0].equalsIgnoreCase("setlore")) {
                ConfigurationSection raiz = plugin.getConfig();
                for (String clave : raiz.getKeys(false)) {
                    if (clave.startsWith("categoria-lore-")) {
                        opciones.add(clave.substring("categoria-lore-".length()));
                    }
                }
            } else if (args[0].equalsIgnoreCase("settematica")) {
                ConfigurationSection raiz = plugin.getConfig();
                for (String clave : raiz.getKeys(false)) {
                    if (clave.startsWith("tematica-")) {
                        opciones.add(clave.substring("tematica-".length()));
                    }
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("enchant")
                && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))) {
            opciones.addAll(Arrays.stream(Enchantment.values())
                    .map(e -> e.getKey().getKey())
                    .collect(Collectors.toList()));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("settematica")) {
            ConfigurationSection seccion = plugin.getConfig().getConfigurationSection("tematica-" + args[1].toLowerCase(Locale.ROOT) + ".personajes");
            if (seccion != null) {
                opciones.addAll(seccion.getKeys(false));
            }
        }

        String ultimo = args[args.length - 1].toLowerCase(Locale.ROOT);
        return opciones.stream()
                .filter(o -> o.toLowerCase(Locale.ROOT).startsWith(ultimo))
                .collect(Collectors.toList());
    }
                                                                    }
