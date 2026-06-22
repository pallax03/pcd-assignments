package pcd.poool;

import pcd.poool.common.model.SmallBallsStrategy;
import pcd.poool.v1.ActiveController;
import pcd.poool.v1.MultithreadingCollisions;

public class V1 {

    public static void main(String[] argv) {
        GameLauncher.launch("V1 ~ ",
                SmallBallsStrategy.massive(),
                new MultithreadingCollisions(),
                ActiveController::new
        );
    }
}