package top.frankyang.exp.mixin;

import com.google.common.collect.EvictingQueue;
import net.minecraft.client.particle.EmitterParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourceReloadListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;

@Mixin(ParticleManager.class)
public abstract class FastParticleManager implements ResourceReloadListener {
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
        if (!this.particles.isEmpty()) {
            this.particles.forEach((particleTextureSheet, queue) -> {
                this.world.getProfiler().push(particleTextureSheet.toString());
                this.tickParticles(queue);
                this.world.getProfiler().pop();
            });
        }

        if (!this.newEmitterParticles.isEmpty()) {
            for (EmitterParticle emitterParticle : this.newEmitterParticles) {
                emitterParticle.tick();
            }
        }

        Particle particle;
        if (!this.newParticles.isEmpty()) {
            while ((particle = this.newParticles.poll()) != null) {
                //noinspection UnstableApiUsage
                this.particles.computeIfAbsent(
                        particle.getType(), (particleTextureSheet) -> EvictingQueue.create(1048576)
                ).add(particle);
            }
        }
    }
}
