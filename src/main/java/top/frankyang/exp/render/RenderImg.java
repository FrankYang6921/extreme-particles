package top.frankyang.exp.render;

import net.minecraft.client.particle.Particle;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import top.frankyang.exp.Main;
import top.frankyang.exp.group.ParticleGroupMgr;
import top.frankyang.exp.internal.Renderer;
import top.frankyang.exp.internal.RendererContext;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public final class RenderImg implements Renderer {
    public static final RenderImg INSTANCE = new RenderImg();

    public static final int XY = 1;
    public static final int XZ = 2;
    public static final int YZ = 4;
    public static final int HR = 8;
    public static final int VR = 16;

    private RenderImg() {
    }

    static void renderMain(ParticleEffect effect, String data, Vec3d origin, Vec3d delta, Vec3d color, boolean mono, Vec2f size, int type, float alpha, int life, float scale, String group) {
        if (Main.disabled) {
            return;
        }

        BufferedImage image;

        try {
            File imageFile = new File(data);
            image = ImageIO.read(imageFile);
        } catch (IOException e) {
            throw new RuntimeException("无法读取指定的图像文件。");
        }

        if (type > 31) {
            throw new RuntimeException("无法处理像素，因为指定的类型不合法。");
        }

        if (type < 1) {
            throw new RuntimeException("无法处理像素，因为没有指定生成平面。");
        }

        BufferedImage i = image;

        if (!size.equals(Vec2f.ZERO)) {
            Image scaled = image.getScaledInstance(
                    (int) size.x, (int) size.y, BufferedImage.SCALE_SMOOTH
            );
            i = new BufferedImage(
                    (int) size.x, (int) size.y, BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D cxt = i.createGraphics();
            cxt.drawImage(scaled, 0, 0, null);
        }

        int w = i.getWidth();
        int h = i.getHeight();

        // Use cache to improve speed
        int[][] pixels = getPixels(i, w, h);

        ArrayList<Particle> particles;
        particles = new ArrayList<>();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int px = pixels[x][y];

                int b = px & 0xff;
                int g = (px >> 8) & 0xff;
                int r = (px >> 16) & 0xff;
                int a = (px >> 24) & 0xff;

                if (a == 0) {  // Hide invisible particles
                    continue;
                }

                if (mono) {  // Add monochrome filter
                    int k = (r + g + b) / 3;
                    r = k;
                    g = k;
                    b = k;
                }

                if (color != null) {  // Add color filter
                    r *= color.x / 255f;
                    g *= color.y / 255f;
                    b *= color.z / 255f;
                }

                if (1 - alpha > 1e-3f) {  // Add alpha filter
                    a *= alpha;
                }

                // Build particles

                int tx = (type & HR) != 0 ? x : -x;
                int ty = (type & VR) != 0 ? y : -y;
                double ox = (type & HR) != 0 ? 0 : w * .1 * scale;
                double oy = (type & VR) != 0 ? 0 : h * .1 * scale;

                double rx = 0,
                        ry = 0,
                        rz = 0;

                if ((type & XY) != 0) {
                    rx = tx * .1 * scale + ox;
                    ry = ty * .1 * scale + oy;
                } else if ((type & XZ) != 0) {
                    rx = tx * .1 * scale + ox;
                    rz = ty * .1 * scale + oy;
                } else if ((type & YZ) != 0) {
                    rz = tx * .1 * scale + ox;
                    ry = ty * .1 * scale + oy;
                } else {
                    rx = tx * .1 * scale + ox;
                    ry = ty * .1 * scale + oy;
                }

                Vec3d position = new Vec3d(origin.x + rx, origin.y + ry, origin.z + rz);
                if (group != null) {
                    Particle particle = Main.constructParticle(
                            effect, position, delta, new Vec3d(r, g, b), a / 255f, life, scale
                    );
                    particles.add(particle);
                } else {
                    Main.constructParticle(
                            effect, position, delta, new Vec3d(r, g, b), a / 255f, life, scale
                    );
                }
            }
        }

        ParticleGroupMgr.INSTANCE.put(group, particles);
    }

    private static int[][] getPixels(BufferedImage bf, int w, int h) {
        int[] rawPixels = new int[w * h];
        int[][] pixels = new int[w][h];

        bf.getRGB(
                0, 0, w, h, rawPixels, 0, w
        );

        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                pixels[x][y] = rawPixels[y * w + x];

        return pixels;
    }

    @Override
    public void renderContext(RendererContext rendererContext) {
        if (!(rendererContext instanceof ImgRenderContext)) {
            throw new IllegalArgumentException("Invalid context type.");
        }
        ImgRenderContext c = (ImgRenderContext) rendererContext;
        c.catchFeedback(
                () -> renderMain(c.effect, c.data, c.origin, c.delta, c.color, c.mono, c.size, c.type, c.alpha, c.life, c.scale, c.group)
        );
    }

    public static class ImgRenderContext extends RendererContext {
        public final ParticleEffect effect;
        public final String data;
        public final Vec3d origin;
        public final Vec3d delta;
        public final Vec3d color;
        public final boolean mono;
        public final Vec2f size;
        public final int type;
        public final float alpha;
        public final int life;
        public final float scale;
        public final String group;

        public ImgRenderContext(ParticleEffect effect, String data, Vec3d origin, Vec3d delta, Vec3d color, boolean mono, Vec2f size, int type, float alpha, int life, float scale, String group) {
            this.effect = effect;
            this.data = data;
            this.origin = origin;
            this.delta = delta;
            this.color = color;
            this.mono = mono;
            this.size = size;
            this.type = type;
            this.alpha = alpha;
            this.life = life;
            this.scale = scale;
            this.group = group;
        }

        @Override
        public String getSuccessfulFeedback() {
            return "通过图像批量构造了粒子。";
        }
    }
}
