package com.dunnewortel.particulate.mixin;

import com.dunnewortel.particulate.Main;
import com.dunnewortel.particulate.Particles;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

@Mixin(Entity.class)
public abstract class InjectEntity
{
	@Shadow private Vec3d velocity;
	@Shadow private EntityDimensions dimensions;
	@Shadow @Final protected Random random;
	@Shadow private World world;

	@Unique
	public Queue<Double> velocities = new LinkedList<>();

	@Inject(
		method = "tick",
		at = @At("TAIL")
	)
	private void onSetVelocity(CallbackInfo ci)
	{
		if (!Main.CONFIG.waterSplash()) { return; }

		velocities.offer(Math.abs(velocity.getY()));
		if (velocities.size() > 4)
		{
			velocities.poll();
		}
	}

	@Inject(
		method = "onSwimmingStart",
		at = @At("TAIL"))
	private void waterParticles(CallbackInfo ci)
	{
		if (!Main.CONFIG.waterSplash()) { return; }

		//noinspection ConstantConditions
		if ((Object) this instanceof ArrowEntity || !(world instanceof ClientWorld)) { return; }

		Entity entity = (Entity)(Object)this;

		// Find water height
		float baseY = MathHelper.floor(entity.getY());

		boolean foundSurface = false;
		FluidState prevState = Fluids.EMPTY.getDefaultState();
		for (int i = 0; i < 5; ++i)
		{
			FluidState nextState = world.getFluidState(entity.getBlockPos().add(0, i, 0));
			if (prevState.isOf(Fluids.WATER) && nextState.isOf(Fluids.EMPTY))
			{
				baseY += i - 1;
				foundSurface = true;
				break;
			}

			prevState = nextState;
		}

		if (!foundSurface) { return; }

		// 3D splash
		double maxVelocity = velocities.isEmpty() ? 0.0 : Collections.max(velocities);
		world.addParticleClient(Particles.WATER_SPLASH_EMITTER, entity.getX(), baseY + prevState.getHeight(), entity.getZ(), dimensions.width(), maxVelocity, 0.0);
	}
}