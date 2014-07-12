package fi.haju.ut2.ui;

import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;

import fi.haju.ut2.geometry.Position;

public final class MeshUtils {
  
  public static Geometry makeSimpleMesh(Mesh mesh, ColorRGBA color, AssetManager assetManager) {
    Geometry characterModel = new Geometry("SimpleMesh", mesh);
    characterModel.setShadowMode(ShadowMode.CastAndReceive);
    Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
    mat.setBoolean("UseMaterialColors", true);
    mat.setColor("Ambient", color);
    mat.setColor("Diffuse", color);
    characterModel.setMaterial(mat);
    return characterModel;
  }
  
  public static Geometry lines(Set<Pair<Position, Position>> endpoints, ColorRGBA color, AssetManager assetManager) {
    Mesh m = new Mesh();
    m.setMode(Mesh.Mode.Lines);
    float[] posData = new float[endpoints.size()*6];
    short[] indexData = new short[endpoints.size()*2];
    int i = 0;
    for (Pair<Position, Position> p : endpoints) {
      posData[i] = (float)p.getLeft().x;
      posData[i+1] = (float)p.getLeft().y;
      posData[i+2] = (float)p.getLeft().z;
      posData[i+3] = (float)p.getRight().x;
      posData[i+4] = (float)p.getRight().y;
      posData[i+5] = (float)p.getRight().z;
      i += 6;
    }
    for (i = 0; i < endpoints.size()*2; ++i) {
      indexData[i] = (short)i;
    }
    m.setBuffer(VertexBuffer.Type.Position, 3, posData);
    m.setBuffer(VertexBuffer.Type.Index, 2, indexData);
    m.updateBound();
    Geometry line = new Geometry("Line", m);
    Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
    mat.setBoolean("UseMaterialColors", true);
    mat.setColor("Ambient", color);
    mat.setColor("Diffuse", color);
    line.setMaterial(mat);
    return line;
  }
  
  private MeshUtils() {}
  
}
