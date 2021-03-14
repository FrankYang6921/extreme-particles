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

import java.util.*;

@Mixin(ParticleManager.class)
public abstract class BetterParticleMgr implements ResourceReloadListener {
    @Final
    @Shadow
    private static List<ParticleTextureSheet> PARTICLE_TEXTURE_SHEETS;
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
     * @reason ??
     * @author kworker
     */
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
     * @reason ??
     * @author kworker
     */
    @Overwrite
    public void renderParticles(MatrixStack matrixStack, VertexConsumerProvider.Immediate immediate, LightmapTextureManager lightmapTextureManager, Camera camera, float f) {
        lightmapTextureManager.enable();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.enableFog();
        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(matrixStack.peek().getModel());
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        Iterator<ParticleTextureSheet> iterator = PARTICLE_TEXTURE_SHEETS.iterator();

        while (true) {
            ParticleTextureSheet particleTextureSheet;

            Iterable<Particle> iterable;
            do {
                if (!iterator.hasNext()) {
                    RenderSystem.popMatrix();
                    RenderSystem.depthMask(true);
                    RenderSystem.depthFunc(515);
                    RenderSystem.disableBlend();
                    RenderSystem.defaultAlphaFunc();
                    lightmapTextureManager.disable();
                    RenderSystem.disableFog();
                    return;
                }

                particleTextureSheet = iterator.next();
                iterable = this.particles.get(particleTextureSheet);
            } while (iterable == null);

            particleTextureSheet.begin(bufferBuilder, textureManager);

            //noinspection CodeBlock2Expr
            iterable.forEach(particle -> {
                particle.buildGeometry(bufferBuilder, camera, f);
            });

            particleTextureSheet.draw(tessellator);
        }
    }
}
