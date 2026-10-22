package sanbaka.assets;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;

/** Nạp PNG đặt trong assets hoặc classpath; ảnh vắng mặt thì dùng hình vẽ Java. */
public final class ArtAssets {
    private static final Map<String, Optional<BufferedImage>> CACHE = new ConcurrentHashMap<>();

    private ArtAssets() {}

    public static BufferedImage load(String objectFolder, String imageName) {
        String relative = objectFolder + "/" + imageName;
        return CACHE.computeIfAbsent(relative, ArtAssets::read).orElse(null);
    }

    private static Optional<BufferedImage> read(String relative) {
        // 1. Thử nạp từ ClassLoader / resource classpath
        String[] resourcePrefixes = {
            "/assets/objects/" + relative,
            "/assets/" + relative,
            "/image/" + relative,
            "/" + relative
        };
        for (String res : resourcePrefixes) {
            try (InputStream in = ArtAssets.class.getResourceAsStream(res)) {
                if (in != null) {
                    BufferedImage img = ImageIO.read(in);
                    if (img != null) return Optional.of(img);
                }
            } catch (Exception ignored) {}
        }

        // 2. Thử tìm trên file system từ working directory
        Path cwd = Paths.get("").toAbsolutePath();
        Optional<BufferedImage> image = searchFrom(cwd, relative);
        if (image.isPresent()) return image;

        try {
            Path classes = Paths.get(ArtAssets.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            if (classes.toFile().isFile()) classes = classes.getParent();
            image = searchFrom(classes, relative);
            if (image.isPresent()) return image;
        } catch (Exception ignored) {}

        return Optional.empty();
    }

    private static Optional<BufferedImage> searchFrom(Path start, String relative) {
        for (Path dir = start; dir != null; dir = dir.getParent()) {
            Path[] candidatePaths = {
                dir.resolve("assets/objects").resolve(relative),
                dir.resolve("assets").resolve(relative),
                dir.resolve("src/main/resources/assets/objects").resolve(relative),
                dir.resolve("src/main/resources/assets").resolve(relative),
                dir.resolve("src/main/java/sanbaka/objects").resolve(relative)
            };
            for (Path candidate : candidatePaths) {
                Optional<BufferedImage> image = readFile(candidate);
                if (image.isPresent()) return image;
            }
        }
        return Optional.empty();
    }

    private static Optional<BufferedImage> readFile(Path path) {
        if (!path.toFile().isFile()) return Optional.empty();
        try {
            return Optional.ofNullable(ImageIO.read(path.toFile()));
        } catch (IOException ignored) {
            return Optional.empty();
        }
    }
}

