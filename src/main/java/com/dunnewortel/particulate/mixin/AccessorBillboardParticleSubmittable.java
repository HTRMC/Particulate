package com.dunnewortel.particulate.mixin;

import net.minecraft.client.particle.BillboardParticleSubmittable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BillboardParticleSubmittable.class)
public interface AccessorBillboardParticleSubmittable
{
	@Accessor("vertices")
	BillboardParticleSubmittable.Vertices getVertices();
}