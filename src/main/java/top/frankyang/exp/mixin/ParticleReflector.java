package top.frankyang.exp.mixin;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ParticleReflector {
    @Accessor("x")
    double getX();

    @Accessor("x")
    void setX(double x);

    @Accessor("y")
    double getY();

    @Accessor("y")
    void setY(double y);

    @Accessor("z")
    double getZ();

    @Accessor("z")
    void setZ(double z);

    @Accessor("velocityX")
    double getVelocityX();

    @Accessor("velocityX")
    void setVelocityX(double dx);

    @Accessor("velocityY")
    double getVelocityY();

    @Accessor("velocityY")
    void setVelocityY(double dy);

    @Accessor("velocityZ")
    double getVelocityZ();

    @Accessor("velocityZ")
    void setVelocityZ(double dz);

    @Accessor("colorRed")
    float getColorRed();

    @Accessor("colorRed")
    void setColorRed(float r);

    @Accessor("colorGreen")
    float getColorGreen();

    @Accessor("colorGreen")
    void setColorGreen(float g);

    @Accessor("colorBlue")
    float getColorBlue();

    @Accessor("colorBlue")
    void setColorBlue(float b);

    @Accessor("colorAlpha")
    float getColorAlpha();

    @Accessor("colorAlpha")
    void setColorAlpha(float a);

    @Accessor("maxAge")
    int getMaxAge();

    @Accessor("maxAge")
    void setMaxAge(int age);
}
