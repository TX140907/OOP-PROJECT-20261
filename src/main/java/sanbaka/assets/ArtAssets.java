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

    /** Tạo BufferedImage tương thích trực tiếp với phần cứng GPU (VRAM) để vẽ nhanh nhất. */
    public static BufferedImage createCompatibleImage(int width, int height) {
        try {
            if (!java.awt.GraphicsEnvironment.isHeadless()) {
                java.awt.GraphicsConfiguration gc = java.awt.GraphicsEnvironment
                        .getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice()
                        .getDefaultConfiguration();
                if (gc != null) {
                    return gc.createCompatibleImage(width, height, java.awt.Transparency.TRANSLUCENT);
                }
            }
        } catch (Exception ignored) {}
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    private static Optional<BufferedImage> read(String relative) {
        // 1. Thử tìm trực tiếp từ thư mục assets nằm ngoài cùng của dự án
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

        // 2. Fallback qua ClassLoader nếu thư mục assets được gắn vào classpath
        String[] resourcePrefixes = {
            "/objects/" + relative,
            "/assets/objects/" + relative,
            "/assets/" + relative,
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

        return Optional.empty();
    }

    private static Optional<BufferedImage> searchFrom(Path start, String relative) {
        for (Path dir = start; dir != null; dir = dir.getParent()) {
            Path[] candidatePaths = {
                dir.resolve("assets/objects").resolve(relative),
                dir.resolve("assets").resolve(relative)
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

