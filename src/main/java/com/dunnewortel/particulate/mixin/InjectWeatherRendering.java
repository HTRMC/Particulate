package com.dunnewortel.particulate.mixin;

import com.dunnewortel.particulate.Main;
import com.dunnewortel.particulate.Particles;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.WeatherRendering;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.tag.FluidTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(WeatherRendering.class)
public class InjectWeatherRendering
{
	@ModifyExpressionValue(
			method = "addParticlesAndSound",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/particle/ParticleTypes;RAIN:Lnet/minecraft/particle/SimpleParticleType;"
			)
	)
	private SimpleParticleType modifyParticleEffect(SimpleParticleType original, @Local FluidState fluidState) {
		if (fluidState.isIn(FluidTags.WATER) && Main.CONFIG.rainRipples())
		{
			return Particles.WATER_RIPPLE;
		}
		return original;
	}
}