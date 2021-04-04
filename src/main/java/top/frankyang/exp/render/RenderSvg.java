package top.frankyang.exp.render;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import top.frankyang.exp.internal.Renderer;
import top.frankyang.exp.internal.RendererContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;

public final class RenderSvg implements Renderer {
    public static final RenderSvg INSTANCE = new RenderSvg();

    private RenderSvg() {
    }

    public static void renderPattern(ParticleEffect effect,
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
            throw new RuntimeException("无法写入临时文件，因为发生了I/O错误。");
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
            throw new RuntimeException("无法转码SVG文件，因为其不符SVG规范。");
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("无法写入临时文件，因为发生了I/O错误。");
        }

        file.deleteOnExit();

        RenderImg.renderPattern(effect, file.getAbsolutePath(), origin, delta, color, mono, size, type, alpha, life, scale, id);
    }

    @Override
    public void renderPattern(RendererContext rendererContext) {
        if (!(rendererContext instanceof RenderImg.ImgRenderContext)) {
            throw new IllegalArgumentException("Invalid context type.");
        }
        RenderImg.ImgRenderContext c = (RenderImg.ImgRenderContext) rendererContext;
        c.catchFeedback(
                () -> renderPattern(c.effect, c.data, c.origin, c.delta, c.color, c.mono, c.size, c.type, c.alpha, c.life, c.scale, c.id)
        );
    }

}
