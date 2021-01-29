package top.frankyang.exp.mixin;

import net.minecraft.client.particle.EmitterParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourceReloadListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@Mixin(ParticleManager.class)
public abstract class BetterParticleMgr implements ResourceReloadListener {
    @Shadow
    protected ClientWorld world;
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

    public void tick() {
        Map<ParticleTextureSheet, Queue<Particle>> thisParticles = this.particles;

        if (!thisParticles.isEmpty()) {
            thisParticles.forEach(
                    (particleTextureSheet, queue) -> this.tickParticles(queue)
            );
        }

        if (!this.newEmitterParticles.isEmpty()) {
            this.newEmitterParticles.forEach(EmitterParticle::tick);
        }

        Particle particle;
        if (!this.newParticles.isEmpty()) {
            while ((particle = this.newParticles.poll()) != null) {
                thisParticles.computeIfAbsent(
                        particle.getType(), (particleTextureSheet) -> new ArrayDeque<>()
                ).add(particle);
            }
        }
    }
}
