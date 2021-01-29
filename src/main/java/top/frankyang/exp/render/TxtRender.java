package top.frankyang.exp.render;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import top.frankyang.exp.Main;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class TxtRender {
    private static final RenderingHints renderingHints;

    static {
        renderingHints = new RenderingHints(null);
        renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        renderingHints.put(
                RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB
        );
    }

    public static String renderPattern(ParticleEffect effect,
                                       String data,
                                       Vec3d origin,
                                       Vec3d delta,
                                       Vec3d color,
                                       String font,
                                       Vec2f size,
                                       int type,
                                       float alpha,
                                       int life,
                                       float scale,
                                       String id) {
        if (Main.disabled) {
            return null;
        }

        int fontSize;
        float length = size.x / getRealLength(data);  // The total width of the string

        if (size.x <= 1e-5f && size.y <= 1e-5f) {
            fontSize = 8;
        } else if (size.y <= 1e-5f) {
            fontSize = Math.round(length);  // The total width of the string
        } else if (size.x <= 1e-5f) {
            fontSize = Math.round(size.y);  // The width of a single character
        } else {
            fontSize = Math.round(size.y);  // The width of a single character
        }

        final int width = getRealWidth(data, fontSize);
        final int height = Math.round(fontSize * 1.25f);

        size = new Vec2f(width, height);

        BufferedImage bufferedImage = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_ARGB  // With alpha
        );
        Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();

        graphics.setRenderingHints(renderingHints);

        graphics.setColor(new Color(
                255,
                255,
                255,
                0
        ));
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(new Color(
                (int) color.x,
                (int) color.y,
                (int) color.z,
                (int) alpha * 255
        ));
        graphics.setFont(
                new Font(font, Font.PLAIN, fontSize)
        );
        graphics.drawString(data, 0, fontSize);

        return ImgRender.renderPattern(effect, bufferedImage, origin, delta, null, false, size, type, 1, life, scale, id);
    }

    private static float getRealLength(String text) {
        float width = 0;

        for (char i : text.toCharArray()) {
            if ((int) i > 255)
                width += 1.;  // Wide char
            else
                width += .5;  // ASCII char
        }

        return width;
    }

    private static int getRealWidth(String text, int fontSize) {
        float width = getRealLength(text);
        return Math.round(width * fontSize);
    }
}
