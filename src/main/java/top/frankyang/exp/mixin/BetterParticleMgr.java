package top.frankyang.exp.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.particle.EmitterParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourceReloadListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import top.frankyang.exp.Main;

import java.util.*;

@Mixin(ParticleManager.class)
public abstract class BetterParticleMgr implements ResourceReloadListener {
    @Final
    @Shadow
    private static List<ParticleTextureSheet> PARTICLE_TEXTURE_SHEETS;
    private final Tessellator tessellator = Tessellator.getInstance();
    private final BufferBuilder bufferBuilder = tessellator.getBuffer();
    @Shadow
    protected ClientWorld world;
    @Final
    @Shadow
    private TextureManager textureManager;
    @Final
    @Shadow
    private Map<ParticleTextureSheet, Queue<Particle>> particles;
    @Final
    @Shadow
    private Queue<EmitterParticle> newEmitterParticles;
    @Final
    @Shadow
    private Queue<Particle> newParticles;

    @Shadow
    protected abstract void tickParticles(Collection<Particle> collection);

    /**
     * @reason No reason!!
     * @author kworker
     */
    @Final
    @Overwrite
    public void tick() {
        Map<ParticleTextureSheet, Queue<Particle>> particles = this.particles;

        if (!particles.isEmpty()) {
            particles.forEach(
                    (particleTextureSheet, queue) -> this.tickParticles(queue)
            );
        }

        if (!this.newEmitterParticles.isEmpty()) {
            this.newEmitterParticles.forEach(EmitterParticle::tick);
        }

        Particle particle;
        if (!this.newParticles.isEmpty()) {
            while ((particle = this.newParticles.poll()) != null) {
                particles.computeIfAbsent(
                        particle.getType(), (particleTextureSheet) -> new ArrayDeque<>()
                ).add(particle);
            }
        }
    }

    /**
     * @reason No reason!!
     * @author kworker
     */
    @Final
    @Overwrite
    @SuppressWarnings("deprecation")
    public void renderParticles(MatrixStack matrixStack, VertexConsumerProvider.Immediate immediate, LightmapTextureManager lightmapTextureManager, Camera camera, float f) {
        final boolean safeMode = !Main.doUnsafeRendererOptimization;

        if (safeMode) {
            lightmapTextureManager.enable();
            RenderSystem.enableAlphaTest();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.enableFog();
        }
        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(matrixStack.peek().getModel());

        int i = 0;
        int size = PARTICLE_TEXTURE_SHEETS.size();

        while (true) {
            ParticleTextureSheet particleTextureSheet;
            Queue<Particle> queue;

            do {
                if (i == size) {
                    RenderSystem.popMatrix();
                    if (safeMode) {
                        RenderSystem.depthMask(true);
                        RenderSystem.depthFunc(515);
                        RenderSystem.disableBlend();
                        RenderSystem.defaultAlphaFunc();
                        lightmapTextureManager.disable();
                        RenderSystem.disableFog();
                    }
                    return;
                }

                particleTextureSheet = PARTICLE_TEXTURE_SHEETS.get(i++);
                queue = particles.get(particleTextureSheet);
            } while (queue == null);

            particleTextureSheet.begin(bufferBuilder, textureManager);

            for (Particle particle : queue) {
                particle.buildGeometry(bufferBuilder, camera, f);
            }

            particleTextureSheet.draw(tessellator);
        }
    }
}
