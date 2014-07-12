package fi.haju.ut2.voxels.octree;

import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;

import fi.haju.ut2.geometry.Position;
import fi.haju.ut2.voxels.functions.Function3d;
import fi.haju.ut2.voxels.functions.NegativeFunction;
import fi.haju.ut2.voxels.octree.utils.OctreeConstructionUtils;

/**
 * Restricted octree, the depth of neighbors may differ only by one
 * The faces of octree and edges of faces are indexed as described in voxel_cube.png
 */
public final class VoxelOctree {

  public VoxelFace[] faces = new VoxelFace[6];
  public VoxelOctree[] children;
  public VoxelNode dividor;
  public VoxelOctree parent;
  public Position vertexPosition = null;
  public Function3d function;
  public List<OctreeComponent> components;
  public int depth;
  public boolean edited = false;
  
  public VoxelOctree() { }
  
  public VoxelOctree(Position upperLeftBackCorner, double side, Function3d function) {
    this.function = function;
    depth = 0;
    faces = OctreeConstructionUtils.createInitialFaces(upperLeftBackCorner, side, function);
    setupFaces();
  }
  
  public Position center() {
    return Position.average(faces[0].edges[0].minus.position, faces[5].edges[2].plus.position);
  }
  
  public List<VoxelOctree> treesInSphere(Position center, double radius, int level) {
    VoxelOctree tree = findFirstParentTreeContainingSphere(center, radius);
    Queue<VoxelOctree> tbp = Queues.newArrayDeque();
    tbp.add(tree);
    List<VoxelOctree> result = Lists.newArrayList();
    while (!tbp.isEmpty()) {
      VoxelOctree cell = tbp.remove();
      if (cell.depth >= level) {
        result.add(cell);
        continue;
      }
      if (cell.children == null) cell.divide();
      for (int i = 0; i < 8; ++i) {
        if (cell.children[i].overlapsSphere(center, radius)) {
          tbp.add(cell.children[i]);
        }
      }
    }
    return result;
  }
  
  public VoxelOctree findSmallestTreeContainingSphere(Position center, double radius) {
    Queue<VoxelOctree> tbp = Queues.newArrayDeque();
    tbp.add(findFirstParentTreeContainingSphere(center, radius));
    while (!tbp.isEmpty()) {
      VoxelOctree tree = tbp.remove();
      VoxelOctree child = null;
      if (tree.children == null) tree.divide();
      for (int i = 0; i < 8; ++i) {
        if (tree.children[i].overlapsSphere(center, radius)) {
          if (child == null) child = tree.children[i];
          else return tree;
        }
      }
      tbp.add(child);
    }
    throw new IllegalStateException();
  }
  
  public VoxelOctree findFirstParentTreeContainingSphere(Position center, double radius) {
    VoxelOctree tree = this;
    while (true) {
      if (tree.isSphereInside(center, radius)) return tree;
      if (tree.parent != null) {
        tree = tree.parent;
      } else {
        double xm = tree.faces[1].edges[0].minus.position.x;
        double xp = tree.faces[3].edges[0].minus.position.x;
        double ym = tree.faces[0].edges[0].minus.position.y;
        double yp = tree.faces[5].edges[0].minus.position.y;
        double zm = tree.faces[2].edges[0].minus.position.z;
        double zp = tree.faces[4].edges[0].minus.position.z;
        
        if (center.x - radius < xm) { tree.parent = tree.generateOctreeWithChild(6); tree = tree.parent; }  
        else if (center.x + radius > xp) { tree.parent = tree.generateOctreeWithChild(0); tree = tree.parent; }
        else if (center.y - radius < ym) { tree.parent = tree.generateOctreeWithChild(6); tree = tree.parent; }
        else if (center.y + radius > yp) { tree.parent = tree.generateOctreeWithChild(0); tree = tree.parent; }
        else if (center.z - radius < zm) { tree.parent = tree.generateOctreeWithChild(6); tree = tree.parent; }
        else if (center.z + radius > zp) { tree.parent = tree.generateOctreeWithChild(0); tree = tree.parent; }
        else throw new IllegalStateException();
      }
    }
  }

  public boolean overlapsSphere(Position center, double radius) {
    Position c1 = faces[0].edges[0].minus.position;
    Position c2 = faces[4].edges[2].plus.position;
    double dist_squared = radius * radius;
    if (center.x < c1.x) dist_squared -= (center.x - c1.x)*(center.x - c1.x);
    else if (center.x > c2.x) dist_squared -= (center.x - c2.x)*(center.x - c2.x);
    if (center.y < c1.y) dist_squared -= (center.y - c1.y)*(center.y - c1.y);
    else if (center.y > c2.y) dist_squared -= (center.y - c2.y)*(center.y - c2.y);
    if (center.z < c1.z) dist_squared -= (center.z - c1.z)*(center.z - c1.z);
    else if (center.z > c2.z) dist_squared -= (center.z - c2.z)*(center.z - c2.z);
    return dist_squared > 0;
  }
  
  private boolean isSphereInside(Position center, double radius) {
    double xm = faces[1].edges[0].minus.position.x;
    double xp = faces[3].edges[0].minus.position.x;
    double ym = faces[0].edges[0].minus.position.y;
    double yp = faces[5].edges[0].minus.position.y;
    double zm = faces[2].edges[0].minus.position.z;
    double zp = faces[4].edges[0].minus.position.z;
    
    if (center.x - radius <= xm || center.x + radius >= xp) return false;
    if (center.y - radius <= ym || center.y + radius >= yp) return false;
    if (center.z - radius <= zm || center.z + radius >= zp) return false;
    return true;
  }

  public void compress() {
   if (children == null) return;
    if (!hasInternalFeatures()) {
      children = null;
      dividor = null;
      for(int i = 0; i < 6; ++i) {
        VoxelOctree n = faces[i].other(this);
        if ((n == null || n.children == null) && !faces[i].hasFeature()) {
          faces[i].children = null;
          faces[i].dividor = null;
        }
      }
    } else {
      for (int i = 0; i < 8; ++i) {
        children[i].compress();
      }
    }
  }

  public boolean hasEdgeFeatures() {
    for (int i = 0; i < 6; ++i) {
      if (faces[i].hasFeature()) return true;
    }
    return false;
  }
  
  private boolean hasInternalFeatures() {
    if(children[0].faces[5].edges[1].vertex() != null) return true;
    if(children[0].faces[5].edges[2].vertex() != null) return true;
    if(children[1].faces[5].edges[2].vertex() != null) return true;
    if(children[3].faces[5].edges[1].vertex() != null) return true;
    if(children[0].faces[3].edges[1].vertex() != null) return true;
    if(children[4].faces[3].edges[1].vertex() != null) return true;
    return false;
  }
  
  public void divideAllToLevel(int level) {
    Queue<VoxelOctree> tbp = Queues.newArrayDeque();
    tbp.add(this);
    while(!tbp.isEmpty()) {
      VoxelOctree tree = tbp.remove();
      if (tree.edited) continue;
      if(tree.depth >= level) continue;
      if (tree.children == null) tree.divide();
      for(int i = 0; i < 8; ++i) {
        tbp.add(tree.children[i]);
      }
    }
  }
  
  public void setupFaces() {
    faces[0].plus = this;
    faces[1].plus = this;
    faces[2].plus = this;
    faces[3].minus = this;
    faces[4].minus = this;
    faces[5].minus = this;
  }
  
  public void divide() {
    if (edited) return;
    if (children != null) return;
    for (VoxelFace face : faces) face.divide(function);
    dividor = node(center());
    children = OctreeConstructionUtils.constructChildren(dividor, faces, function);
    for (int i = 0; i < 8; ++i) {
      children[i].depth = depth + 1;
      children[i].parent = this;
      children[i].function = function;
      children[i].setupFaces();
    }
  }
  
  public VoxelOctree getOctreeAtPosition(Position p, int depth) {
    if(isInside(p)) {
      if (depth <= this.depth) return this;
      if (children == null) {
        divide();
      }
      for(int i = 0; i < 8; ++i) {
        if (children[i].isInside(p)) {
          return children[i].getOctreeAtPosition(p, depth);
        }
      }
      throw new IllegalStateException();
    } else {
      if (parent != null) {
        return parent.getOctreeAtPosition(p, depth);
      } else {
        int index = OctreeConstructionUtils.getParentsChildIndex(p, faces);
        this.parent = generateOctreeWithChild(index);
        return parent.getOctreeAtPosition(p, depth);
      }
    }
  }

  public VoxelOctree generateOctreeWithChild(int index) {
    return OctreeConstructionUtils.generateOctreeWithChild(index, faces, function, this);
  }

  private final boolean isInside(Position p) {
    return faces[0].edges[0].minus.position.y <= p.y 
        && faces[5].edges[0].minus.position.y >= p.y
        && faces[1].edges[0].minus.position.x <= p.x
        && faces[3].edges[0].minus.position.x >= p.x
        && faces[2].edges[0].minus.position.z <= p.z
        && faces[4].edges[0].minus.position.z >= p.z;
  }
  
  public void calculateComponents() {
    Set<FaceSegment> segments = Sets.newHashSet();
    for (VoxelFace face : faces) {
      segments.addAll(face.getMostDetailedSegments());
    }
    components = OctreeConstructionUtils.createComponentsFromSegments(segments);
    if (children != null) {
      for(int i = 0; i < 8; ++i) {
        children[i].calculateComponents();
      }
    }
  }
  
  private final VoxelNode node(Position pos) {
    return VoxelNode.node(pos, function);
  }
  
  public Set<VoxelEdge> edges() {
    Set<VoxelEdge> result = Sets.newHashSet();
    for (VoxelFace face : faces) {
      for (int i = 0; i < 4; ++i) result.add(face.edges[i]);
    }
    if (children != null) {
      for(int i = 0; i < 8; ++i) {
        result.addAll(children[i].edges());
      }
    }
    return result;
  }
  
  public List<VoxelOctree> neighbours() {
    return Lists.newArrayList(
        faces[0].minus(),
        faces[1].minus(),
        faces[2].minus(),
        faces[3].plus(),
        faces[4].plus(),
        faces[5].plus()
    );
  }
  
  public VoxelOctree up() { return faces[0].minus(); }
  public VoxelOctree west() { return faces[1].minus(); }
  public VoxelOctree north() { return faces[2].minus(); }
  public VoxelOctree east() { return faces[3].plus(); }
  public VoxelOctree south() { return faces[4].plus(); }
  public VoxelOctree down() { return faces[5].plus(); }

  public double edgeLength() {
    return faces[0].edges[0].edgeVector().length();
  }

  public VoxelOctree copyTopLevel() {
    VoxelOctree copy = new VoxelOctree(faces[0].edges[0].minus.position, faces[0].edges[0].edgeVector().length(), function);
    copy.depth = depth;
    return copy;
  }

  public List<VoxelNode> corners() {
    return Lists.newArrayList(
      faces[0].edges[0].minus,
      faces[0].edges[0].plus,
      faces[0].edges[2].minus,
      faces[0].edges[2].plus,
      faces[5].edges[0].minus,
      faces[5].edges[0].plus,
      faces[5].edges[2].minus,
      faces[5].edges[2].plus
    );
  }
  
  public void constructFromMeshToLevel(Geometry g, int level, Position center, double radius) {
    children = null;
    function = new NegativeFunction();
    if (depth >= level) {
      Set<VoxelEdge> edges = Sets.newHashSet();
      for (int i = 0; i < 4; ++i) {
        edges.add(faces[0].edges[i]);
        edges.add(faces[5].edges[i]);
        edges.add(faces[1].edges[i]);
        edges.add(faces[3].edges[i]);
      }
      for (VoxelEdge e : edges) {
        if (isInsideMesh(e.minus.position, g)) {
          e.minus.positive = true;
        }
        if (isInsideMesh(e.plus.position, g)) {
          e.plus.positive = true;
        }
      }
      for (VoxelEdge e : edges) {
        if (e.minus.positive != e.plus.positive) {
          Position direction = Position.substract(e.plus.position, e.minus.position).normalize(); 
          Ray ray = new Ray(convert(e.minus.position), convert(direction));
          CollisionResults collision = new CollisionResults();
          g.collideWith(ray, collision); 
          CollisionResult cr = collision.getClosestCollision();
          // FIXME: should never be null
          if (cr == null) continue;
          e.vertex = new PositionWithNormal(convert(cr.getContactPoint()), convert(cr.getContactNormal()));
        }
      }
    }
    if (depth < level) {
      divide();
      for (int i = 0; i < 8; ++i) {
        if (children[i].overlapsSphere(center, radius)) {
          children[i].constructFromMeshToLevel(g, level, center, radius);
        }
      }
    } else {
      edited = true;
    }
  }

  private static Position convert(Vector3f v) {
    return new Position(v.x, v.y, v.z);
  }

  private static Vector3f convert(Position p) {
    return new Vector3f((float)p.x, (float)(p.y), (float)p.z);
  }
  
  private static boolean isInsideMesh(Position position, Geometry g) {
    Ray ray = new Ray(convert(position), new Vector3f(0,1,0));
    CollisionResults collision = new CollisionResults();
    int numCollisions = g.collideWith(ray, collision); 
    return numCollisions % 2 != 0;
  }

  public List<VoxelOctree> getLevelAffected(int level) {
    if (depth == level) return Lists.newArrayList(this);
    if (depth > level) return parent.getLevelAffected(level);
    List<VoxelOctree> result = Lists.newArrayList();
    if (children != null) {
      for (int i = 0; i < 8; ++i) {
        result.addAll(children[i].getLevelAffected(level));
      }
    }
    return result;
  }

  public void mergeWith(VoxelOctree editTree) {
    if (this.depth != editTree.depth) throw new IllegalStateException();
    if (editTree.edited) this.edited = true;
    for (int i = 0; i < 6; ++i) {
      mergeFaces(faces[i], editTree.faces[i]);
    }
    if (editTree.children != null) {
      if (children == null) divide();
      for (int i = 0; i < 8; ++i) {
        children[i].mergeWith(editTree.children[i]);
      }
    }
  }

  private static void mergeFaces(VoxelFace f1, VoxelFace f2) {
    for (int i = 0; i < 4; ++i) {
      VoxelEdge e1 = f1.edges[i];
      VoxelEdge e2 = f2.edges[i];
      e1.minus.positive |= e2.minus.positive;
      e1.plus.positive |= e2.plus.positive;
      if (e1.minus.positive == e1.plus.positive) {
        e1.vertex = null;
      } else {
        if (e2.vertex != null) {
          e1.vertex = e2.vertex;
        }
      }
    }
  }
  
}

