package game.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class AppSettings {
    private AppSettings() {}
    public static ConfigValues load() {
        try (InputStream stream=AppSettings.class.getResourceAsStream("game.properties")) {
            if (stream==null) throw new IllegalArgumentException("Missing game.properties");
            Properties p=PropertiesReader.read(new InputStreamReader(stream,StandardCharsets.UTF_8));
            ConfigValues c=new ConfigValues(p);
            c.range("transition.fadeOutSeconds",.01,10); c.range("transition.fadeInSeconds",.01,10);
            c.range("loop.maxFrameSeconds",.01,1); c.range("loop.timerMillis",1,100);
            for (String key: new String[]{"hud.progressX","hud.progressY","hud.progressWidth","hud.progressHeight","hud.avatarSize"}) c.range(key,1,1000);
            return c;
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
}
