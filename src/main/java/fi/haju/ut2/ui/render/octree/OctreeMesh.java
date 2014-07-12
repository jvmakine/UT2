package fi.haju.ut2.ui.render.octree;

import java.util.List;

import com.jme3.scene.Geometry;

import fi.haju.ut2.voxels.octree.VoxelOctree;

public class OctreeMesh {

  public OctreeMesh(VoxelOctree octree, List<Geometry> geometries, int depth) {
    this.octree = octree;
    this.renderLevel = depth;
    this.geometries = geometries;
  }
  
  public final VoxelOctree octree;
  public final int renderLevel;
  public final List<Geometry> geometries;
  
}
