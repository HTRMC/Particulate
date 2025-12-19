package com.dunnewortel.particulate.mixin;

import com.dunnewortel.particulate.rendering.RectangularParticleRenderer;
import com.dunnewortel.particulate.rendering.RectangularParticleSheets;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleRenderer;
import net.minecraft.client.particle.ParticleTextureSheet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public class InjectParticleManagerCreateRenderer {

	@Inject(method = "createParticleRenderer", at = @At("HEAD"), cancellable = true)
	private void onCreateParticleRenderer(ParticleTextureSheet textureSheet,
	                                      CallbackInfoReturnable<ParticleRenderer<?>> cir) {
		if (textureSheet == RectangularParticleSheets.RECTANGULAR_PARTICLES) {
			cir.setReturnValue(new RectangularParticleRenderer((ParticleManager) (Object) this));
		}
	}
}
