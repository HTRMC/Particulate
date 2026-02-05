package com.dunnewortel.particulate.rendering;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public abstract class RectangularParticle extends Particle {
	protected float red = 1.0F;
	protected float green = 1.0F;
	protected float blue = 1.0F;
	protected float alpha = 1.0F;
	protected Sprite sprite;

	protected RectangularParticle(ClientWorld world, double x, double y, double z, Sprite sprite) {
		super(world, x, y, z);
		this.sprite = sprite;
	}

	protected RectangularParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, Sprite sprite) {
		super(world, x, y, z, velocityX, velocityY, velocityZ);
		this.sprite = sprite;
	}

	@Override
	public ParticleTextureSheet textureSheet() {
		return RectangularParticleSheets.RECTANGULAR_PARTICLES;
	}

	public abstract float getWidth(float tickProgress);

	public abstract float getHeight(float tickProgress);

	public abstract BillboardParticle.RenderType getRenderType();

	// Position accessors for renderer
	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

	public double getZ() {
		return this.z;
	}

	public double getLastX() {
		return this.lastX;
	}

	public double getLastY() {
		return this.lastY;
	}

	public double getLastZ() {
		return this.lastZ;
	}

	public void render(RectangularParticleSubmittable submittable, Camera camera, float tickProgress) {
		Vec3d cameraPos = camera.getCameraPos();
		float x = (float) (MathHelper.lerp(tickProgress, this.lastX, this.x) - cameraPos.getX());
		float y = (float) (MathHelper.lerp(tickProgress, this.lastY, this.y) - cameraPos.getY());
		float z = (float) (MathHelper.lerp(tickProgress, this.lastZ, this.z) - cameraPos.getZ());

		Quaternionf rotation = new Quaternionf();
		getRotator().setRotation(rotation, camera, tickProgress);

		float width = getWidth(tickProgress);
		float height = getHeight(tickProgress);

		// Compute the 4 corner positions after rotation
		// Vertex order: BL, BR, TR, TL (matches old code: a, b, b+h, a+h)
		Vector3f bl = new Vector3f(-width, -height, 0.0F).rotate(rotation).add(x, y, z);
		Vector3f br = new Vector3f(width, -height, 0.0F).rotate(rotation).add(x, y, z);
		Vector3f tr = new Vector3f(width, height, 0.0F).rotate(rotation).add(x, y, z);
		Vector3f tl = new Vector3f(-width, height, 0.0F).rotate(rotation).add(x, y, z);

		submittable.render(
			getRenderType(),
			bl.x(), bl.y(), bl.z(),
			br.x(), br.y(), br.z(),
			tr.x(), tr.y(), tr.z(),
			tl.x(), tl.y(), tl.z(),
			getMinU(), getMaxU(), getMinV(), getMaxV(),
			ColorHelper.fromFloats(alpha, red, green, blue),
			getBrightness(tickProgress)
		);
	}

	public Rotator getRotator() {
		return Rotator.ALL_AXIS;
	}

	public void updateSprite(SpriteProvider spriteProvider) {
		if (!this.dead) {
			this.setSprite(spriteProvider.getSprite(this.age, this.maxAge));
		}
	}

	protected void setSprite(Sprite sprite) {
		this.sprite = sprite;
	}

	protected float getMinU() {
		return sprite.getMinU();
	}

	protected float getMaxU() {
		return sprite.getMaxU();
	}

	protected float getMinV() {
		return sprite.getMinV();
	}

	protected float getMaxV() {
		return sprite.getMaxV();
	}

	public void setColor(float red, float green, float blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	protected void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	@Environment(EnvType.CLIENT)
	public interface Rotator {
		Rotator ALL_AXIS = (quaternion, camera, tickProgress) -> quaternion.set(camera.getRotation());
		Rotator Y_ONLY = (quaternion, camera, tickProgress) -> quaternion.set(0.0F, camera.getRotation().y, 0.0F, camera.getRotation().w);
		Rotator FIXED = (quaternion, camera, tickProgress) -> quaternion.identity();

		void setRotation(Quaternionf quaternion, Camera camera, float tickProgress);
	}
}