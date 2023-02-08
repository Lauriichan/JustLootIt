package me.lauriichan.spigot.justlootit.util.direction;

public class Direction {

	private float yRotation;
	private float xRotation;

	private Vertical vertical;
	private Horizontal horizontal;
	
	public Direction() {
		setRotationX(0f);
		setRotationY(0f);
	}

	/**
	 * Creates a Direction instance
	 * 
	 * @param xRotation entity yaw
	 * @param yRotation entity pitch
	 */
	public Direction(float xRotation, float yRotation) {
		setRotationX(xRotation);
		setRotationY(yRotation);
	}

	/*
	 * 
	 * 
	 * 
	 */

	public Vertical calculateVertical() {
		/*
		 * 	UP, 
		 * 	UP_MID,
		 *	MID,
		 *  MID_DOWN,
		 *  DOWN,
		 * 
		 * Range: 90 / -90
		 */
		if (yRotation < -75f) {
			return Vertical.DOWN;
		} else if (yRotation < -25f && yRotation >= -75f) {
			return Vertical.MID_DOWN;
		} else if (yRotation < 25f && yRotation <= -25f) {
			return Vertical.MID;
		} else if (yRotation > 25f && yRotation <= 75f) {
			return Vertical.UP_MID;
		} else if (yRotation > 75f) {
			return Vertical.UP;
		}
		return Vertical.MID;
	}

	public Horizontal calculateHorizontal() {
		/*
		 * 	NORTH, 
		 * 	NORTH_NORTH_EAST,
		 *	NORTH_EAST,
		 *  EAST_NORTH_EAST,
		 *  EAST,
		 *  EAST_SOUTH_EAST,
		 *  SOUTH_EAST,
		 *  SOUTH_SOUTH_EAST,
		 *  SOUTH,
		 *	SOUTH_SOUTH_WEST,
		 *	SOUTH_WEST,
		 *	WEST_SOUTH_WEST,
		 *	WEST,
		 *	WEST_NORTH_WEST,
		 *	NORTH_WEST,
		 *	NORTH_NORTH_WEST;
		 * 
		 * Range: 0 / -360
		 */
		if ((xRotation <= 0 && xRotation > -11.25f) || (xRotation >= -360.0f && xRotation <= -348.75f)) {
			return Horizontal.SOUTH;
		} else if(xRotation > -33.75f && xRotation <= -11.25f) {
			return Horizontal.SOUTH_SOUTH_EAST;
		} else if(xRotation > -56.25f && xRotation <= -33.75f) {
			return Horizontal.SOUTH_EAST;
		} else if(xRotation > -78.75f && xRotation <= -56.25f) {
			return Horizontal.EAST_SOUTH_EAST;
		} else if(xRotation > -101.25f && xRotation <= -78.75f) {
			return Horizontal.EAST;
		} else if(xRotation > -123.75f && xRotation <= -101.25f) {
			return Horizontal.EAST_NORTH_EAST;
		} else if(xRotation > -146.25f && xRotation <= -123.75f) {
			return Horizontal.NORTH_EAST;
		} else if(xRotation > -168.75f && xRotation <= -146.25f) {
			return Horizontal.NORTH_NORTH_EAST;
		} else if(xRotation > -191.25f && xRotation <= -168.75f) {
			return Horizontal.NORTH;
		} else if(xRotation > -213.75f && xRotation <= -191.25f) {
			return Horizontal.NORTH_NORTH_WEST;
		} else if(xRotation > -236.25f && xRotation <= -213.75f) {
			return Horizontal.NORTH_WEST;
		} else if(xRotation > -258.75f && xRotation <= -236.25f) {
			return Horizontal.WEST_NORTH_WEST;
		} else if(xRotation > -281.25f && xRotation <= -258.75f) {
			return Horizontal.WEST;
		} else if(xRotation > -303.75f && xRotation <= -281.25f) {
			return Horizontal.WEST_SOUTH_WEST;
		} else if(xRotation > -326.25f && xRotation <= -303.75f) {
			return Horizontal.SOUTH_WEST;
		} else if(xRotation > -348.75f && xRotation <= -326.25f) {
			return Horizontal.SOUTH_SOUTH_WEST;
		}
		return Horizontal.NORTH;
	}

	/*
	 * 
	 * 
	 * 
	 */

	public Vertical getVertical() {
		return vertical != null ? vertical : (vertical = calculateVertical());
	}

	public Horizontal getHorizontal() {
		return horizontal != null ? horizontal : (horizontal = calculateHorizontal());
	}

	/*
	 * 
	 * 
	 * 
	 */

	public float getRotationX() {
		return xRotation;
	}

	public void setRotationX(float xRotation) {
		this.xRotation = xRotation > 0 ? xRotation * -1 : xRotation;
	}

	public float getRotationY() {
		return yRotation;
	}

	public void setRotationY(float yRotation) {
		this.yRotation = (yRotation * -1);
	}

}
