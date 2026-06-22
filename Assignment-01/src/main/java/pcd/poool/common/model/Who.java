package pcd.poool.common.model;

public enum Who {
    PLAYER, COM, ANY;

    public boolean notAny() {
        return this != Who.ANY;
    }
}
