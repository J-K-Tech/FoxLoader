package com.fox2code.foxloader.registry;

import org.jetbrains.annotations.ApiStatus;

public class EntityTypeRegistryEntry {
	public final int realId, fallbackId;
	public final String name;

	@ApiStatus.Internal
	public EntityTypeRegistryEntry(int realId, int fallbackId, String name) {
		this.realId = realId;
		this.fallbackId = fallbackId;
		this.name = name;
	}
}
