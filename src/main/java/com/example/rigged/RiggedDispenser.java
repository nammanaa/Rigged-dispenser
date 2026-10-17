package com.example.rigged;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

import java.util.HashMap;
import java.util.Map;

public class RiggedDispenser implements ModInitializer {
    /** 0 = tắt, 1 = ưu tiên dispenser TRÁI, 2 = ưu tiên dispenser PHẢI */
    public static volatile int mode = 0;

    private record Pending(long tick, int slot) {}
    private static final Map<Long, Pending> PENDING = new HashMap<>();
    private static boolean busy = false;

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(ModePayload.ID, ModePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ModePayload.ID, (payload, context) -> {
            int m = payload.mode();
            mode = (mode == m) ? 0 : m;
            String msg = switch (mode) {
                case 1 -> "Rigged: TRÁI >= PHẢI (trái bắn slot thấp hơn)";
                case 2 -> "Rigged: PHẢI >= TRÁI (phải bắn slot thấp hơn)";
                default -> "Rigged: TẮT";
            };
            context.player().sendMessage(Text.literal(msg), true);
        });
    }

    public static int adjust(DispenserBlockEntity self, Random random, int own) {
        if (busy || mode == 0 || own < 0) return -1;
        if (!(self.getWorld() instanceof ServerWorld world)) return -1;

        BlockPos pos = self.getPos();
        Direction facing = self.getCachedState().get(DispenserBlock.FACING);
        if (facing.getAxis() == Direction.Axis.Y) return -1;

        Pending p = PENDING.remove(pos.asLong());
        if (p != null && p.tick() == world.getTime() && !self.getStack(p.slot()).isEmpty()) {
            return p.slot();
        }

        Direction leftDir = facing.rotateYCounterclockwise();
        Direction rightDir = facing.rotateYClockwise();

        boolean thisIsLeft;
        BlockPos otherPos;
        if (isPartner(world, pos.offset(leftDir), facing)) {
            thisIsLeft = false;
            otherPos = pos.offset(leftDir);
        } else if (isPartner(world, pos.offset(rightDir), facing)) {
            thisIsLeft = true;
            otherPos = pos.offset(rightDir);
        } else {
            return -1;
        }

        DispenserBlockEntity other = (DispenserBlockEntity) world.getBlockEntity(otherPos);
        int otherSlot;
        busy = true;
        try {
            otherSlot = other.chooseNonEmptySlot(random);
        } finally {
            busy = false;
        }
        if (otherSlot < 0) return -1;

        int low = Math.min(own, otherSlot);
        int high = Math.max(own, otherSlot);
        boolean favoredIsThis = (mode == 1) == thisIsLeft;
        int mine = favoredIsThis ? low : high;
        int theirs = favoredIsThis ? high : low;

        PENDING.put(otherPos.asLong(), new Pending(world.getTime(), theirs));
        return mine;
    }

    private static boolean isPartner(ServerWorld world, BlockPos pos, Direction facing) {
        BlockEntity be = world.getBlockEntity(pos);
        return be instanceof DispenserBlockEntity
                && world.getBlockState(pos).contains(DispenserBlock.FACING)
                && world.getBlockState(pos).get(DispenserBlock.FACING) == facing;
    }
}
