package pcd.poool.common.control;

import pcd.poool.common.model.Logics;

@FunctionalInterface
public interface GameCommand {

    void execute(Logics logics);
}
