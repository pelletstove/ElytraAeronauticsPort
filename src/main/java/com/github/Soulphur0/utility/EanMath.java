package com.github.Soulphur0.utility;

public class EanMath {
	/** The value at x on the straight line through (ax, ay) and (bx, by). */
	public static double getLinealValue(double pointAX, double pointAY, double pointBX, double pointBY, double valueToGetX) {
		double slope = (pointBY - pointAY) / (pointBX - pointAX);
		double intercept = pointBY - slope * pointBX;
		return slope * valueToGetX + intercept;
	}
}
