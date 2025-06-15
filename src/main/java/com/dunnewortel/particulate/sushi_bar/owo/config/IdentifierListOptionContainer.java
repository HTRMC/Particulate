package com.dunnewortel.particulate.sushi_bar.owo.config;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.config.ui.component.ListOptionContainer;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import io.wispforest.owo.ui.util.UISounds;
import io.wispforest.owo.util.ReflectionUtils;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class IdentifierListOptionContainer extends ListOptionContainer
{
	public IdentifierListOptionContainer(Option option)
	{
		super(option);
	}

	@Override
	@SuppressWarnings({"unchecked", "ConstantConditions"})
	protected void refreshOptions()
	{
		collapsibleChildren.clear();

		var listType = ReflectionUtils.getTypeArgument(backingOption.backingField().field().getGenericType(), 0);
		for (int i = 0; i < backingList.size(); i++)
		{
			var container = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
			container.verticalAlignment(VerticalAlignment.CENTER);

			int optionIndex = i;
			final var label = Components.label(TextOps.withFormatting("- ", Formatting.GRAY));
			label.margins(Insets.left(10));
			if (!backingOption.detached())
			{
				label.cursorStyle(CursorStyle.HAND);
				label.mouseEnter().subscribe(() -> label.text(TextOps.withFormatting("x ", Formatting.GRAY)));
				label.mouseLeave().subscribe(() -> label.text(TextOps.withFormatting("- ", Formatting.GRAY)));
				label.mouseDown().subscribe((mouseX, mouseY, button) -> {
					backingList.remove(optionIndex);
					refreshResetButton();
					refreshOptions();
					UISounds.playInteractionSound();

					return true;
				});
			}
			container.child(label);

			final var box = new ConfigTextBox();
			box.setText(backingList.get(i).toString());
			box.setCursorToStart(false);
			box.setDrawsBackground(false);
			box.margins(Insets.vertical(2));
			box.horizontalSizing(Sizing.fill(95));
			box.verticalSizing(Sizing.fixed(8));
			box.inputPredicate(s -> s.matches("[a-z0-9_.:\\-]*"));
			box.applyPredicate(s -> Identifier.tryParse(s) != null);
			box.valueParser(Identifier::of);

			if (!backingOption.detached())
			{
				box.onChanged().subscribe(s -> {
					if (!box.isValid()) { return; }

					backingList.set(optionIndex, box.parsedValue());
					refreshResetButton();
				});
			}
			else
			{
				box.active = false;
			}

			container.child(box);
			collapsibleChildren.add(container);
		}

		contentLayout.<FlowLayout>configure(layout -> {
			layout.clearChildren();
			if (expanded) { layout.children(collapsibleChildren); }
		});
		refreshResetButton();
	}
}