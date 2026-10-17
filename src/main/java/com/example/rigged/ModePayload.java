package com.example.rigged;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ModePayload(int mode) implements CustomPayload {
    public static final Id<ModePayload> ID = new Id<>(Identifier.of("riggeddispenser", "mode"));
    public static final PacketCodec<RegistryByteBuf, ModePayload> CODEC =
            PacketCodec.tuple(PacketCodecs.VAR_INT, ModePayload::mode, ModePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
