package fi.haju.ut2.ui.render.renderers;

import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.inject.Singleton;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.voxels.octree.OctreeComponent;
import fi.haju.ut2.voxels.octree.VoxelOctree;

@Singleton
public class OctreeSurfaceRenderer {
  
  private Node rootNode;
  private AssetManager assetManager;
  
  public void setup(Node rootNode, AssetManager assetManager) {
    this.rootNode = rootNode;
    this.assetManager = assetManager;
  }
    
  public void render(VoxelOctree root) {
    Set<OctreeComponent> components = Sets.newHashSet();
    Queue<VoxelOctree> tbp = Queues.newArrayDeque();
    tbp.add(root);
    while (!tbp.isEmpty()) {
      VoxelOctree octree = tbp.remove();
      if(octree.children == null) {
        components.addAll(octree.components);
      } else { 
        for(VoxelOctree child : octree.children) {
          if (child != null) {
            tbp.add(child);
          }
        }
      }
    }
    for(OctreeComponent component : components) {
      Position center = Position.average(component.vertices);
      Mesh m = new Mesh();
      List<Position> points = Lists.newArrayList();
      points.add(center);
      points.addAll(component.vertices);
      points.add(component.vertices.peekFirst());
      float[] data = new float[3*points.size()];
      int i = 0;
      for(Position p : points) {
        data[i++] = (float)p.x;
        data[i++] = (float)p.y;
        data[i++] = (float)p.z;
      }
      m.setMode(Mesh.Mode.TriangleFan);
      m.setBuffer(VertexBuffer.Type.Position, 3, data);
      Geometry surface = new Geometry("surface", m);
      Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
      mat.setBoolean("UseMaterialColors", true);
      mat.setColor("Ambient", new ColorRGBA(1, 1, 1, 1));
      mat.setColor("Diffuse",  new ColorRGBA(1, 1, 1, 1));
      surface.setMaterial(mat);
      rootNode.attachChild(surface);
    }
  }

}
