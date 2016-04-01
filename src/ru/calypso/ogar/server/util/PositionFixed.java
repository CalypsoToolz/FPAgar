package ru.calypso.ogar.server.util;

import ru.calypso.ogar.server.config.Config;

/**
 * @autor Calypso - Freya Project team
 */

public class PositionFixed {
	public double x, y, angle;
	public PositionFixed(double x, double y, double angle)
	{
		double r = 40;
        if ((x - r) < Config.WorldConfig.left) {
            angle = 6.28 - angle;
            x = Config.WorldConfig.left + r;
        }
        if ((x + r) > Config.WorldConfig.right) {
            angle = 6.28 - angle;
            x = Config.WorldConfig.right - r;
        }
        if ((y - r) < Config.WorldConfig.top) {
            angle = (angle <= 3.14) ? 3.14 - angle : 9.42 - angle;
            y = Config.WorldConfig.top + r;
        }
        if ((y + r) > Config.WorldConfig.bottom) {
            angle = (angle <= 3.14) ? 3.14 - angle : 9.42 - angle;
            y = Config.WorldConfig.bottom - r;
        }
        this.x = x; this.y = y; this.angle = angle;
	}

	public static PositionFixed byRadius (double x, double y, double radius)
	{
		radius *= 0.75;
        if ((x - radius) < Config.WorldConfig.left) {
            x = Config.WorldConfig.left + radius;
        }
        if ((x + radius) > Config.WorldConfig.right) {
            x = Config.WorldConfig.right - radius;
        }
        if ((y - radius) < Config.WorldConfig.top) {
            y = Config.WorldConfig.top + radius;
        }
        if ((y + radius) > Config.WorldConfig.bottom) {
            y = Config.WorldConfig.bottom - radius;
        }
        return new PositionFixed(x, y);
	}

	public PositionFixed(double x, double y)
	{
		double r = 40;
        if ((x - r) < Config.WorldConfig.left) {
            x = Config.WorldConfig.left + r;
        }
        if ((x + r) > Config.WorldConfig.right) {
            x = Config.WorldConfig.right - r;
        }
        if ((y - r) < Config.WorldConfig.top) {
            y = Config.WorldConfig.top + r;
        }
        if ((y + r) > Config.WorldConfig.bottom) {
            y = Config.WorldConfig.bottom - r;
        }
        this.x = x; this.y = y; this.angle = 0;
	}
}
