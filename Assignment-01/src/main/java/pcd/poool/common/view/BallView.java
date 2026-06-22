package pcd.poool.common.view;

import pcd.poool.common.model.Who;
import pcd.poool.common.util.P2d;

public record BallView(P2d pos, double radius, Who hitBy) {}
