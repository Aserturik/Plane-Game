package co.edu.uptc.pojo;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Plane {
    private Double angle = 0.0;
    private List<Point> path;
    private boolean newPlane;
    boolean isFollowPath = false;
    private Point position;
    private Point nextPosition;
    private int finalId;
    private static int id;

    public Plane() {
        this.path = new ArrayList<Point>();
        this.newPlane = true;
        id++;
        System.out.println(id);
    }

    public void addPoint(Point point) {
        path.add(point);
    }

    public Double getAngle() {
        return angle;
    }

    public void setAngle(Double angle) {
        this.angle = angle;
    }

    public List<Point> getPath() {
        return path;
    }

    public void setPath(List<Point> path) {
        this.path = path;
    }

    public Point getPosition() {
        return path.get(0);
    }

    public boolean isNewPlane() {
        return newPlane;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public Point getNextPosition() {
        return path.get(1);
    }

    public void setNextPosition(Point nextPosition) {
        if (path.size() > 1) {
            path.set(1, nextPosition);
        } else {
            path.add(nextPosition);
        }
    }

    @Override
    public String toString() {
        return "Plane{" +
                "angle=" + angle +
                ", path=" + path +
                ", position=" + getPosition() +
                '}';
    }

    public void setNewPlane(boolean b) {
        this.newPlane = b;
    }

    public void setFollowPath(boolean b) {
        this.isFollowPath = b;
    }

    public int getId() {
        return id;
    }

    public boolean isFollowPath() {
        //System.out.println(id + "tamaño del pat " + path.size());
        return isFollowPath;
    }

    public int getFinalId() {
        return finalId;
    }

    public void setFinalId(int finalId) {
        this.finalId = finalId;
    }

    public static void setId(int id) {
        Plane.id = id;
    }
}