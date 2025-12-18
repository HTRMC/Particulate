package com.dunnewortel.particulate.mixin;

import net.minecraft.client.particle.BillboardParticleSubmittable;
import net.minecraft.client.render.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BillboardParticleSubmittable.Vertices.class)
public interface AccessorBillboardParticleSubmittableVertices
{
	@Accessor("vertices")
	VertexConsumer getVertices();
}