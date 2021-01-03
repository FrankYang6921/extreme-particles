package top.frankyang.exp;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ExpTxtRender {
    static String renderPattern(ParticleEffect effect, String text, Vec3d origin, Vec3d delta, Vec3d color, String font, Vec2f size, int type, float alpha, int life, float scale) {
        if (ExpMain.disabled) {
            return null;
        }

        final int len = text.length();

        int fontSize;
        if (size.x <= 1e-5f && size.y <= 1e-5f) {
            fontSize = 8;
        } else if (size.y <= 1e-5f) {
            fontSize = Math.round(size.x / len);
        } else if (size.x <= 1e-5f) {
            fontSize = Math.round(size.y);  // Auto width
        } else {
            fontSize = Math.round(size.y);  // Auto width
        }

        final int width = getRealWidth(text, fontSize);
        final int height = Math.round(fontSize * 1.15f);

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();

        RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        rh.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        graphics.setRenderingHints(rh);

        graphics.setColor(new Color(255, 255, 255, 0));
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(new Color(
                (int) color.x,
                (int) color.y,
                (int) color.z,
                (int) alpha * 255
        ));
        graphics.setFont(new Font(font, Font.PLAIN, fontSize));
        graphics.drawString(text, 0, fontSize);

        return ExpImgRender.renderPattern(effect, bufferedImage, origin, delta, new Vec2f(width, height), type, life, scale);
    }

    static int getRealWidth(String text, int fontSize) {
        float width = 0;
        for (char i : text.toCharArray()) {
            if ((int) i > 255)
                width += 1;  // Wide char
            else
                width += .5;  // ASCII char
        }
        return Math.round(width * fontSize);
    }
}
