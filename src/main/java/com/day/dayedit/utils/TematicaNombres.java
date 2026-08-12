package com.day.dayedit.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Nombres (con degradado hex) de cada personaje de cada tematica.
 * Esto vive en el codigo (se edita subiendo un commit a GitHub) y NO
 * en config.yml, a pedido: los hex de las cabezas se editan aqui.
 *
 * Para agregar una tematica nueva: agregar un nuevo Map<String,String>
 * y registrarlo en el bloque static con NOMBRES.put("clave-tematica", mapa).
 * La clave debe coincidir con la clave usada en config.yml
 * (menu-tematicas.lista.<clave> y tematica-<clave>.personajes.<clave>).
 */
public class TematicaNombres {

    private static final Map<String, Map<String, String>> NOMBRES = new HashMap<>();

    static {
        Map<String, String> brawlstars = new HashMap<>();
        brawlstars.put("shelly", "&x&F&F&8&C&2&C&lS&x&F&F&9&2&3&8&lH&x&F&F&9&8&4&3&lE&x&F&F&9&F&4&F&lL&x&F&F&A&5&5&A&lL&x&F&F&A&B&6&6&lY");
        brawlstars.put("colt", "&x&F&F&D&9&3&D&lC&x&F&F&B&F&3&1&lO&x&F&F&A&6&2&6&lL&x&F&F&8&C&1&A&lT");
        brawlstars.put("bull", "&x&8&B&4&5&1&3&lB&x&A&3&5&1&1&7&lU&x&B&A&5&D&1&A&lL&x&D&2&6&9&1&E&lL");
        brawlstars.put("el_primo", "&x&F&F&D&7&0&0&lE&x&F&2&B&9&0&6&lL&x&E&5&9&B&0&B&lP&x&D&8&7&C&1&1&lR&x&C&C&5&E&1&7&lI&x&B&F&4&0&1&C&lM&x&B&2&2&2&2&2&lO");
        brawlstars.put("poco", "&x&2&E&C&C&7&1&lP&x&2&5&A&6&5&C&lO&x&1&D&8&0&4&7&lC&x&1&4&5&A&3&2&lO");
        brawlstars.put("rico", "&x&0&0&E&5&F&F&lR&x&0&0&B&B&E&E&lI&x&0&0&9&0&D&D&lC&x&0&0&6&6&C&C&lO");
        brawlstars.put("spike", "&x&7&C&F&C&0&0&lS&x&6&4&D&C&0&7&lP&x&4&C&B&B&0&E&lI&x&3&3&9&A&1&4&lK&x&1&B&7&A&1&B&lE");
        brawlstars.put("crow", "&x&9&B&3&0&F&F&lC&x&8&0&2&0&D&5&lR&x&6&6&1&0&A&C&lO&x&4&B&0&0&8&2&lW");
        brawlstars.put("leon", "&x&3&9&F&F&1&4&lL&x&2&F&C&C&1&6&lE&x&2&4&9&9&1&8&lO&x&1&A&6&6&1&A&lN");
        brawlstars.put("mortis", "&x&8&A&2&B&E&2&lM&x&7&8&2&4&C&6&lO&x&6&5&1&D&A&9&lR&x&5&3&1&6&8&D&lT&x&4&0&0&F&7&0&lI&x&2&E&0&8&5&4&lS");
        NOMBRES.put("brawlstars", brawlstars);
    }

    /**
     * Devuelve el nombre hex del personaje. Si la tematica o el personaje
     * no estan registrados aqui, devuelve el "fallback" que se le pase
     * (normalmente lo que tenga config.yml, o la clave tal cual).
     */
    public static String getNombre(String tematica, String personaje, String fallback) {
        Map<String, String> mapa = NOMBRES.get(tematica);
        if (mapa == null) return fallback;
        return mapa.getOrDefault(personaje, fallback);
    }
          }
