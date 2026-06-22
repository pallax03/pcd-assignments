package pcd.poool;

import pcd.poool.common.control.Controller;
import pcd.poool.common.model.*;
import pcd.poool.common.view.View;
import pcd.poool.common.view.ViewModel;

import java.util.function.Function;

public class GameLauncher {

    public static void launch(String versionTitle, SmallBallsStrategy strategy, CollisionResolver resolver, Function<Logics, Controller> controllerFactory) {
        var conf = new BoardConf();

        var gameLogics = new LogicsImpl(
                new Board(
                        conf,
                        strategy,
                        resolver
                )
        );
        
        var controller = controllerFactory.apply(gameLogics);
        String title = versionTitle + controller.getClass().getSimpleName() + ": " + strategy + " Conf | " + resolver.getClass().getSimpleName();
        var viewModel = new ViewModel();
        viewModel.update(gameLogics, 0);
        
        var view = new View(title, conf, viewModel, controller);
        gameLogics.addObserver(view);

        controller.start();
        view.display();
    }
}