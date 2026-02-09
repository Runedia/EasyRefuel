package com.easyrefuel.region;

import com.easyrefuel.model.Position;
import net.minecraft.util.math.BlockPos;

public class RegionSelector {
    private static RegionSelector instance;
    private BlockPos pos1;
    private BlockPos pos2;

    private RegionSelector() {
        this.pos1 = null;
        this.pos2 = null;
    }

    public static RegionSelector getInstance() {
        if (instance == null) {
            instance = new RegionSelector();
        }
        return instance;
    }

    public void setPos1(BlockPos pos) {
        this.pos1 = pos;
    }

    public void setPos2(BlockPos pos) {
        this.pos2 = pos;
    }

    public BlockPos getPos1() {
        return pos1;
    }

    public BlockPos getPos2() {
        return pos2;
    }

    public boolean hasPos1() {
        return pos1 != null;
    }

    public boolean hasPos2() {
        return pos2 != null;
    }

    public boolean hasBothPositions() {
        return pos1 != null && pos2 != null;
    }

    public void reset() {
        pos1 = null;
        pos2 = null;
    }

    public Position convertToPosition(BlockPos pos) {
        return new Position(pos.getX(), pos.getY(), pos.getZ());
    }
}
