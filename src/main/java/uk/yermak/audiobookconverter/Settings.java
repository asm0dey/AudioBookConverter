package uk.yermak.audiobookconverter;

import com.google.gson.*;
import uk.yermak.audiobookconverter.formats.Format;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;

public class Settings {

    private static final Preferences preferences = Preferences.userNodeForPackage(AudiobookConverter.class);
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Format.class, (JsonDeserializer<Format>) (jsonElement, type, context) -> {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                JsonElement extension = jsonObject.get("extension");
                String formatType = extension.getAsString();
                return new FormatInstanceCreator(formatType).createInstance(type);
            }).create();

    private boolean darkMode = false;
    private boolean showHints = true;
    private int lastUsedPreset = 0;
    private List<Preset> presets = new ArrayList<>();
    private Set<String> genres = new TreeSet<>();
    private String chapterFormat = "<if(BOOK_NUMBER)><BOOK_NUMBER>. <endif>" +
            "<if(BOOK_TITLE)><BOOK_TITLE>. <endif>" +
            "<if(CHAPTER_TEXT)><CHAPTER_TEXT> <endif>" +
            "<if(CHAPTER_NUMBER)><CHAPTER_NUMBER; format=\"%,03d\"> <endif>" +
            "<if(TAG)><TAG> <endif>" +
            "<if(CUSTOM_TITLE)><CUSTOM_TITLE> <endif>" +
            "<if(DURATION)> - <DURATION; format=\"%02d:%02d:%02d\"><endif>";
    private String filenameFormat = "<WRITER><if(SERIES)> - [<SERIES><if(BOOK_NUMBER)> - <BOOK_NUMBER; format=\"%,02d\"><endif>]<endif> - <TITLE><if(NARRATOR)> (<NARRATOR>)<endif>";
    private String partFormat = "<if(WRITER)><WRITER> <endif>" +
            "<if(SERIES)>- [<SERIES><if(BOOK_NUMBER)> -<BOOK_NUMBER><endif>] - <endif>" +
            "<if(TITLE)><TITLE><endif>" +
            "<if(NARRATOR)> (<NARRATOR>)<endif>" +
            "<if(YEAR)>-<YEAR><endif>" +
            "<if(PART)>, Part <PART; format=\"%,03d\"><endif>";
    private String chapterContext = "CHAPTER_NUMBER:CHAPTER_TEXT:DURATION";
    private String chapterCustomTitle = "";
    private String outputFolder = System.getProperty("user.home");
    private String sourceFolder = System.getProperty("user.home");

    public static void saveSetting(Settings settings) {
        preferences.put(Version.getSettingsVersion(), gson.toJson(settings));
    }

    public static void clear() {
        preferences.remove(Version.getSettingsVersion());
    }

    public void save() {
        saveSetting(this);
    }

    public static Settings loadSetting() {
        String settingsJson = preferences.get(Version.getSettingsVersion(), null);
        if (settingsJson == null) {
            Settings settings = new Settings();
            settings.setPresets(Preset.defaultValues);
            return settings;
        }
        Settings settings = gson.fromJson(settingsJson, Settings.class);
        return settings;
    }

    public static String getRawData() {
        String settingsJson = preferences.get(Version.getSettingsVersion(), null);
        return settingsJson;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public Settings setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
        return this;
    }

    public boolean isShowHints() {
        return showHints;
    }

    public Settings setShowHints(boolean showHints) {
        this.showHints = showHints;
        return this;
    }

    public List<Preset> getPresets() {
        return presets;
    }

    public void setPresets(List<Preset> presets) {
        this.presets = presets;
    }

    public Set<String> getGenres() {
        return genres;
    }

    public void setGenres(Set<String> genres) {
        this.genres = genres;
    }

    public String getChapterFormat() {
        return chapterFormat;
    }

    public void setChapterFormat(String chapterFormat) {
        this.chapterFormat = chapterFormat;
    }

    public String getFilenameFormat() {
        return filenameFormat;
    }

    public void setFilenameFormat(String filenameFormat) {
        this.filenameFormat = filenameFormat;
    }

    public String getPartFormat() {

        return partFormat;
    }

    public void setPartFormat(String partFormat) {
        this.partFormat = partFormat;
    }

    public String getChapterContext() {
        return chapterContext;
    }

    public Settings setChapterContext(String chapterContext) {
        this.chapterContext = chapterContext;
        return this;
    }

    public String getChapterCustomTitle() {
        return chapterCustomTitle;
    }

    public Settings setChapterCustomTitle(String chapterCustomTitle) {
        this.chapterCustomTitle = chapterCustomTitle;
        return this;
    }

    public File getOutputFolder() {
        File output = new File(outputFolder);
        if (output.exists()) {
            return output;
        } else if (output.getParentFile().exists()) {
            return output.getParentFile();
        } else {
            return new File(System.getProperty("user.home"));
        }
    }

    public Settings setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
        return this;
    }

    public File getSourceFolder() {
        File source = new File(sourceFolder);
        if (source.exists()) {
            return source;
        } else if (source.getParentFile().exists()) {
            return source.getParentFile();
        } else {
            return new File(System.getProperty("user.home"));
        }
    }

    public Settings setSourceFolder(String sourceFolder) {
        this.sourceFolder = sourceFolder;
        return this;
    }

    public Preset findPreset(String name) {
        for (Preset preset : presets) {
            if (preset.getName().equals(name)) return preset;
        }
        return null;
    }

    public int getLastUsedPreset() {
        return lastUsedPreset;
    }

    public void setLastUsedPreset(int lastUsedPreset) {
        this.lastUsedPreset = lastUsedPreset;
    }

    public static class FormatInstanceCreator implements InstanceCreator<Format> {
        private final String formatType;

        public FormatInstanceCreator(String formatType) {
            this.formatType = formatType;
        }

        @Override
        public Format createInstance(Type type) {
            return Format.instance(formatType);
        }
    }
}

