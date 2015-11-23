package milespeele.canvas.util;

public class Circle {

    private float centerX, centerY, radius;

    public Circle(float centerX, float centerY, float radius) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
    }

    public boolean contains(float x, float y) {
        x = centerX - x;
        y = centerY - y;
        return x * x + y * y <= radius * radius;
    }

    public float getCenterX() {
        return centerX;
    }

    public float getCenterY() {
        return centerY;
    }

    public float getRadius() {
        return radius;
    }
}
