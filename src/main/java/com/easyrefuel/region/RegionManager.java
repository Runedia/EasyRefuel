package com.easyrefuel.region;

import com.easyrefuel.model.Region;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RegionManager {
    private static RegionManager instance;
    private final List<Region> regions;

    private RegionManager() {
        this.regions = new ArrayList<>();
    }

    public static RegionManager getInstance() {
        if (instance == null) {
            instance = new RegionManager();
        }
        return instance;
    }

    public void addRegion(Region region) {
        regions.add(region);
    }

    public boolean removeRegion(String name) {
        return regions.removeIf(region -> region.getName().equals(name));
    }

    public Optional<Region> getRegion(String name) {
        return regions.stream()
                .filter(region -> region.getName().equals(name))
                .findFirst();
    }

    public List<Region> getAllRegions() {
        return new ArrayList<>(regions);
    }

    public List<Region> getEnabledRegions() {
        return regions.stream()
                .filter(Region::isEnabled)
                .toList();
    }

    public Optional<Region> getRegionAt(BlockPos pos) {
        return regions.stream()
                .filter(region -> region.containsPosition(pos))
                .findFirst();
    }

    public boolean hasRegion(String name) {
        return regions.stream()
                .anyMatch(region -> region.getName().equals(name));
    }

    public void clearRegions() {
        regions.clear();
    }

    public int getRegionCount() {
        return regions.size();
    }

    public void setRegions(List<Region> newRegions) {
        regions.clear();
        regions.addAll(newRegions);
    }
}
