package fi.haju.ut2.ui.render.renderers;

import java.util.Set;

import javax.inject.Singleton;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Sets;
import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.ui.MeshUtils;
import fi.haju.ut2.voxels.octree.VoxelEdge;
import fi.haju.ut2.voxels.octree.VoxelOctree;

@Singleton
public class OctreeVertexNormalRenderer {
  
  private Node rootNode;
  private AssetManager assetManager;
  
  public void setup(Node rootNode, AssetManager assetManager) {
    this.rootNode = rootNode;
    this.assetManager = assetManager;
  }
  
  
  public void render(VoxelOctree root) {
    Set<Pair<Position, Position>> endpoints = Sets.newHashSet();
    for(VoxelEdge edge : root.edges()) {      
      if (edge.dividor ==  null && edge.vertex() != null) {
        endpoints.add(Pair.of(edge.vertex().position, edge.vertex().position.add(edge.vertex().normal.div(10.0))));
      }
    }
    rootNode.attachChild(MeshUtils.lines(endpoints, new ColorRGBA(1,0,0,1), assetManager));
  }
  
}
