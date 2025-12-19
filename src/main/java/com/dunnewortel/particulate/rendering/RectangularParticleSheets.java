package com.dunnewortel.particulate.rendering;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleTextureSheet;

@Environment(EnvType.CLIENT)
public class RectangularParticleSheets {
	public static final ParticleTextureSheet RECTANGULAR_PARTICLES = new ParticleTextureSheet("rectangular_particles");
}
