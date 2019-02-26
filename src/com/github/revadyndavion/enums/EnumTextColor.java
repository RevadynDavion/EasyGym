package com.github.revadyndavion.enums;

import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

public enum EnumTextColor {
	AQUA(TextColors.AQUA),
	BLACK(TextColors.BLACK),
	BLUE(TextColors.BLUE),
	DARK_AQUA(TextColors.DARK_AQUA),
	DARK_BLUE(TextColors.DARK_BLUE),
	DARK_GRAY(TextColors.DARK_GRAY),
	DARK_GREEN(TextColors.DARK_GREEN),
	DARK_PURPLE(TextColors.DARK_PURPLE),
	DARK_RED(TextColors.DARK_RED),
	GOLD(TextColors.GOLD),
	GRAY(TextColors.GRAY),
	GREEN(TextColors.GREEN),
	LIGHT_PURPLE(TextColors.LIGHT_PURPLE),
	RED(TextColors.RED),
	WHITE(TextColors.WHITE),
	YELLOW(TextColors.YELLOW);
	
	private final TextColor textColor;
	private EnumTextColor(TextColor color) {
		this.textColor = color;
	}
	
	public TextColor get() {
		return this.textColor;
	}
}