package cn.lenovo.cwnisface.face.bean;

/** 
* @Date��2018��1��31�� ����10:44:56  
* @author baohm1
*/

public class FaceRect {
	public int x, y, width, height;
	
	public FaceRect(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public FaceRect() {
        this(0, 0, 0, 0);
    }
    
    public boolean IsEmpty() {
        return width <= 0 || height <= 0;
    }

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public String toString() {
		return "FaceRect [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
	}
}
