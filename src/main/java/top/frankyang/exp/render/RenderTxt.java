package top.frankyang.exp.render;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import top.frankyang.exp.Main;
import top.frankyang.exp.internal.Renderer;
import top.frankyang.exp.internal.RendererContext;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class RenderTxt implements Renderer {
    public static final RenderTxt INSTANCE = new RenderTxt();

    private static final RenderingHints renderingHints;

    static {
        renderingHints = new RenderingHints(null);
        renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        renderingHints.put(
                RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB
        );
    }

    private RenderTxt() {
    }

    private static void renderMain(ParticleEffect effect, String data, Vec3d origin, Vec3d delta, Vec3d color, String font, Vec2f size, int type, float alpha, int life, float scale, String group) {
        if (Main.isParticleConstructionPaused()) {
            return;
        }

        int fontSize;
        float length = size.x / getRealLength(data);  // The total width of the string

        if (size.x <= 1e-3f && size.y <= 1e-3f) {
            fontSize = 8;
        } else if (size.y <= 1e-3f) {
            fontSize = Math.round(length);  // The total width of the string
        } else if (size.x <= 1e-3f) {
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

        File file;
        try {
            ImageIO.write(bufferedImage, "PNG", file = File.createTempFile("txt2png", ".png"));
        } catch (IOException e) {  // Throw it!!
            throw new RuntimeException(e);
        }
        file.deleteOnExit();

        RenderImg.renderMain(effect,
                file.getAbsolutePath(),
                origin,
                delta,
                null,
                false,
                size,
                type,
                1,
                life,
                scale,
                group);
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

    @Override
    public void renderContext(RendererContext rendererContext) {
        if (!(rendererContext instanceof TxtRenderContext)) {
            throw new IllegalArgumentException("Invalid context type.");
        }
        TxtRenderContext c = (TxtRenderContext) rendererContext;
        c.catchFeedback(
                () -> renderMain(c.effect, c.data, c.origin, c.delta, c.color, c.font, c.size, c.type, c.alpha, c.life, c.scale, c.group)
        );
    }

    public static class TxtRenderContext extends RendererContext {
        public final ParticleEffect effect;
        public final String data;
        public final Vec3d origin;
        public final Vec3d delta;
        public final Vec3d color;
        public final String font;
        public final Vec2f size;
        public final int type;
        public final float alpha;
        public final int life;
        public final float scale;
        public final String group;

        public TxtRenderContext(ParticleEffect effect, String data, Vec3d origin, Vec3d delta, Vec3d color, String font, Vec2f size, int type, float alpha, int life, float scale, String group) {
            this.effect = effect;
            this.data = data;
            this.origin = origin;
            this.delta = delta;
            this.color = color;
            this.font = font;
            this.size = size;
            this.type = type;
            this.alpha = alpha;
            this.life = life;
            this.scale = scale;
            this.group = group;
        }

        @Override
        public String getSuccessfulFeedback() {
            return "通过文本批量构造了粒子。";
        }
    }
}
