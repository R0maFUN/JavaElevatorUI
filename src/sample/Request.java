package sample;

import java.util.Random;

public class Request {

    static int idGenerator = 0;

    public int id;
    public int elevatorId;
    public int currentFloor;
    public int destinationFloor;
    public Direction direction;
    public Status status;

    static private Random rand = new Random();

    public Request() {
        this(rand.nextInt(Controller.MaxFloor), rand.nextInt(Controller.MaxFloor));
    }

    public Request(int currentFloor) {
        this(currentFloor, rand.nextInt(Controller.MaxFloor));
    }

    public Request(int currentFloor, int destinationFloor) {
        this.currentFloor = currentFloor;
        this.destinationFloor = destinationFloor;
        this.direction = destinationFloor > currentFloor ? Direction.up : Direction.down;
        this.status = Status.wait;
        if (destinationFloor == currentFloor) {
            this.direction = Direction.noDirection;
            this.status = Status.error;
        }
        this.id = idGenerator++;
        this.elevatorId = -1;
    }

    public void printData() {
        System.out.println("Request #" + this.id +
                " Status: " + this.status.toString() +
                " Current floor = " + this.currentFloor +
                " Dest floor = " + this.destinationFloor);
    }

}

enum Direction {
    up,
    down,
    noDirection
}

enum Status {
    wait,
    process,
    finish,
    error
}
