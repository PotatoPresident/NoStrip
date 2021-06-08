package us.potatoboy.nostrip.client;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class NoStripConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final boolean stripping = false;
    private final boolean feedback = true;

    public static NoStripConfig loadConfig(File file) {
        NoStripConfig config;

        if (file.exists() && file.isFile()) {
            try (
                    FileInputStream fileInputStream = new FileInputStream(file);
                    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            ) {
                config = GSON.fromJson(bufferedReader, NoStripConfig.class);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load config", e);
            }
        } else {
            config = new NoStripConfig();
        }

        config.saveConfig(file);

        return config;
    }

    public void saveConfig(File config) {
        try (
                FileOutputStream stream = new FileOutputStream(config);
                Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
        ) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }

    public boolean isStripping() {
        return this.stripping;
    }

    public boolean isFeedback() {
        return feedback;
    }
}
