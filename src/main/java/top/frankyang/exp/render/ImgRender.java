package top.frankyang.exp.render;

import net.minecraft.client.particle.Particle;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import top.frankyang.exp.Main;
import top.frankyang.exp.anime.AnimationMgr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public final class ImgRender {
    private static final int XY = 1;
    private static final int XZ = 2;
    private static final int YZ = 4;
    private static final int HR = 8;
    private static final int VR = 16;

    public static String renderPattern(ParticleEffect effect,
                                       Image data,
                                       Vec3d origin,
                                       Vec3d delta,
                                       Vec3d color,
                                       boolean mono,
                                       Vec2f size,
                                       int type,
                                       float alpha,
                                       int life,
                                       float scale,
                                       String id) {
        if (Main.disabled) {
            return null;
        }

        if ((type | 31) > 31) {
            return "无法处理像素，因为指定的类型不合法。";
        }

        if ((type & 8) == 0) {
            return "无法处理像素，因为没有指定生成平面。";
        }

        if (id != null && AnimationMgr.isAbsent(id)) {
            return "指定的标识符不是有效的动画。";
        }

        BufferedImage i;

        if (!size.equals(Vec2f.ZERO)) {
            Image scaled = data.getScaledInstance(
                    (int) size.x, (int) size.y, BufferedImage.SCALE_SMOOTH
            );
            i = new BufferedImage(
                    (int) size.x, (int) size.y, BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D cxt = i.createGraphics();
            cxt.drawImage(scaled, 0, 0, null);
        } else {
            i = (BufferedImage) data;
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

                if (mono) {  // Add monochrome filter
                    int k = (r + g + b) / 3;
                    r = k;
                    g = k;
                    b = k;
                }

                if (color != null) {  // Add color filter
                    r *= color.x / 255.;
                    g *= color.y / 255.;
                    b *= color.z / 255.;
                }

                if (1 - alpha > 1e-5f) {  // Add alpha filter
                    a *= alpha;
                }

                if (a == 0) {  // Hide invisible particles
                    continue;
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
                if (id != null) {
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

        if (id != null) {
            AnimationMgr.apply(id, particles);
        }

        return null;
    }

    public static String renderPattern(ParticleEffect effect,
                                       String data,
                                       Vec3d origin,
                                       Vec3d delta,
                                       Vec3d color,
                                       boolean mono,
                                       Vec2f size,
                                       int type,
                                       float alpha,
                                       int life,
                                       float scale,
                                       String id) {
        BufferedImage image;

        try {
            File imageFile = new File(data);
            image = ImageIO.read(imageFile);
        } catch (IOException e) {
            return "无法读取指定的图像文件。";
        }

        return renderPattern(effect, image, origin, delta, color, mono, size, type, alpha, life, scale, id);
    }

    private static int[][] getPixels(BufferedImage bf, int w, int h) {
        int[] rawPixels = new int[w * h];
        int[][] pixels = new int[w][h];

        bf.getRGB(0, 0, w, h, rawPixels, 0, w);

        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                pixels[x][y] = rawPixels[y * w + x];

        return pixels;
    }
}
