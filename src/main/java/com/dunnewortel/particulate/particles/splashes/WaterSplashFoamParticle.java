package com.dunnewortel.particulate.particles.splashes;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class WaterSplashFoamParticle extends WaterSplashParticle
{
	WaterSplashFoamParticle(ClientWorld clientWorld, double x, double y, double z, float width, float height, SpriteProvider provider)
	{
		super(clientWorld, x, y, z, width, height, provider);
		colored = false;
	}

	@Environment(EnvType.CLIENT)
	public static class Factory implements ParticleFactory<SimpleParticleType>
	{
		private final SpriteProvider provider;

		public Factory(SpriteProvider provider)
		{
			this.provider = provider;
		}

		@Nullable
		@Override
		public Particle createParticle(SimpleParticleType SimpleParticleType, ClientWorld clientWorld, double x, double y, double z, double g, double h, double i)
		{
			return new WaterSplashFoamParticle(clientWorld, x, y, z, (float) g, (float) h, provider);
		}
	}
}