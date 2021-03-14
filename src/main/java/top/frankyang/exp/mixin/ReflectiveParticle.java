package top.frankyang.exp.mixin;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ReflectiveParticle {
    @Accessor("x")
    double getX();
    @Accessor("y")
    double getY();
    @Accessor("z")
    double getZ();
    @Accessor("velocityX")
    double getVelocityX();
    @Accessor("velocityY")
    double getVelocityY();
    @Accessor("velocityZ")
    double getVelocityZ();
    @Accessor("colorRed")
    float getColorRed();
    @Accessor("colorGreen")
    float getColorGreen();
    @Accessor("colorBlue")
    float getColorBlue();
    @Accessor("colorAlpha")
    float getColorAlpha();
    @Accessor("maxAge")
    int getMaxAge();

    @Accessor("x")
    void setX(double x);
    @Accessor("y")
    void setY(double y);
    @Accessor("z")
    void setZ(double z);
    @Accessor("velocityX")
    void setVelocityX(double dx);
    @Accessor("velocityY")
    void setVelocityY(double dy);
    @Accessor("velocityZ")
    void setVelocityZ(double dz);
    @Accessor("colorRed")
    void setColorRed(float r);
    @Accessor("colorGreen")
    void setColorGreen(float g);
    @Accessor("colorBlue")
    void setColorBlue(float b);
    @Accessor("colorAlpha")
    void setColorAlpha(float a);
    @Accessor("maxAge")
    void setMaxAge(int age);
}
