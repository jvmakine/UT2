package fi.haju.ut2.ui.input;

import com.google.inject.Singleton;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.MouseButtonTrigger;

@Singleton
public class InputController {

  public static final String EDIT_ADD = "edit-add";
  public static final String EDIT_DELETE = "edit-delete";
  
  public void setup(InputManager inputManager) {
    inputManager.addMapping(EDIT_ADD, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
    inputManager.addMapping(EDIT_DELETE, new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
  }
  
}
