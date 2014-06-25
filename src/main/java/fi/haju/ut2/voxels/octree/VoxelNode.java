package fi.haju.ut2.voxels.octree;

import java.util.Set;

import com.google.common.collect.Sets;

import fi.haju.ut2.geometry.Position;

public final class VoxelNode {
    public Position position;
    public Set<VoxelOctree> octrees = Sets.newHashSet();
    
    public VoxelNode(Position pos) {
      position = new Position(pos.x, pos.y, pos.z);
    }
    
    public static final VoxelNode node(Position pos) {
      return new VoxelNode(pos);
    }
    
}
