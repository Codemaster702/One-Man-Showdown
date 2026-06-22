//Mohammed Shekhibrahim
//June 15 2026
//Asset manager

package managers;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class AssetManager {

    private static final Map<String, Image> cache = new HashMap<>();
    private static final Map<String, BufferedImage> bufferedCache = new HashMap<>();

    public static Image load(String path) {
        return cache.computeIfAbsent(path, AssetManager::readImage);
    }

    /**
     * Load an image as a BufferedImage — needed for sprite sheets, which require
     * direct width access and sub-image (frame) drawing. CheerpJ-safe: reads from
     * the classpath first, then falls back to the working directory on desktop.
     */
    public static BufferedImage loadBuffered(String path) {
        return bufferedCache.computeIfAbsent(path, AssetManager::readBufferedImage);
    }

    private static BufferedImage readBufferedImage(String path) {
        try {
            URL url = resourceUrl(path);
            if (url != null) return ImageIO.read(url);
            File file = new File(path);
            if (file.exists()) return ImageIO.read(file);
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private static Image readImage(String path) {
        // GIFs go through ImageIcon so their animation frames are preserved;
        // everything else is decoded with ImageIO.
        boolean isGif = path.toLowerCase().endsWith(".gif");

        // CheerpJ runs in the browser with no local working directory — assets are
        // bundled inside the JAR, so look them up on the classpath first.
        URL url = resourceUrl(path);
        if (url != null) {
            if (isGif) {
                return new ImageIcon(url).getImage();
            }
            try {
                return ImageIO.read(url);
            } catch (IOException e) {
                return null;
            }
        }

        // Desktop fallback: read straight from the working directory.
        File file = new File(path);
        if (!file.exists()) return null;
        if (isGif) {
            return new ImageIcon(file.getAbsolutePath()).getImage();
        }
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            return null;
        }
    }

    // Resolve a relative asset path (e.g. "assets/foo.png") to a classpath URL.
    private static URL resourceUrl(String path) {
        String resource = path.startsWith("/") ? path : "/" + path;
        return AssetManager.class.getResource(resource);
    }

    private AssetManager() {}
}
