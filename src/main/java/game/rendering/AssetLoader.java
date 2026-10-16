package game.rendering;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import javax.imageio.ImageIO;

/** Cache successful and missing resources alike, never do IO every frame. */
public final class AssetLoader {
    private final Map<String,Optional<BufferedImage>> images=new HashMap<>();
    public BufferedImage image(String path) {
        if (path==null || path.isBlank()) return null;
        return images.computeIfAbsent(path,key->{
            URL resource=AssetLoader.class.getResource(key);
            if (resource==null) return Optional.empty();
            try { return Optional.ofNullable(ImageIO.read(resource)); }
            catch (IOException e) { return Optional.empty(); }
        }).orElse(null);
    }
}
