package terra.shell.utils.math;

public class Coordinate {
	private int x, y, z;

	public Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
		this.z = 0;
	}

	public Coordinate(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

}
