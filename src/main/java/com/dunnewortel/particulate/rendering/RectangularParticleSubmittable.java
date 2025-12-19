package com.dunnewortel.particulate.rendering;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.BillboardParticleSubmittable;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Submittable;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.command.LayeredCustomCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.BufferAllocator;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class RectangularParticleSubmittable implements OrderedRenderCommandQueue.LayeredCustom, Submittable {
	private static final int INITIAL_BUFFER_MAX_LENGTH = 1024;
	private static final int BUFFER_FLOAT_FIELDS = 16; // 4 corners (12 floats) + 4 UVs
	private static final int BUFFER_INT_FIELDS = 2;

	private final Map<BillboardParticle.RenderType, Vertices> bufferByType = new HashMap<>();
	private int particles;

	public void render(
		BillboardParticle.RenderType renderType,
		float x0, float y0, float z0,
		float x1, float y1, float z1,
		float x2, float y2, float z2,
		float x3, float y3, float z3,
		float minU, float maxU, float minV, float maxV,
		int color, int brightness
	) {
		bufferByType.computeIfAbsent(renderType, rt -> new Vertices())
			.vertex(x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3,
				minU, maxU, minV, maxV, color, brightness);
		particles++;
	}

	@Override
	public void onFrameEnd() {
		bufferByType.values().forEach(Vertices::reset);
		particles = 0;
	}

	@Nullable
	@Override
	public BillboardParticleSubmittable.Buffers submit(LayeredCustomCommandRenderer.VerticesCache cache) {
		int vertexCount = particles * 4;

		Object result;
		try (BufferAllocator bufferAllocator = BufferAllocator.fixedSized(
			vertexCount * VertexFormats.POSITION_TEXTURE_COLOR_LIGHT.getVertexSize())) {

			BufferBuilder bufferBuilder = new BufferBuilder(bufferAllocator,
				VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
			Map<BillboardParticle.RenderType, BillboardParticleSubmittable.Layer> layers = new HashMap<>();
			int vertexOffset = 0;

			for (var entry : bufferByType.entrySet()) {
				Vertices vertices = entry.getValue();
				vertices.render((x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, minU, maxU, minV, maxV, col, brightness) ->
					drawFace(bufferBuilder, x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, minU, maxU, minV, maxV, col, brightness)
				);

				if (vertices.nextVertexIndex() > 0) {
					layers.put(entry.getKey(), new BillboardParticleSubmittable.Layer(vertexOffset, vertices.nextVertexIndex() * 6));
				}
				vertexOffset += vertices.nextVertexIndex() * 4;
			}

			BuiltBuffer builtBuffer = bufferBuilder.endNullable();
			if (builtBuffer != null) {
				cache.write(builtBuffer.getBuffer());
				RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS)
					.getIndexBuffer(builtBuffer.getDrawParameters().indexCount());
				GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().write(
					RenderSystem.getModelViewMatrix(),
					new Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
					new Vector3f(),
					RenderSystem.getTextureMatrix(),
					RenderSystem.getShaderLineWidth()
				);
				return new BillboardParticleSubmittable.Buffers(builtBuffer.getDrawParameters().indexCount(), gpuBufferSlice, layers);
			}

			result = null;
		}

		return (BillboardParticleSubmittable.Buffers) result;
	}

	@Override
	public void render(BillboardParticleSubmittable.Buffers buffers, LayeredCustomCommandRenderer.VerticesCache cache,
	                   RenderPass renderPass, TextureManager manager, boolean translucent) {
		var shapeIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS);
		renderPass.setVertexBuffer(0, cache.get());
		renderPass.setIndexBuffer(shapeIndexBuffer.getIndexBuffer(buffers.indexCount()), shapeIndexBuffer.getIndexType());
		renderPass.setUniform("DynamicTransforms", buffers.dynamicTransforms());

		for (var entry : buffers.layers().entrySet()) {
			if (translucent == entry.getKey().translucent()) {
				renderPass.setPipeline(entry.getKey().pipeline());
				renderPass.bindSampler("Sampler0",
					manager.getTexture(entry.getKey().textureAtlasLocation()).getGlTextureView());
				renderPass.drawIndexed(entry.getValue().vertexOffset(), 0, entry.getValue().indexCount(), 1);
			}
		}
	}

	public void drawFace(
		VertexConsumer vertexConsumer,
		float x0, float y0, float z0,
		float x1, float y1, float z1,
		float x2, float y2, float z2,
		float x3, float y3, float z3,
		float minU, float maxU, float minV, float maxV,
		int color, int brightness
	) {
		// Vertex order: v0(bottom-left), v1(bottom-right), v2(top-right), v3(top-left)
		// UV mapping matches old working code: (minU,maxV), (maxU,maxV), (maxU,minV), (minU,minV)
		vertexConsumer.vertex(x0, y0, z0).texture(minU, maxV).color(color).light(brightness);
		vertexConsumer.vertex(x1, y1, z1).texture(maxU, maxV).color(color).light(brightness);
		vertexConsumer.vertex(x2, y2, z2).texture(maxU, minV).color(color).light(brightness);
		vertexConsumer.vertex(x3, y3, z3).texture(minU, minV).color(color).light(brightness);
	}

	@Override
	public void submit(OrderedRenderCommandQueue orderedRenderCommandQueue, CameraRenderState cameraRenderState) {
		if (particles > 0) {
			orderedRenderCommandQueue.submitCustom(this);
		}
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface Consumer {
		void consume(float x0, float y0, float z0,
		             float x1, float y1, float z1,
		             float x2, float y2, float z2,
		             float x3, float y3, float z3,
		             float minU, float maxU, float minV, float maxV,
		             int color, int brightness);
	}

	@Environment(EnvType.CLIENT)
	static class Vertices {
		private int maxVertices = INITIAL_BUFFER_MAX_LENGTH;
		private float[] floatData = new float[maxVertices * BUFFER_FLOAT_FIELDS];
		private int[] intData = new int[maxVertices * BUFFER_INT_FIELDS];
		private int nextVertexIndex;

		public void vertex(float x0, float y0, float z0,
		                   float x1, float y1, float z1,
		                   float x2, float y2, float z2,
		                   float x3, float y3, float z3,
		                   float minU, float maxU, float minV, float maxV,
		                   int color, int brightness) {
			if (nextVertexIndex >= maxVertices) {
				increaseCapacity();
			}

			int fi = nextVertexIndex * BUFFER_FLOAT_FIELDS;
			floatData[fi++] = x0;
			floatData[fi++] = y0;
			floatData[fi++] = z0;
			floatData[fi++] = x1;
			floatData[fi++] = y1;
			floatData[fi++] = z1;
			floatData[fi++] = x2;
			floatData[fi++] = y2;
			floatData[fi++] = z2;
			floatData[fi++] = x3;
			floatData[fi++] = y3;
			floatData[fi++] = z3;
			floatData[fi++] = minU;
			floatData[fi++] = maxU;
			floatData[fi++] = minV;
			floatData[fi] = maxV;

			int ii = nextVertexIndex * BUFFER_INT_FIELDS;
			intData[ii++] = color;
			intData[ii] = brightness;
			nextVertexIndex++;
		}

		public void render(Consumer consumer) {
			for (int i = 0; i < nextVertexIndex; i++) {
				int fi = i * BUFFER_FLOAT_FIELDS;
				int ii = i * BUFFER_INT_FIELDS;
				consumer.consume(
					floatData[fi], floatData[fi + 1], floatData[fi + 2],
					floatData[fi + 3], floatData[fi + 4], floatData[fi + 5],
					floatData[fi + 6], floatData[fi + 7], floatData[fi + 8],
					floatData[fi + 9], floatData[fi + 10], floatData[fi + 11],
					floatData[fi + 12], floatData[fi + 13], floatData[fi + 14], floatData[fi + 15],
					intData[ii], intData[ii + 1]
				);
			}
		}

		public void reset() {
			nextVertexIndex = 0;
		}

		private void increaseCapacity() {
			maxVertices *= 2;
			floatData = Arrays.copyOf(floatData, maxVertices * BUFFER_FLOAT_FIELDS);
			intData = Arrays.copyOf(intData, maxVertices * BUFFER_INT_FIELDS);
		}

		public int nextVertexIndex() {
			return nextVertexIndex;
		}
	}
}