package com.dunnewortel.particulate.mixin;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface AccessorParticle
{
	@Accessor
	void setStopped(boolean val);

	@Accessor
	double getX();

	@Accessor
	double getY();

	@Accessor
	double getZ();

	@Accessor
	double getLastX();

	@Accessor
	double getLastY();

	@Accessor
	double getLastZ();

	@Accessor
	int getAge();

	@Accessor
	int getMaxAge();
}
