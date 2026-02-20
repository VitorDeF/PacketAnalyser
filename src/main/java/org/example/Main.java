package org.example;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application{

    private boolean isCapturing = false;
    private final ObservableList<PacketInfo> packetList = FXCollections.observableArrayList();
    private final NetMapper netMapper = new NetMapper();
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

        btn.setText("Initialize Capture");
        root.setTop(btn);

        TableView<PacketInfo> table = createPacketTable();
        table.setItems(packetList);

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
                new Thread(() -> netMapper.captureNetwork(packetList)).start();
                isCapturing = true;
                btn.setText("Stop Capture");
            }
        });

        packetList.addListener( (ListChangeListener<? super PacketInfo>) change -> {
            while(change.next()){
                if(change.wasAdded()){
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
        count.setCellValueFactory(cellData -> cellData.getValue().countProperty().asObject());

        TableColumn<PacketInfo, String> source = new TableColumn<>("Source");
        source.setCellValueFactory(cellData -> cellData.getValue().sourceProperty());

        TableColumn<PacketInfo, String> destination = new TableColumn<>("Destination");
        destination.setCellValueFactory(cellData -> cellData.getValue().destinationProperty());

        TableColumn<PacketInfo, String> protocol = new TableColumn<>("Protocol");
        protocol.setCellValueFactory(cellData -> cellData.getValue().protocolProperty());

        TableColumn<PacketInfo, Integer> length = new TableColumn<>("Length");
        length.setCellValueFactory(cellData -> cellData.getValue().lengthProperty().asObject());

        TableColumn<PacketInfo, String> timestamp = new TableColumn<>("Capture Time");
        timestamp.setCellValueFactory(cellData -> cellData.getValue().timestampProperty());

        table.getColumns().addAll(count, source, destination, protocol, length, timestamp);
        return table;
    }
}