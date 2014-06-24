package fi.haju.ut2.runners;

import com.google.inject.Guice;
import com.google.inject.Injector;

import fi.haju.ut2.conf.GameModule;
import fi.haju.ut2.ui.Game;

/**
 * Class to start the client
 */
public class ClientRunner {

  public static void main(String[] args) throws Exception {
    Injector injector = Guice.createInjector(new GameModule());
    Game game = injector.getInstance(Game.class);
    game.start();
  }

}
