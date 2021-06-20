package sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Elevator extends Thread {

    private static int idGenerator;

    public int id;
    public HashMap<Integer, List<Request>> waitingRequests; // Key is currentFloor of request
    public HashMap<Integer, List<Request>> processingRequests; // Key is destFloor of request
    public int currentFloor;
    public int lastDestinationFloor;
    public int workload;
    public Direction direction;
    public ElevatorStatus status;

    public Elevator() {
        this.waitingRequests = new HashMap<Integer, List<Request>>();
        this.processingRequests = new HashMap<Integer, List<Request>>();
        this.currentFloor = 0;
        this.lastDestinationFloor = 0;
        this.workload = 0;
        this.direction = Direction.noDirection;
        for(int i = 0; i <= Controller.MaxFloor; ++i) {
            waitingRequests.put(i, new ArrayList<Request>());
            processingRequests.put(i, new ArrayList<Request>());
        }
        this.status = ElevatorStatus.wait;
        this.id = idGenerator++;
    }

    public boolean hasNoRequests() {
        for (List<Request> req : waitingRequests.values()) {
            if (!req.isEmpty())
                return false;
        }

        for (List<Request> req : processingRequests.values()) {
            if (!req.isEmpty())
                return false;
        }

        return true;
    }

    public int getPredictedWorkload(int destFloor) {
        int result = this.workload;
        for (int i = currentFloor; i != destFloor; i = destFloor > currentFloor ? i + 1 : i - 1) {
            result += this.waitingRequests.get(i).size();
            if (result > Controller.MaxWorkload)
                result = Controller.MaxWorkload;
            result -= this.processingRequests.get(i).size();
        }
        return result;
    }

    public void processRequest(Request req) {
        if (hasNoRequests() && req.currentFloor == this.currentFloor) {
            this.lastDestinationFloor = req.destinationFloor;
            this.direction = this.lastDestinationFloor > this.currentFloor ? Direction.up : Direction.down;
            req.status = Status.process;
            req.elevatorId = this.id;
            this.status = ElevatorStatus.work;
            this.processingRequests.get(req.destinationFloor).add(req);
        }
        else if (hasNoRequests() && req.currentFloor != this.currentFloor) {
            this.lastDestinationFloor = req.currentFloor;
            this.direction = this.lastDestinationFloor > this.currentFloor ? Direction.up : Direction.down;
            this.status = ElevatorStatus.work;
            req.elevatorId = this.id;
            this.waitingRequests.get(req.currentFloor).add(req);
        } else {
            this.waitingRequests.get(req.currentFloor).add(req);
            req.elevatorId = this.id;
        }
    }

    public void printData() {
        System.out.println("Elevator #" + this.id +
                " Status: " + this.status.toString() +
                " Workload = " + this.workload +
                " CurrentFloor = " + this.currentFloor +
                " DestinationFloor = " + this.lastDestinationFloor +
                " Direction: " + this.direction.toString());
        for (List<Request> reqList : waitingRequests.values()) {
            for (Request req : reqList)
                req.printData();
        }

        for (List<Request> reqList : processingRequests.values()) {
            for (Request req : reqList)
                req.printData();
        }
        System.out.println();
    }

    public String getData() {
        String res = "#" + id;
        res += "\nWill get:\n";

        for (List<Request> reqList : waitingRequests.values()) {
            for (Request req : reqList)
                res += req.id + "\n";
        }
        res += "Inside:\n";
        for (List<Request> reqList : processingRequests.values()) {
            for (Request req : reqList)
                res += req.id + " " + req.destinationFloor + "\n";
        }
        res += "Destination: " + lastDestinationFloor;
        return res;
    }

    public void run() {
        System.out.println("Elevator #" + this.id + " started");
        while(true) {
            try {
                sleep(2000);
            } catch(InterruptedException e) {

            }

            if (hasNoRequests()) {
                direction = Direction.noDirection;
                status = ElevatorStatus.wait;
                continue;
            }

            status = ElevatorStatus.work;

            // Load passengers
            while(!this.waitingRequests.get(this.currentFloor).isEmpty()
                    && this.workload <= Controller.MaxWorkload) {
                Request passenger = this.waitingRequests.get(this.currentFloor).get(0);
                this.waitingRequests.get(this.currentFloor).remove(passenger);
                passenger.status = Status.process;
                if (this.direction == Direction.up && passenger.destinationFloor > this.lastDestinationFloor ||
                        this.direction == Direction.down && passenger.destinationFloor < this.lastDestinationFloor)
                    this.lastDestinationFloor = passenger.destinationFloor;
                this.processingRequests.get(passenger.destinationFloor).add(passenger);
                this.workload++;
            }

            // Unload passengers
            while(!this.processingRequests.get(this.currentFloor).isEmpty()) {
                Request passenger = this.processingRequests.get(this.currentFloor).get(0);
                this.processingRequests.get(this.currentFloor).remove(passenger);
                passenger.status = Status.finish;
                this.workload--;
            }

            if (this.direction == Direction.up && this.currentFloor >= this.lastDestinationFloor ||
                    this.direction == Direction.down && this.currentFloor <= this.lastDestinationFloor) {
                int inProcess = 0;
                int waiting = 0;
                if (this.direction == Direction.down) {
                    this.direction = Direction.up;
                    for (List<Request> inProcessReq : this.processingRequests.values())
                        for (Request req : inProcessReq) {
                            inProcess++;
                            if (req.destinationFloor > this.lastDestinationFloor)
                                this.lastDestinationFloor = req.destinationFloor;
                        }
                    for (List<Request> waitingReq : this.processingRequests.values())
                        for (Request req : waitingReq) {
                            waiting++;
                            if (req.currentFloor > this.lastDestinationFloor)
                                this.lastDestinationFloor = req.currentFloor;
                        }
                } else if (this.direction == Direction.up) {
                    this.direction = Direction.down;
                    for (List<Request> inProcessReq : this.processingRequests.values())
                        for (Request req : inProcessReq) {
                            inProcess++;
                            if (req.destinationFloor < this.lastDestinationFloor)
                                this.lastDestinationFloor = req.destinationFloor;
                        }
                    for (List<Request> waitingReq : this.processingRequests.values())
                        for (Request req : waitingReq) {
                            waiting++;
                            if (req.currentFloor < this.lastDestinationFloor)
                                this.lastDestinationFloor = req.currentFloor;
                        }
                }
                System.out.println("Changing direction\nInProcess size = " + inProcess + "\nWaiting: " + waiting);
            }

            if (this.direction == Direction.down && currentFloor > 0)
                currentFloor--;
            if (this.direction == Direction.up && currentFloor < Controller.MaxFloor)
                currentFloor++;

        }
    }
}

enum ElevatorStatus {
    wait,
    work
}