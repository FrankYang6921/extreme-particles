package top.frankyang.exp.mixin;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import top.frankyang.exp.ParticleUtils;
import top.frankyang.exp.group.ParticleGroupMgr;

@Mixin(Particle.class)
public class ParticleDeathHook {
    @Shadow
    protected boolean dead;

    /**
     * @reason No reason!!
     * @author kworker
     */
    @Overwrite
    public void markDead() {
        this.dead = true;
        ParticleGroupMgr.deathHook(this);
        ParticleUtils.deathHook(this);
    }
}
