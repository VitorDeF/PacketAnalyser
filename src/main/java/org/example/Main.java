package org.example;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import jdk.swing.interop.SwingInterOpUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Main extends Application{

    private boolean isCapturing = false;
    private final ObservableList<PacketInfo> packetList = FXCollections.observableArrayList();
    private final NetMapper netMapper = new NetMapper();
    private final Queue<PacketInfo> packetQueue = new ConcurrentLinkedQueue<>();
    private double appWidth = Screen.getPrimary().getBounds().getWidth()*3/4;
    private double appHeight = Screen.getPrimary().getBounds().getHeight()*3/4;

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        Button btn = new Button();
        ToggleButton scrollButton = new ToggleButton();
        HBox topPanel = new HBox(10, btn, scrollButton);

        topPanel.setAlignment(Pos.CENTER);
        root.setTop(topPanel);
        scrollButton.setSelected(true);

        btn.setText("Initialize Capture");
        scrollButton.setText("AutoScroll On");

        TableView<PacketInfo> table = createPacketTable();
        table.setItems(packetList);

        AnimationTimer updater = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!packetQueue.isEmpty()) {
                    List<PacketInfo> auxList = new ArrayList<>();
                    PacketInfo info;
                    while ((info = packetQueue.poll()) != null) {
                        auxList.add(info);
                    }
                    packetList.addAll(auxList);
                }
            }
        };
        updater.start();

        TextArea detailsAreaHeader = createDetailArea();
        TextArea detailsAreaPayload = createDetailArea();
        SplitPane splitPaneDetails = new SplitPane();

        splitPaneDetails.setOrientation(Orientation.HORIZONTAL);
        splitPaneDetails.getItems().addAll(detailsAreaHeader, detailsAreaPayload);
        splitPaneDetails.setDividerPositions(0.5);
        splitPaneDetails.setDividerPositions(0.5);
        splitPaneDetails.setMinHeight(appHeight*1/3);

        root.setCenter(table);
        root.setBottom(splitPaneDetails);

        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newSelection) -> {
            detailsAreaHeader.setText(newSelection.getHeader());
            detailsAreaPayload.setText(newSelection.getPayload());
        });

        btn.setOnAction(event -> {
            if (isCapturing) {
                netMapper.stopCaptureNetwork();
                isCapturing = false;
                btn.setText("Initialize Capture");
            } else {
                packetList.clear();
                new Thread(() -> netMapper.captureNetwork(packetQueue)).start();
                isCapturing = true;
                btn.setText("Stop Capture");
            }
        });


        table.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            ScrollBar vBar = (ScrollBar) table.lookup(".scroll-bar:vertical");

            vBar.valueProperty().addListener((vObs, oldVal, newVal) -> {
                if (newVal.doubleValue() == 1.0) {
                    scrollButton.setSelected(true);
                    scrollButton.setText("AutoScroll On");
                }
            });
        });

        table.addEventFilter(ScrollEvent.SCROLL, event -> {
            if(event.getDeltaY()>=0) {
                scrollButton.setSelected(false);
                scrollButton.setText("AutoScroll Off");
            }
        });

        scrollButton.setOnMouseClicked(event -> {
            if(scrollButton.isSelected()){
                scrollButton.setText("AutoScroll On");
            } else {
                scrollButton.setText("AutoScroll Off");
            }
        });

        packetList.addListener( (ListChangeListener<? super PacketInfo>) change -> {
            while(change.next()){
                if(change.wasAdded() && scrollButton.isSelected()){
                    table.scrollTo(packetList.size() - 1);
                }
            }
        });

        Scene scene = new Scene(root, appWidth, appHeight);

        primaryStage.setTitle("Packet Analyser");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private TextArea createDetailArea(){
        TextArea detailArea = new TextArea();
        detailArea.setEditable(false);
        detailArea.setWrapText(true);

        return detailArea;
    }

    private TableView<PacketInfo> createPacketTable() {
        TableView<PacketInfo> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<PacketInfo, Integer> count = new TableColumn<>("No.");
        count.setCellValueFactory(new PropertyValueFactory<>("count"));

        TableColumn<PacketInfo, String> source = new TableColumn<>("Source");
        source.setCellValueFactory(new PropertyValueFactory<>("source"));

        TableColumn<PacketInfo, String> destination = new TableColumn<>("Destination");
        destination.setCellValueFactory(new PropertyValueFactory<>("destination"));

        TableColumn<PacketInfo, String> protocol = new TableColumn<>("Protocol");
        protocol.setCellValueFactory(new PropertyValueFactory<>("protocol"));

        TableColumn<PacketInfo, Integer> length = new TableColumn<>("Length");
        length.setCellValueFactory(new PropertyValueFactory<>("length"));

        TableColumn<PacketInfo, String> timestamp = new TableColumn<>("Capture Time");
        timestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        table.getColumns().addAll(count, source, destination, protocol, length, timestamp);
        return table;
    }
}