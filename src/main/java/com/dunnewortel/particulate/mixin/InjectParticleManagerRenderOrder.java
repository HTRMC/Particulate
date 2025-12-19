package com.dunnewortel.particulate.mixin;

import com.dunnewortel.particulate.rendering.RectangularParticleSheets;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ParticleManager.class)
public class InjectParticleManagerRenderOrder {

	@Shadow
	@Mutable
	private static List<ParticleTextureSheet> PARTICLE_TEXTURE_SHEETS;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void onStaticInit(CallbackInfo ci) {
		List<ParticleTextureSheet> newList = new ArrayList<>(PARTICLE_TEXTURE_SHEETS);
		newList.add(RectangularParticleSheets.RECTANGULAR_PARTICLES);
		PARTICLE_TEXTURE_SHEETS = newList;
	}
}
