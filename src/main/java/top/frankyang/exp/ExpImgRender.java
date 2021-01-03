package top.frankyang.exp;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ExpImgRender {
    private static final int XY = 1;
    private static final int XZ = 2;
    private static final int YZ = 4;
    private static final int HR = 8;
    private static final int VR = 16;

    static String renderPattern(ParticleEffect effect, BufferedImage image, Vec3d origin, Vec3d delta, Vec2f size, int type, int life, float scale) {
        if (ExpMain.disabled) {
            return null;
        }

        if (!size.equals(Vec2f.ZERO)) {
            Image scaled = image.getScaledInstance(
                    (int) size.x, (int) size.y, Image.SCALE_SMOOTH
            );
            image = new BufferedImage(
                    (int) size.x, (int) size.y,
                    BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D cxt = image.createGraphics();
            cxt.drawImage(scaled, 0, 0, null);
        }
        int w = image.getWidth();
        int h = image.getHeight();

        int[][] RGBArray = imageToArray(image, w, h);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int px = RGBArray[x][y];
                int b = px & 0xff;
                int g = (px >> 8) & 0xff;
                int r = (px >> 16) & 0xff;
                int a = (px >> 24) & 0xff;

                if (a == 0) {
                    continue;
                }

                double rx = 0,
                        ry = 0,
                        rz = 0;

                if (type > 31) {
                    return "无法处理像素，因为指定的类型不合法。";
                }

                int tx = (type & HR) != 0 ? x : -x;
                int ty = (type & VR) != 0 ? y : -y;
                double ox = (type & HR) != 0 ? 0 : w * .1 * scale;
                double oy = (type & VR) != 0 ? 0 : h * .1 * scale;

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
                    return "无法处理像素，因为没有指定生成平面。";
                }

                Vec3d position = new Vec3d(origin.x + rx, origin.y + ry, origin.z + rz);
                ExpMain.constructParticle(effect, position, delta, new Vec3d(r, g, b), a / 255.0f, life, scale);
            }
        }

        return null;
    }

    static String renderPattern(ParticleEffect effect, String path, Vec3d origin, Vec3d delta, Vec2f size, int type, int life, float scale) {
        BufferedImage image;

        try {
            File imageFile = new File(path);
            image = ImageIO.read(imageFile);
        } catch (IOException e) {
            return "无法读取指定的图像文件。";
        }

        return renderPattern(effect, image, origin, delta, size, type, life, scale);
    }

    public static int[][] imageToArray(BufferedImage bf, int w, int h) {
        int[] data = new int[w * h];

        bf.getRGB(0, 0, w, h, data, 0, w);

        int[][] RGBArray = new int[w][h];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                RGBArray[x][y] = data[y * w + x];
            }
        }

        return RGBArray;
    }
}
