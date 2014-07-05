package fi.haju.ut2.ui.render.octreegenerator;

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
import com.jme3.scene.VertexBuffer;

import fi.haju.ut2.voxels.octree.OctreeComponent;
import fi.haju.ut2.voxels.octree.PositionWithNormal;
import fi.haju.ut2.voxels.octree.VoxelOctree;

@Singleton
public class OctreeSurfaceGeometryGenerator implements OctreeGeometryGenerator {
  
  private Logger log = Logger.getLogger(this.getClass().getName());

  private Set<OctreeComponent> collectComponents(VoxelOctree root) {
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
    return components;
  }

  @Override
  public List<Geometry> generate(VoxelOctree octree, AssetManager assetManager) {
    log.info("starting octree mesh construction");
    Set<OctreeComponent> components = collectComponents(octree);
    Mesh m = new Mesh();
    int vertices = 0;
    int triangles = 0;
    for(OctreeComponent component : components) {
      vertices += component.vertices.size() + 1;
      triangles += component.vertices.size();
    }
    float[] positionData = new float[3*vertices];
    float[] normalData = new float[3*vertices];
    short[] indexData = new short[3*triangles];
    int ti = 0;
    int vi = 0;
    for(OctreeComponent component : components) {
      PositionWithNormal center = component.centralPoint;
      int centerIndex = vi/3;
      positionData[vi] = (float)center.position.x;
      positionData[vi+1] = (float)center.position.y;
      positionData[vi+2] = (float)center.position.z;
      normalData[vi] = (float)center.normal.x;
      normalData[vi+1] = (float)center.normal.y;
      normalData[vi+2] = (float)center.normal.z;
      vi += 3;
      for(PositionWithNormal pwn : component.vertices) {
        positionData[vi] = (float)pwn.position.x;
        positionData[vi+1] = (float)pwn.position.y;
        positionData[vi+2] = (float)pwn.position.z;
        normalData[vi] = (float)pwn.normal.x;
        normalData[vi+1] = (float)pwn.normal.y;
        normalData[vi+2] = (float)pwn.normal.z;
        vi += 3;
      }
      for (int i = 1; i < component.vertices.size(); ++i) {
        indexData[ti] = (short)centerIndex;
        indexData[ti+1] = (short)(centerIndex + i);
        indexData[ti+2] = (short)(centerIndex + i + 1);
        ti += 3;
      }
      indexData[ti] = (short)centerIndex;
      indexData[ti+1] = (short)(centerIndex + component.vertices.size());
      indexData[ti+2] = (short)(centerIndex + 1);
      ti += 3;
    }
    m.setMode(Mesh.Mode.Triangles);
    m.setBuffer(VertexBuffer.Type.Position, 3, positionData);
    m.setBuffer(VertexBuffer.Type.Normal, 3, normalData);
    m.setBuffer(VertexBuffer.Type.Index, 3, indexData);
    m.updateBound();
    Geometry surface = new Geometry("surface", m);
    Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
    mat.setBoolean("UseMaterialColors", true);
    mat.setColor("Ambient", new ColorRGBA(1, 1, 1, 1));
    mat.setColor("Diffuse",  new ColorRGBA(1, 1, 1, 1));
    surface.setMaterial(mat);
    log.info("octree mesh construction done");
    return Lists.newArrayList(surface);
  }

}
