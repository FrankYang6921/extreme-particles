package top.frankyang.exp.render;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.io.*;
import java.nio.file.Paths;

public final class RenderSvg {
    private RenderSvg() {
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
        TranscoderInput inputImage = new TranscoderInput(Paths.get(data).toUri().toString());

        OutputStream outputStream;
        File file;
        try {
            outputStream = new FileOutputStream(file = File.createTempFile("svg2png", ".png"));
        } catch (IOException e) {
            return "无法写入临时文件，因为发生了I/O错误。";
        }

        TranscoderOutput outputImage = new TranscoderOutput(outputStream);
        PNGTranscoder converter = new PNGTranscoder();

        if (!size.equals(Vec2f.ZERO)) {
            converter.addTranscodingHint(ImageTranscoder.KEY_WIDTH, size.x);
            converter.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, size.y);
        }

        try {
            converter.transcode(inputImage, outputImage);
        } catch (TranscoderException e) {
            return "无法转码SVG文件，因为其不符SVG规范。";
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            return "无法写入临时文件，因为发生了I/O错误。";
        }

        file.deleteOnExit();

        return RenderImg.renderPattern(effect, file.getAbsolutePath(), origin, delta, color, mono, size, type, alpha, life, scale, id);
    }
}
