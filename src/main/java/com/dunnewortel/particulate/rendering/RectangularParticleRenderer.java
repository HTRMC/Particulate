package com.dunnewortel.particulate.rendering;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.Submittable;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;

@Environment(EnvType.CLIENT)
public class RectangularParticleRenderer extends ParticleRenderer<RectangularParticle> {
	final RectangularParticleSubmittable submittable = new RectangularParticleSubmittable();

	public RectangularParticleRenderer(ParticleManager manager) {
		super(manager);
	}

	@Override
	public Submittable render(Frustum frustum, Camera camera, float tickProgress) {
		for (RectangularParticle particle : this.particles) {
			if (frustum.intersectPoint(particle.getX(), particle.getY(), particle.getZ())) {
				try {
					particle.render(this.submittable, camera, tickProgress);
				} catch (Throwable t) {
					CrashReport crashReport = CrashReport.create(t, "Rendering Rectangular Particle");
					CrashReportSection section = crashReport.addElement("Particle being rendered");
					section.add("Particle", particle::toString);
					throw new CrashException(crashReport);
				}
			}
		}
		return this.submittable;
	}
}