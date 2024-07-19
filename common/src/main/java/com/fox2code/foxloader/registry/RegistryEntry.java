package com.fox2code.foxloader.registry;

import org.jetbrains.annotations.ApiStatus;

public final class RegistryEntry {
    public final short realId, fallbackId;
    public final String name, fallbackDisplayName;

    @ApiStatus.Internal
    public RegistryEntry(short realId, short fallbackId, String name, String fallbackDisplayName) {
        this.realId = realId;
        this.fallbackId = fallbackId;
        this.name = name;
        this.fallbackDisplayName = fallbackDisplayName;
    }
}
