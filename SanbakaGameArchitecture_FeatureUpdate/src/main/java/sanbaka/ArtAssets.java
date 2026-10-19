package sanbaka;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;

/** Nạp PNG đặt ngay trong thư mục class đối tượng; ảnh vắng mặt thì dùng hình Java. */
final class ArtAssets {
    private static final Map<String, Optional<BufferedImage>> CACHE = new ConcurrentHashMap<>();

    private ArtAssets() {}

    static BufferedImage load(String objectFolder, String imageName) {
        String relative = objectFolder + "/" + imageName;
        return CACHE.computeIfAbsent(relative, ArtAssets::read).orElse(null);
    }

    private static Optional<BufferedImage> read(String relative) {
        Path cwd = Paths.get("").toAbsolutePath();
        Optional<BufferedImage> image = searchFrom(cwd, relative);
        if (image.isPresent()) return image;

        try {
            Path classes = Paths.get(ArtAssets.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            if (classes.toFile().isFile()) classes = classes.getParent();
            image = searchFrom(classes, relative);
            if (image.isPresent()) return image;
        } catch (Exception ignored) {
            // Nếu ảnh không tìm thấy thì dùng hình Java.
        }
        return Optional.empty();
    }

    private static Optional<BufferedImage> searchFrom(Path start, String relative) {
        for (Path dir = start; dir != null; dir = dir.getParent()) {
            Path objectImage = dir.resolve("src/main/java/sanbaka/objects").resolve(relative);
            Optional<BufferedImage> image = readFile(objectImage);
            if (image.isPresent()) return image;

            Path resourceImage = dir.resolve("assets").resolve(relative);
            image = readFile(resourceImage);
            if (image.isPresent()) return image;
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
