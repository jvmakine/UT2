package fi.haju.ut2.voxels.octree;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.voxels.functions.Function3d;

public final class VoxelNode {
    public Position position;
    public Position normal;
    public boolean positive = false;
    
    public VoxelNode(Position pos, Function3d function) {
      position = new Position(pos.x, pos.y, pos.z);
      this.positive = function.value(pos.x, pos.y, pos.z) >= 0;
      this.normal = function.gradient(pos.x, pos.y, pos.z);
    }
    
    public static final VoxelNode node(Position pos, Function3d function) {
      return new VoxelNode(pos, function);
    }
    
}
