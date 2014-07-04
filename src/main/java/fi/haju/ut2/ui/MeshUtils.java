package fi.haju.ut2.ui;

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
  
  public static Geometry line(Position p1, Position p2, ColorRGBA color, AssetManager assetManager) {
    Mesh m = new Mesh();
    m.setMode(Mesh.Mode.Lines);
    m.setBuffer(VertexBuffer.Type.Position, 3, new float[]{ (float)p1.x, (float)p1.y, (float)p1.z, (float)p2.x, (float)p2.y, (float)p2.z});
    m.setBuffer(VertexBuffer.Type.Index, 2, new short[]{ 0, 1 });
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
