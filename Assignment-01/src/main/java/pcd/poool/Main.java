package pcd.poool;

import pcd.poool.common.model.SequentialCollisions;
import pcd.poool.common.model.SmallBallsStrategy;
import pcd.poool.v1.ActiveController;

public class Main {

    public static void main(String[] argv) {
        GameLauncher.launch("V1 ~ ",
                SmallBallsStrategy.massive(),
                new SequentialCollisions(),
                ActiveController::new
        );
    }
}