package fi.haju.ut2.ui.render.modules;

import javax.inject.Singleton;

import com.jme3.asset.AssetManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;

@Singleton
public class SimpleLightingModule {
  
  private DirectionalLight directionalLight;
  private AmbientLight ambientLight;
  private DirectionalLightShadowRenderer dlsr;
  
  public SimpleLightingModule() {
    directionalLight = new DirectionalLight();
    directionalLight.setDirection(new Vector3f(1.0f, 1.0f, 1.0f).normalizeLocal());
    directionalLight.setColor(new ColorRGBA(1f, 1f, 1f, 1f).mult(1.0f));
    
    ambientLight = new AmbientLight();
    ambientLight.setColor(new ColorRGBA(1f, 1f, 1f, 1f).mult(0.6f));
  }
  
  
  public void setup(Node rootNode, AssetManager assetManager, ViewPort viewPort) {
    rootNode.addLight(directionalLight);
    rootNode.addLight(ambientLight);

    dlsr = new DirectionalLightShadowRenderer(assetManager, 2048, 4);
    dlsr.setLight(directionalLight);
    dlsr.setShadowIntensity(0.4f);
    dlsr.setEdgeFilteringMode(EdgeFilteringMode.PCF4);
    viewPort.addProcessor(dlsr);
  }
  
}
