package pcd.poool;

import pcd.poool.common.model.SmallBallsStrategy;
import pcd.poool.v2.ExecutorCollisions;
import pcd.poool.v2.ExecutorController;

public class V2 {

    public static void main(String[] argv) {
        GameLauncher.launch("V2 ~ ",
                SmallBallsStrategy.massive(),
                new ExecutorCollisions(),
                ExecutorController::new
        );
    }
}
