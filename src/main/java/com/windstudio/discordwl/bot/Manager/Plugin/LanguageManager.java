package com.windstudio.discordwl.bot.Manager.Plugin;

import com.windstudio.discordwl.Main;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class LanguageManager {
    HashMap<String, String> translate = new HashMap<>();
    HashMap<String, List<String>> translateLoop = new HashMap<>();
    File languageDirectory = new File(Main.plugin.getDataFolder(), "languages/");
    private final Main plugin;

    public LanguageManager(Main plugin) {
        this.plugin = plugin;
        if (!languageDirectory.isDirectory()) {
            languageDirectory.mkdir();
        }
        switch (getString("Language")) {
            case "en_US":
                setLang("en_US");
                break;
            case "ru_RU":
                setLang("ru_RU");
                break;
            case "uk_UA":
                setLang("uk_UA");
                break;
            case "be_BY":
                setLang("be_BY");
                break;
            case "pl_PLh":
                setLang("pl_PLh");
                break;
            case "pl_PLm":
                setLang("pl_PLm");
                break;
            case "cs_CZ":
                setLang("cs_CZ");
                break;
            case "de_DE":
                setLang("de_DE");
                break;
            case "el_GR":
                setLang("el_GR");
                break;
            case "es_ES":
                setLang("es_ES");
                break;
            case "et_EE":
                setLang("et_EE");
                break;
            case "fi_FI":
                setLang("fi_FI");
                break;
            case "fr_FR":
                setLang("fr_FR");
                break;
            case "hu_HU":
                setLang("hu_HU");
                break;
            case "it_IT":
                setLang("it_IT");
                break;
            case "ja_JP":
                setLang("ja_JP");
                break;
            case "ka_GE":
                setLang("ka_GE");
                break;
            case "ko_KR":
                setLang("ko_KR");
                break;
            case "lt_LT":
                setLang("lt_LT");
                break;
            case "lv_LV":
                setLang("lv_LV");
                break;
            case "nl_NL":
                setLang("nl_NL");
                break;
            case "pt_PT":
                setLang("pt_PT");
                break;
            case "ro_RO":
                setLang("ro_RO");
                break;
            case "sv_SE":
                setLang("sv_SE");
                break;
            case "tr_TR":
                setLang("tr_TR");
                break;
            case "zn_CN":
                setLang("zn_CN");
                break;
            default:
                setLang("en_US");
            break;
        }
    }
    public String get(String path){
        return translate.get(path);
    }
    public List<String> getStringList(String path){
        return translateLoop.get(path);
    }
    public void setLang(String lang) {
        File LanguageFile = new File(plugin.getDataFolder(), "languages/" + lang+".yml");
        InputStream stream = plugin.getResource(lang+".yml");
        if (!LanguageFile.exists()) {
            try {
                FileUtils.copyInputStreamToFile(stream, LanguageFile);
                LanguageFile.createNewFile();
            } catch (IOException e) {
                plugin.console.sendMessage(ColorManager.translate("&c > Language file '" + lang+".yml' cannot created. Plugin was disabled"));
                Bukkit.getServer().getPluginManager().disablePlugin(Main.getPlugin());
            }
        }
        FileConfiguration translationL = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "languages/" + lang+".yml"));
        for (String translation : translationL.getKeys(false)) {
            translate.put(translation, translationL.getString(translation));
            translateLoop.put(translation, translationL.getStringList(translation));
        }
    }
    public void updateLang(String lang) {
        File LanguageFile = new File(plugin.getDataFolder(), "languages/" + lang+".yml");
        if (LanguageFile.exists()) {
            try {
                ConfigUpdater.update(plugin, lang+".yml", LanguageFile, Arrays.asList());
            } catch (IOException e) {
                plugin.console.sendMessage(ColorManager.translate("&c > Language file '"+lang+".yml' cannot updated."));
            }
        }
    }
    public String getString(String path) { return plugin.getConfig().getString(path); }
    public File FileUpdate() {
        switch (getString("Language")) {
            case "en_US":
                updateLang("en_US");
                break;
            case "ru_RU":
                updateLang("ru_RU");
                break;
            case "uk_UA":
                updateLang("uk_UA");
                break;
            case "be_BY":
                updateLang("be_BY");
                break;
            case "pl_PLh":
                updateLang("pl_PLh");
                break;
            case "pl_PLm":
                updateLang("pl_PLm");
                break;
            case "cs_CZ":
                updateLang("cs_CZ");
                break;
            case "de_DE":
                updateLang("de_DE");
                break;
            case "el_GR":
                updateLang("el_GR");
                break;
            case "es_ES":
                updateLang("es_ES");
                break;
            case "et_EE":
                updateLang("et_EE");
                break;
            case "fi_FI":
                updateLang("fi_FI");
                break;
            case "fr_FR":
                updateLang("fr_FR");
                break;
            case "hu_HU":
                updateLang("hu_HU");
                break;
            case "it_IT":
                updateLang("it_IT");
                break;
            case "ja_JP":
                updateLang("ja_JP");
                break;
            case "ka_GE":
                updateLang("ka_GE");
                break;
            case "ko_KR":
                updateLang("ko_KR");
                break;
            case "lt_LT":
                updateLang("lt_LT");
                break;
            case "lv_LV":
                updateLang("lv_LV");
                break;
            case "nl_NL":
                updateLang("nl_NL");
                break;
            case "pt_PT":
                updateLang("pt_PT");
                break;
            case "ro_RO":
                updateLang("ro_RO");
                break;
            case "sv_SE":
                updateLang("sv_SE");
                break;
            case "tr_TR":
                updateLang("tr_TR");
                break;
            case "zn_CN":
                updateLang("zn_CN");
                break;
            default:
                updateLang("en_US");
                break;
        }
        return null;
    }
}