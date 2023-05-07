package co.edu.uptc.model;

import co.edu.uptc.pojo.Plane;
import co.edu.uptc.presenter.Contract;
import co.edu.uptc.view.globals.ValuesGlobals;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OperationPlanes {
    private List<Plane> planes;
    private static final int SPEED = 5;
    private Contract.Model model;
    private Plane planeSelected;
    private boolean isStartGame = false;
    private boolean isPauseGame = false;
    private LocalDate dateStartGame;
    private LocalDate datePauseGame;
    private Object lock;

    public OperationPlanes(Contract.Model model) {
        lock = new Object();
        this.model = model;
        planes = new ArrayList<>();
    }


    public void startGame() {
        isStartGame = true;
        dateStartGame = LocalDate.now();
        startThread();
        createPlanes();
        eliminatePlanes();
    }

    public void viewIsReady() {
        this.lock = true;
    }

    private synchronized void startThread() {
        Thread thread = new Thread(() -> {
            while (!isPauseGame) {
                try {
                    synchronized (lock) {
                        model.setPlanes(planes);
                        advance();
                        lock.notifyAll();
                    }
                    Thread.sleep(ValuesGlobals.TIME_SLEEP);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private synchronized void createPlanes() {
        Thread addPlanes = new Thread(() -> {
            while (!isPauseGame) {
                try {
                    synchronized (lock) {
                        randomPositionGenerator();
                        lock.wait();
                    }
                    Thread.sleep(ValuesGlobals.TIME_GENERATE_PLANE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        addPlanes.start();
    }

    private void eliminatePlanes() {
        Thread eliminatePlanes = new Thread(() -> {
            while (!isPauseGame) {
                try {
                    synchronized (lock) {
                        verifyPlanes();
                        lock.notifyAll();
                    }
                    Thread.sleep(ValuesGlobals.TIME_ELIMINATE_PLANE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        eliminatePlanes.start();
    }

    private void verifyPlanes() {
        for (Plane plane : planes) {
            if (getDistanceTo(plane, plane.getNextPosition()) == 0 && plane.getPosition().x == 0) {
                planes.remove(plane);
                break;
            } else if (getDistanceTo(plane, plane.getNextPosition()) == 0 && plane.getPosition().x == ValuesGlobals.WIDTH_FRAME) {
                planes.remove(plane);
                break;
            } else if (getDistanceTo(plane, plane.getNextPosition()) == 0 && plane.getPosition().y == 0) {
                planes.remove(plane);
                break;
            } else if (getDistanceTo(plane, plane.getNextPosition()) == 0 && plane.getPosition().y == ValuesGlobals.HEIGHT_FRAME) {
                planes.remove(plane);
                break;
            }
        }
    }

    private void randomPositionGenerator() {
        Plane plane = new Plane();
        addNewPlane(plane);
        plane.setAngle(getAngle(plane));
        moveToRoute(plane);
    }

    private Point getInversePosition(Plane plane) {
        Point position = plane.getPosition();
        Point endPoint = new Point();
        if (position.y >= ValuesGlobals.HEIGHT_FRAME) {
            endPoint.x = ValuesGlobals.WIDTH_FRAME - position.x;
            endPoint.y = 0;
        } else if (position.y <= 0) {
            endPoint.x = ValuesGlobals.WIDTH_FRAME - position.x;
            endPoint.y = ValuesGlobals.HEIGHT_FRAME;
        } else if (position.x >= ValuesGlobals.WIDTH_FRAME) {
            endPoint.x = 0;
            endPoint.y = ValuesGlobals.HEIGHT_FRAME - position.y;
        } else if (position.x <= 0) {
            endPoint.x = ValuesGlobals.WIDTH_FRAME;
            endPoint.y = ValuesGlobals.HEIGHT_FRAME - position.y;
        }
        return endPoint;
    }

    private void addNewPlane(Plane plane) {
        Random random = new Random();
        switch (random.nextInt(4 - 1 + 1) + 1) {
            case 1:
                plane.addPoint(new Point(0, random.nextInt(ValuesGlobals.HEIGHT_FRAME - 10 + 1) + 10));
                break;
            case 2:
                plane.addPoint(new Point(1010, random.nextInt(ValuesGlobals.HEIGHT_FRAME - 10 + 1) + 10));
                break;
            case 3:
                plane.addPoint(new Point(random.nextInt(ValuesGlobals.WIDTH_FRAME - 10 + 1) + 10, 0));
                break;
            case 4:
                plane.addPoint(new Point(random.nextInt(ValuesGlobals.WIDTH_FRAME - 10 + 1) + 10, ValuesGlobals.HEIGHT_FRAME));
                break;
        }
        plane.setPosition(plane.getPath().get(0));
        plane.setNextPosition(getInversePosition(plane));
        planes.add(plane);
    }

    public void moveToRoute(Plane plane) {
        double distance = getDistanceTo(plane, plane.getNextPosition());
        double dx = plane.getNextPosition().x - plane.getPosition().x;
        double dy = plane.getNextPosition().y - plane.getPosition().y;

        if (distance <= SPEED) {
            plane.getPosition().setLocation(plane.getNextPosition());
        } else {
            double angle = Math.atan2(dy, dx);
            int deltaX = (int) Math.round(SPEED * Math.cos(angle));
            int deltaY = (int) Math.round(SPEED * Math.sin(angle));

            plane.getPosition().x += deltaX;
            plane.getPosition().y += deltaY;
        }
    }

    public void advance() {
        for (Plane plane : planes) {
            if (plane.isNewPlane()) {
                moveToRoute(plane);
            } else {
                double radians = Math.toRadians(plane.getAngle());
                int dx = (int) Math.round(SPEED * Math.sin(radians));
                int dy = (int) Math.round(-SPEED * Math.cos(radians));
                plane.getPosition().translate(dx, dy);
            }
        }
    }

    public boolean checkBounds(Point point) {
        for (Plane plane : planes) {
            if (plane.getRectangle().contains(point)) {
                planeSelected = plane;
                return true;
            }
        }
        return false;
    }

    private void getNextPosition(Plane plane) {
        setNextPlanePosition(plane);
    }

    private double getAngle(Plane plane) {
        getNextPosition(plane);
        int x1 = plane.getNextPosition().x;
        int y1 = plane.getNextPosition().y;
        int x2 = plane.getPosition().x;
        int y2 = plane.getPosition().y;

        double angle = Math.atan2(y2 - y1, x2 - x1);

        return Math.toDegrees(angle);
    }

    private double getDistanceTo(Plane plane, Point point) {
        double dx = point.x - plane.getPosition().x;
        double dy = point.y - plane.getPosition().y;

        return Math.sqrt(dx * dx + dy * dy);
    }

    private void setNextPlanePosition(Plane plane) {
        if (plane.isNewPlane()) {
            setNewNextPlanePosition(plane);
        } else {
            plane.getPath().remove(0);
            plane.setNextPosition(plane.getPath().get(0));
        }
    }

    private void setNewNextPlanePosition(Plane plane) {
        if (plane.getPosition().y >= ValuesGlobals.HEIGHT_FRAME) {
            plane.getNextPosition().x = ValuesGlobals.WIDTH_FRAME - plane.getPosition().x;
            plane.getNextPosition().y = 0;
        } else if (plane.getPosition().y <= 0) {
            plane.getNextPosition().x = ValuesGlobals.WIDTH_FRAME - plane.getPosition().x;
            plane.getNextPosition().y = ValuesGlobals.HEIGHT_FRAME;
        } else if (plane.getPosition().x >= ValuesGlobals.WIDTH_FRAME) {
            plane.getNextPosition().x = 0;
            plane.getNextPosition().y = ValuesGlobals.HEIGHT_FRAME - plane.getPosition().y;
        } else if (plane.getPosition().x <= 0) {
            plane.getNextPosition().x = ValuesGlobals.WIDTH_FRAME;
            plane.getNextPosition().y = ValuesGlobals.HEIGHT_FRAME - plane.getPosition().y;
        }
    }

    public void isSelectedPlane(Point point) {
        if(checkBounds(point)) {
            System.out.println("Plane selected");
            //planeSelected.setNewPlane(false);>
        }
    }

    public void addPointToPath(Point point) {
        if (planeSelected!=null) {
            planeSelected.addPoint(point);
        }

    }

    public void pauseGame() {
        if (isPauseGame) {
            isPauseGame = false;
            returnGame();
        } else {
            isPauseGame = true;
        }
    }

    private void returnGame() {
        startThread();
        eliminatePlanes();
    }

    public void selectedPlaneNull() {
        planeSelected = null;
    }
}
