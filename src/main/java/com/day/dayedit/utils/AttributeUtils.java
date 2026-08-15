package com.day.dayedit.utils;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AttributeUtils {

    public static Attribute buscarAtributo(String nombre) {
        String clave = nombre.trim().toUpperCase(Locale.ROOT).replace(" ", "_");
        try {
            return Attribute.valueOf(clave);
        } catch (IllegalArgumentException ignored) {
        }
        if (!clave.startsWith("GENERIC_")) {
            try {
                return Attribute.valueOf("GENERIC_" + clave);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    public static AttributeModifier.Operation buscarOperacion(String nombre) {
        String clave = nombre.trim().toLowerCase(Locale.ROOT);
        switch (clave) {
            case "add":
            case "add_number":
            case "sumar":
                return AttributeModifier.Operation.ADD_NUMBER;
            case "add_scalar":
            case "multiply_base":
                return AttributeModifier.Operation.ADD_SCALAR;
            case "multiply":
            case "multiply_total":
            case "multiply_scalar_1":
                return AttributeModifier.Operation.MULTIPLY_SCALAR_1;
            default:
                return null;
        }
    }

    public static EquipmentSlot buscarSlot(String nombre) {
        String clave = nombre.trim().toLowerCase(Locale.ROOT);
        switch (clave) {
            case "mainhand":
            case "hand":
            case "mano":
                return EquipmentSlot.HAND;
            case "offhand":
            case "off_hand":
                return EquipmentSlot.OFF_HAND;
            case "feet":
            case "pies":
            case "botas":
                return EquipmentSlot.FEET;
            case "legs":
            case "piernas":
            case "pantalones":
                return EquipmentSlot.LEGS;
            case "chest":
            case "pecho":
            case "torso":
                return EquipmentSlot.CHEST;
            case "head":
            case "cabeza":
                return EquipmentSlot.HEAD;
            default:
                return null;
        }
    }

    private static UUID uuidDeterminista(Attribute attribute, EquipmentSlot slot) {
        String clave = "dayedit:" + attribute.name() + ":" + slot.name();
        return UUID.nameUUIDFromBytes(clave.getBytes(StandardCharsets.UTF_8));
    }

    public static void aplicarModificador(ItemStack item, Attribute attribute, EquipmentSlot slot,
                                           AttributeModifier.Operation operacion, double cantidad) {
        ItemMeta meta = item.getItemMeta();
        UUID uuid = uuidDeterminista(attribute, slot);

        if (meta.hasAttributeModifiers()) {
            Collection<AttributeModifier> existentes = meta.getAttributeModifiers(attribute);
            if (existentes != null) {
                for (AttributeModifier mod : new ArrayList<>(existentes)) {
                    if (mod.getUniqueId().equals(uuid)) {
                        meta.removeAttributeModifier(attribute, mod);
                    }
                }
            }
        }

        AttributeModifier nuevo = new AttributeModifier(uuid, "dayedit", cantidad, operacion, slot);
        meta.addAttributeModifier(attribute, nuevo);
        item.setItemMeta(meta);
    }

    public static boolean quitarModificador(ItemStack item, Attribute attribute, EquipmentSlot slot) {
        ItemMeta meta = item.getItemMeta();
        UUID uuid = uuidDeterminista(attribute, slot);

        boolean encontrado = false;
        if (meta.hasAttributeModifiers()) {
            Collection<AttributeModifier> existentes = meta.getAttributeModifiers(attribute);
            if (existentes != null) {
                for (AttributeModifier mod : new ArrayList<>(existentes)) {
                    if (mod.getUniqueId().equals(uuid)) {
                        meta.removeAttributeModifier(attribute, mod);
                        encontrado = true;
                    }
                }
            }
        }

        item.setItemMeta(meta);
        return encontrado;
    }

    public static void limpiarModificadores(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.setAttributeModifiers(null);
        item.setItemMeta(meta);
    }

    public static List<String> listarModificadores(ItemStack item) {
        List<String> lineas = new ArrayList<>();
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasAttributeModifiers()) return lineas;

        for (Map.Entry<Attribute, AttributeModifier> entry : meta.getAttributeModifiers().entries()) {
            Attribute attribute = entry.getKey();
            AttributeModifier modifier = entry.getValue();
            lineas.add("&b" + attribute.name() + " &7(" + modifier.getSlot() + "): &f"
                    + modifier.getAmount() + " &7[" + modifier.getOperation() + "]");
        }
        return lineas;
    }
          }
