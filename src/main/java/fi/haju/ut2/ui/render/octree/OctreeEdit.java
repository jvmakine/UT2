package fi.haju.ut2.ui.render.octree;

import com.jme3.math.Vector3f;
import com.jme3.math.Quaternion;
import com.jme3.scene.Mesh;

public class OctreeEdit {
  public final Mesh mesh;
  public final Vector3f location;
  public final Quaternion rotation;
  public final boolean delete;
  
  public OctreeEdit(Mesh mesh, Vector3f location, Quaternion rotation, boolean delete) {
    this.mesh = mesh;
    this.location = location;
    this.rotation = rotation;
    this.delete = delete;
  }
  
}
