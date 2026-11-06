package anilink.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * A full-bleed, auto-rotating photo background with a cross-fade between
 * slides and a dark overlay so light-coloured text painted on top (by the
 * panel's own child components, added via the usual GroupLayout) stays
 * legible regardless of how bright a given photo is.
 *
 * <p>Used in place of a plain {@code JPanel} for the guest landing page's
 * hero banner: swapping the declared type is enough, since GroupLayout works
 * on any {@link java.awt.Container} and this changes nothing about how the
 * hero's own title/subtitle/buttons are laid out over it.
 */
public class SlideshowPanel extends JPanel {

    private static final int SLIDE_MS = 5000;
    private static final int FADE_MS = 900;
    private static final int TICK_MS = 33;

    private final List<BufferedImage> images = new ArrayList<>();
    private final Color overlay = new Color(0, 0, 0, 115);
    private final Timer timer;
    private int index;
    private long elapsed;

    public SlideshowPanel() {
        timer = new Timer(TICK_MS, e -> tick());
    }

    /** Loads images from classpath resources (e.g. {@code "/images/hero-corn.jpg"}). */
    public void setImages(String... classpathResources) {
        images.clear();
        for (String path : classpathResources) {
            try (InputStream in = getClass().getResourceAsStream(path)) {
                if (in != null) {
                    images.add(ImageIO.read(in));
                }
            } catch (IOException ignored) {
                // Skip a photo that fails to load rather than breaking the page.
            }
        }
        index = 0;
        elapsed = 0;
        repaint();
    }

    public void start() {
        if (images.size() > 1 && !timer.isRunning()) {
            timer.start();
        }
    }

    public void stop() {
        timer.stop();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        timer.stop();
    }

    private void tick() {
        elapsed += TICK_MS;
        if (elapsed >= SLIDE_MS) {
            elapsed = 0;
            index = (index + 1) % images.size();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        if (images.isEmpty() || w <= 0 || h <= 0) {
            super.paintComponent(g);
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        drawCover(g2, images.get(index), w, h, 1f);

        boolean fading = images.size() > 1 && elapsed >= SLIDE_MS - FADE_MS;
        if (fading) {
            float progress = Math.min(1f, (elapsed - (SLIDE_MS - FADE_MS)) / (float) FADE_MS);
            int next = (index + 1) % images.size();
            drawCover(g2, images.get(next), w, h, progress);
        }

        g2.setComposite(AlphaComposite.SrcOver);
        g2.setColor(overlay);
        g2.fillRect(0, 0, w, h);
        g2.dispose();
    }

    /** Draws {@code img} scaled and cropped to fill {@code w x h}, at the given opacity. */
    private void drawCover(Graphics2D g2, BufferedImage img, int w, int h, float alpha) {
        int iw = img.getWidth();
        int ih = img.getHeight();
        double scale = Math.max(w / (double) iw, h / (double) ih);
        int sw = (int) Math.round(w / scale);
        int sh = (int) Math.round(h / scale);
        int sx = (iw - sw) / 2;
        int sy = (ih - sh) / 2;

        Graphics2D slice = (Graphics2D) g2.create();
        if (alpha < 1f) {
            slice.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }
        slice.drawImage(img, 0, 0, w, h, sx, sy, sx + sw, sy + sh, null);
        slice.dispose();
    }
}
