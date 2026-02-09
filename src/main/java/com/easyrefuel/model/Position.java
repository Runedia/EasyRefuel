package com.easyrefuel.model;

import com.google.gson.annotations.SerializedName;

public class Position {
    @SerializedName("x")
    private final int x;

    @SerializedName("y")
    private final int y;

    @SerializedName("z")
    private final int z;

    public Position(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d, %d)", x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Position)) return false;
        Position other = (Position) obj;
        return x == other.x && y == other.y && z == other.z;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * x + y) + z;
    }
}
