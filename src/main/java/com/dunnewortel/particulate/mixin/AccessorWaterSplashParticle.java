package com.dunnewortel.particulate.mixin;

import com.dunnewortel.particulate.particles.splashes.WaterSplashParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.awt.*;

@Mixin(WaterSplashParticle.class)
public interface AccessorWaterSplashParticle
{
	@Accessor("width")
	float getWidth();

	@Accessor("height")
	float getHeight();

	@Accessor("color")
	Color getColor();

	@Accessor("unit")
	float getUnit();

	@Accessor("colored")
	boolean isColored();

	@Accessor("minU")
	float getMinU();

	@Accessor("maxU")
	float getMaxU();

	@Accessor("minV")
	float getMinV();

	@Accessor("maxV")
	float getMaxV();

	int getBrightness(float tickDelta);
}