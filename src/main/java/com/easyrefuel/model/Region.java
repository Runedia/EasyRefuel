package com.easyrefuel.model;

import com.google.gson.annotations.SerializedName;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class Region {
    @SerializedName("name")
    private String name;

    @SerializedName("pos1")
    private Position pos1;

    @SerializedName("pos2")
    private Position pos2;

    @SerializedName("fuel_type")
    private String fuelType;

    @SerializedName("enabled")
    private boolean enabled;

    public Region(String name, Position pos1, Position pos2, String fuelType) {
        this.name = name;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.fuelType = fuelType;
        this.enabled = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Position getPos1() {
        return pos1;
    }

    public Position getPos2() {
        return pos2;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMinX() {
        return Math.min(pos1.getX(), pos2.getX());
    }

    public int getMinY() {
        return Math.min(pos1.getY(), pos2.getY());
    }

    public int getMinZ() {
        return Math.min(pos1.getZ(), pos2.getZ());
    }

    public int getMaxX() {
        return Math.max(pos1.getX(), pos2.getX());
    }

    public int getMaxY() {
        return Math.max(pos1.getY(), pos2.getY());
    }

    public int getMaxZ() {
        return Math.max(pos1.getZ(), pos2.getZ());
    }

    public boolean containsPosition(BlockPos pos) {
        return pos.getX() >= getMinX() && pos.getX() <= getMaxX() &&
               pos.getY() >= getMinY() && pos.getY() <= getMaxY() &&
               pos.getZ() >= getMinZ() && pos.getZ() <= getMaxZ();
    }

    public List<BlockPos> getAllPositions() {
        List<BlockPos> positions = new ArrayList<>();
        for (int x = getMinX(); x <= getMaxX(); x++) {
            for (int y = getMinY(); y <= getMaxY(); y++) {
                for (int z = getMinZ(); z <= getMaxZ(); z++) {
                    positions.add(new BlockPos(x, y, z));
                }
            }
        }
        return positions;
    }

    public int getVolume() {
        return (getMaxX() - getMinX() + 1) *
               (getMaxY() - getMinY() + 1) *
               (getMaxZ() - getMinZ() + 1);
    }

    @Override
    public String toString() {
        return String.format("%s [%s ~ %s] (%s)", name, pos1, pos2, enabled ? "enabled" : "disabled");
    }
}
