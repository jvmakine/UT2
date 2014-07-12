package fi.haju.ut2.ui.render.octree;

import java.util.List;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Geometry;

import fi.haju.ut2.voxels.octree.VoxelOctree;

public interface OctreeGeometryGenerator {
  List<Geometry> generate(int renderLevel, VoxelOctree octree, AssetManager assetManager);
}
