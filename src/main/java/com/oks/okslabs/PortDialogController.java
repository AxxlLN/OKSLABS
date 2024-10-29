package com.oks.okslabs;

import com.fazecast.jSerialComm.SerialPort;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.oks.okslabs.Packets.GROUP_NUMBER;

public class PortDialogController {
    @FXML
    private Label labelSend1;
    @FXML
    private Label labelReceive1;
    @FXML
    private Label labelSend2;
    @FXML
    private Label labelReceive2;
    @FXML
    private ComboBox<Integer> comboBoxBaud1;
    @FXML
    private ComboBox<Integer> comboBoxBaud2;
    @FXML
    private ComboBox<Integer> comboBoxBaud3;
    @FXML
    private ComboBox<Integer> comboBoxBaud4;
    @FXML
    private TextArea inputOne;
    @FXML
    private TextArea inputTwo;
    @FXML
    private Label port1;
    @FXML
    private Label port2;
    @FXML
    private Label port3;
    @FXML
    private Label port4;
    @FXML
    private Label outputOne;
    @FXML
    private Label outputTwo;

    private final ArrayList<SerialPort> ports = new ArrayList<>();

    public void initialize(List<String> ports) {
        SerialPort[] portsFind = SerialPort.getCommPorts();

        for (String portName : ports) {
            for (SerialPort port : portsFind) {
                if (portName.equals(port.getSystemPortName())) {
                    this.ports.add(port);
                }
            }
        }

        this.port1.setText(ports.get(2));
        this.port2.setText(ports.get(1));
        this.port3.setText(ports.get(3));
        this.port4.setText(ports.get(0));

        for (ComboBox<Integer> comboBox : Arrays.asList(comboBoxBaud1, comboBoxBaud2, comboBoxBaud3, comboBoxBaud4)) {
            comboBox.getItems().addAll(9600, 19200, 38400, 57600, 115200);
        }

        comboBoxBaud1.getSelectionModel().select(0);
        comboBoxBaud2.getSelectionModel().select(0);
        comboBoxBaud3.getSelectionModel().select(0);
        comboBoxBaud4.getSelectionModel().select(0);

        inputOne.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                handleEnterPressOne();
            }
        });

        inputTwo.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                handleEnterPressTwo();
            }
        });
    }

    private void handleEnterPressOne() {
        sendAndReceiveMessage(
                ports.get(2), ports.get(3),
                (int) comboBoxBaud1.getValue(),
                (int) comboBoxBaud2.getValue(),
                outputOne, labelSend1, labelReceive1,
                inputOne.getText()
        );
    }

    private void handleEnterPressTwo() {
        sendAndReceiveMessage(
                ports.get(0), ports.get(1),
                (int) comboBoxBaud3.getValue(),
                (int) comboBoxBaud4.getValue(),
                outputTwo, labelSend2, labelReceive2,
                inputTwo.getText()
        );
    }

    private void sendAndReceiveMessage(SerialPort sender, SerialPort receiver, int baud1, int baud2, Label label, Label labelSend, Label labelReceive, String message) {
        if (sender.openPort() && receiver.openPort()) {
            sender.setComPortParameters(baud1, 8, 1, 0);
            receiver.setComPortParameters(baud2, 8, 1, 0);
            sender.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);
            receiver.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);
            sendMessage(sender, message, labelSend);
            receiveMessage(receiver, label, labelReceive);
            sender.closePort();
            receiver.closePort();
        }
    }

    private void sendMessage(SerialPort sender, String message, Label labelSend) {
        byte[] messageBytes = message.getBytes();
        byte flag = (byte) GROUP_NUMBER;
        byte sourceAddress = (byte) ports.indexOf(sender);
        byte destinationAddress = (byte) 1;

        List<Packets> packets = Packets.fragmentData(flag, sourceAddress, destinationAddress, messageBytes);

        try {
            for (Packets packet : packets) {
                String frameStructure = packet.displayFrameStructure();
                System.out.println(frameStructure);
                byte[] frame = packet.toByteArrayWithBitStuffing();
                sender.getOutputStream().write(frame);
                sender.getOutputStream().flush();
                System.out.println("Send from " + sender.getSystemPortName() + ": " + Arrays.toString(frame));

                labelSend.setText("Sent " + frame.length);
            }
        } catch (IOException e) {
            labelSend.setText("Error sending message: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void receiveMessage(SerialPort receiver, Label label, Label labelReceive) {
        byte[] buffer = new byte[256];
        try {
            while (true) {
                int bytesRead = receiver.getInputStream().read(buffer);
                if (bytesRead > 0) {
                    String receivedMessage = new String(buffer, 0, bytesRead);

                    labelReceive.setText("Received: " + bytesRead + " bytes");

                    System.out.println("Final Received Data: " + receivedMessage);

                    label.setText(receivedMessage);
                    break;
                }
            }
        } catch (IOException e) {
            label.setText("Error receiving message: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

}