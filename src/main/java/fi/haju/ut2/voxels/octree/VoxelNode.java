package fi.haju.ut2.voxels.octree;

import java.util.Set;

import com.google.common.collect.Sets;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.voxels.functions.Function3d;

public final class VoxelNode {
    public Position position;
    public Set<VoxelOctree> octrees = Sets.newHashSet();
    public boolean positive = false;
    
    public VoxelNode(Position pos, boolean positive) {
      position = new Position(pos.x, pos.y, pos.z);
      this.positive = positive;
    }
    
    public static final VoxelNode node(Position pos, Function3d function) {
      return new VoxelNode(pos, function.value(pos.x, pos.y, pos.z) > 0);
    }
    
}
