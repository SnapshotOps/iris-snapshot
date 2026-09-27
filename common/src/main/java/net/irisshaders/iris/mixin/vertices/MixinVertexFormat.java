package net.irisshaders.iris.mixin.vertices;

import com.google.common.collect.ImmutableSet;
import net.irisshaders.iris.gl.state.GlStateManager;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.irisshaders.iris.pipeline.programs.VertexFormatExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(VertexFormat.class)
public abstract class MixinVertexFormat implements VertexFormatExtension {
	@Shadow
	public abstract List<VertexFormatElement> getElements();

	@Unique
	private static final ImmutableSet<String> ATTRIBUTE_LIST = ImmutableSet.of("Position", "Color", "Normal", "UV0", "UV1", "UV2", "UV3", "LineWidth");

	@Override
	public void bindAttributesIris(boolean isFallback, int i) {
		int j = 0;

		for (VertexFormatElement x : this.getElements()) {
			var string = x.name();
			GlStateManager._glBindAttribLocation(i, j, ATTRIBUTE_LIST.contains(string) && !isFallback ? "iris_" + string : string);
			j++;
		}
	}
}
