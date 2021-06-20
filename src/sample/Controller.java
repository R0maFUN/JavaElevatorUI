package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import javafx.concurrent.Task;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

public class Controller {
    static int MaxFloor = 15;
    static int MaxWorkload = 5;
    static int ElevatorsAmount = 4;

    List<Elevator> elevators;
    Map<Elevator, Button> elevatorElements;
    List<Request> unhandledRequests;
    List<Label> floorLabels;

    @FXML
    private GridPane root;

    @FXML
    public void initialize() {
        elevators = new ArrayList<Elevator>();
        unhandledRequests = new ArrayList<Request>();
        elevatorElements = new HashMap<Elevator, Button>();
        floorLabels = new ArrayList<Label>();

        //root.gridLinesVisibleProperty().set(true);

        for (int i = 0; i < MaxFloor; ++i) {
            Label floorLabel = new Label("Floor #" + i);
            floorLabel.setPrefHeight(80);
            floorLabels.add(floorLabel);
            root.add(floorLabel, 0, i, 1, 1);
        }

        for (int i = 0; i < ElevatorsAmount; ++i) {
            elevators.add(new Elevator());
            elevators.get(i).start();
            Button elevEl = new Button("#" + i);
            elevEl.setPrefWidth(100);
            elevEl.setPrefHeight(80);
            elevatorElements.put(elevators.get(i), elevEl);
            root.add(elevatorElements.get(elevators.get(i)), i + 1, 0, 1, 1);
            elevatorElements.get(elevators.get(i)).relocate(100, 100);
        }

        new Thread(() -> {
            while (true) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                }
                System.out.println("\nCurrent data:");
                for (Elevator el : elevators)
                    el.printData();

                for (Elevator el : elevators) {
                    Platform.runLater(() -> {
                        root.getChildren().remove(elevatorElements.get(el));
                        elevatorElements.get(el).setText(el.getData());
                        root.add(elevatorElements.get(el), el.id + 1, el.currentFloor, 1, 1);
                    });
                }

                for (int i = 0; i < MaxFloor; ++i) {
                    floorLabels.get(i).setText("Floor #" + i);
                }

                // Handle old requests
                //unhandledRequests.removeIf(req -> RequestHandler.HandleRequest(req, elevators));
                int k = 0;
                while (k < unhandledRequests.size()) {
                    Request req = unhandledRequests.get(k);
                    if (RequestHandler.HandleRequest(req, elevators)) {
                        unhandledRequests.remove(req);
                        k--;
                    }
                    k++;
                }

                System.out.println("Unhandled requests:");
                for (Request unhandled : unhandledRequests) {
                    unhandled.printData();
                    final String labelText = floorLabels.get(unhandled.currentFloor).getText() + "\n" + unhandled.id + " " + unhandled.direction;
                    //labelText += "\n" + unhandled.id + " " + unhandled.direction;
                    //floorLabels.get(unhandled.currentFloor).setText(labelText);

                    Platform.runLater(() -> {
                        floorLabels.get(unhandled.currentFloor).setText(labelText);
                        //root.getChildren().remove(floorLabels.get(unhandled.currentFloor));
                        //root.add(floorLabels.get(unhandled.currentFloor), 0, unhandled.currentFloor, 1, 1);
                    });

                }

                if (unhandledRequests.size() > 7)
                    continue;

                Request newRequest = new Request();
                if (newRequest.status == Status.error)
                    continue;

                // Handle new request
                if (!RequestHandler.HandleRequest(newRequest, elevators)) {
                    unhandledRequests.add(newRequest);
                }
            }
        }).start();
    }
}

class RequestHandler {
    static boolean HandleRequest(Request req, List<Elevator> elevators) {
        // Find waiting elevators
        for (Elevator elevator : elevators) {
            if (elevator.hasNoRequests()) {
                elevator.processRequest(req);
                return true;
            }
        }

//        // Find elevators that will be waiting
//        for (Elevator elevator : elevators) {
//            if (elevator.getPredictedWorkload(req.currentFloor) == 0) {
//                elevator.processRequest(req);
//                return true;
//            }
//        }

        // Find elevators that will go through requested floor
        for (Elevator elevator : elevators) {
            if (req.direction != elevator.direction)
                continue;
            if (!(req.direction == Direction.up && req.currentFloor >= elevator.lastDestinationFloor) &&
                    !(req.direction == Direction.down && req.currentFloor <= elevator.lastDestinationFloor))
                continue;
            if (elevator.getPredictedWorkload(req.currentFloor) < Controller.MaxWorkload) {
                elevator.processRequest(req);
                return true;
            }
        }

        return false;
    }
}
