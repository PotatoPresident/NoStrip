package us.potatoboy.nostrip.client;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class NoStripConfig {
    private final boolean stripping;
    private final boolean feedback;
    public NoStripConfig(boolean stripping, boolean feedback) {
        this.stripping = stripping;
        this.feedback = feedback;
    }

    public static void save(NoStripConfig config) {
        JsonObject object = new JsonObject();
        object.addProperty("stripping", config.getStrip());
        object.addProperty("send_feedback", config.sendFeedback());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            Files.write(Paths.get(FabricLoader.getInstance().getConfigDir().toAbsolutePath().toString() + "\\nostrip.json"), gson.toJson(object).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static NoStripConfig read() throws IOException {
        String jsonString = new String(Files.readAllBytes(Paths.get(FabricLoader.getInstance().getConfigDir().toAbsolutePath().toString() + "\\nostrip.json")));
        JsonObject object = new JsonParser().parse(jsonString).getAsJsonObject();
        boolean stripping = object.get("stripping").getAsBoolean();
        boolean feedback = object.get("send_feedback").getAsBoolean();
        return new NoStripConfig(stripping, feedback);
    }
    public boolean getStrip() {
        return this.stripping;
    }
    public boolean sendFeedback() {
        return this.feedback;
    }
    public static void create() {
        JsonObject object = new JsonObject();
        object.addProperty("stripping", false);
        object.addProperty("send_feedback", true);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            Files.write(Paths.get(FabricLoader.getInstance().getConfigDir().toAbsolutePath().toString() + "\\nostrip.json"), gson.toJson(object).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
