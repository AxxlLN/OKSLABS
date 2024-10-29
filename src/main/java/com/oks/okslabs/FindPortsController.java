package com.oks.okslabs;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import com.fazecast.jSerialComm.*;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindPortsController {
    @FXML
    private ListView<String> portsListView;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    public void initialize() {
        portsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedPort = portsListView.getSelectionModel().getSelectedItem();
                if (selectedPort != null) {
                    String regex = "COM\\d+";

                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(selectedPort);

                    List<String> ports = new ArrayList<>();

                    while (matcher.find()) {
                        ports.add(matcher.group());
                    }
                    openPortDialog(ports);
                }
            }
        });
    }

    @FXML
    protected void onUpdateClick() {
        loadingIndicator.setVisible(true);
        scrollPane.setOpacity(0);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                checkConnectedPorts();
                return null;
            }

            @Override
            protected void succeeded() {
                loadingIndicator.setVisible(false);
                scrollPane.setOpacity(1);
                super.succeeded();
            }

            @Override
            protected void failed() {
                loadingIndicator.setVisible(false);
                scrollPane.setOpacity(1);
                super.failed();
            }
        };

        new Thread(task).start();
    }

    private void checkConnectedPorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        ArrayList<SerialPort> connectionPorts = new ArrayList<>();

        for (int i = 0; i < ports.length; i++) {
            for (int j = i + 1; j < ports.length; j++) {
                if (checkConnection(ports[i], ports[j])) {
                    System.out.println("Related ports: " + ports[i].getSystemPortName() + " and " + ports[j].getSystemPortName());
                    connectionPorts.add(ports[i]);
                    connectionPorts.add(ports[j]);
                }
            }
        }

        portsListView.getItems().clear();

        for(int i = 0; i<connectionPorts.size(); i+=2) {
            for(int j = 0; j<connectionPorts.size() && i!=j; j+=2) {
                portsListView.getItems().add(connectionPorts.get(i).getSystemPortName() + " -> " +
                        connectionPorts.get(i+1).getSystemPortName() + "\n" +
                        connectionPorts.get(j).getSystemPortName() + " -> " +
                        connectionPorts.get(j+1).getSystemPortName());
            }
        }
    }

    private boolean checkConnection(SerialPort sender, SerialPort receiver) {
        if (sender.openPort() && receiver.openPort()) {
            try {
                sender.setComPortParameters(9600, 8, 1, 0);
                receiver.setComPortParameters(9600, 8, 1, 0);
                sender.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);
                receiver.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);

                String testMessage = "test";
                sender.getOutputStream().write(testMessage.getBytes());
                sender.getOutputStream().flush();

                byte[] buffer = new byte[1024];
                int bytesRead = receiver.getInputStream().read(buffer);

                if (bytesRead > 0) {
                    return true;
                }
            } catch (Exception e) {
                System.out.println(sender.getSystemPortName() + " и " + receiver.getSystemPortName() + " not related.");
            } finally {
                sender.closePort();
                receiver.closePort();
            }
        }
        return false;
    }

    private void openPortDialog(List<String> ports) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("port-dialog.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        PortDialogController controller = loader.getController();
        controller.initialize(ports);

        Stage dialogStage = new Stage();
        dialogStage.setTitle("PortsDialog");
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setScene(new Scene(root));
        dialogStage.show();
    }
}