package fi.haju.ut2.ui.render.renderers;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

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
import fi.haju.ut2.voxels.octree.PositionWithNormal;
import fi.haju.ut2.voxels.octree.VoxelOctree;

@Singleton
public class OctreeSurfaceRenderer {
  
  private Logger log = Logger.getLogger(this.getClass().getName());
  private Node rootNode;
  private AssetManager assetManager;
  
  public void setup(Node rootNode, AssetManager assetManager) {
    this.rootNode = rootNode;
    this.assetManager = assetManager;
  }
    
  public void render(VoxelOctree root) {
    log.info("starting octree mesh construction");
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
      List<Position> positions = Lists.newArrayList();
      List<Position> normals = Lists.newArrayList();
      for(PositionWithNormal p : component.vertices) {
        positions.add(p.position);
        normals.add(p.normal);
      }
      Position center = Position.average(positions);
      Position centerNorm = Position.average(normals).normalize();
      Mesh m = new Mesh();
      List<Position> points = Lists.newArrayList();
      List<Position> norms = Lists.newArrayList();
      points.add(center);
      points.addAll(positions);
      points.add(component.vertices.peekFirst().position);
      norms.add(centerNorm);
      norms.addAll(normals);
      norms.add(component.vertices.peekFirst().normal);
      float[] positionData = new float[3*points.size()];
      float[] normalData = new float[3*points.size()];
      int i = 0;
      for(Position p : points) {
        positionData[i++] = (float)p.x;
        positionData[i++] = (float)p.y;
        positionData[i++] = (float)p.z;
      }
      i = 0;
      for(Position p : norms) {
        normalData[i++] = (float)p.x;
        normalData[i++] = (float)p.y;
        normalData[i++] = (float)p.z;
      }
      m.setMode(Mesh.Mode.TriangleFan);
      m.setBuffer(VertexBuffer.Type.Position, 3, positionData);
      m.setBuffer(VertexBuffer.Type.Normal, 3, normalData);
      m.updateBound();
      Geometry surface = new Geometry("surface", m);
      Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
      mat.setBoolean("UseMaterialColors", true);
      mat.setColor("Ambient", new ColorRGBA(1, 1, 1, 1));
      mat.setColor("Diffuse",  new ColorRGBA(1, 1, 1, 1));
      surface.setMaterial(mat);
      rootNode.attachChild(surface);
    }
    log.info("octree mesh construction done");
  }

}
