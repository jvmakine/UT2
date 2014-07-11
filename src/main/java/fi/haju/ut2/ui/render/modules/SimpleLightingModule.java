package fi.haju.ut2.ui.render.modules;

import javax.inject.Singleton;

import com.jme3.asset.AssetManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;

@Singleton
public class SimpleLightingModule {
  
  private DirectionalLight directionalLight;
  private AmbientLight ambientLight;
  
  public SimpleLightingModule() {
    directionalLight = new DirectionalLight();
    directionalLight.setDirection(new Vector3f(1.0f, -1.0f, 1.0f).normalizeLocal());
    directionalLight.setColor(new ColorRGBA(1.0f, 0.9f, 0.8f, 1f).mult(1.0f));
    
    ambientLight = new AmbientLight();
    ambientLight.setColor(new ColorRGBA(0.8f, 0.8f, 1.0f, 1f).mult(0.5f));
  }
  
  
  public void setup(Node rootNode, AssetManager assetManager, ViewPort viewPort) {
    rootNode.addLight(directionalLight);
    rootNode.addLight(ambientLight);    
  }
  
}
