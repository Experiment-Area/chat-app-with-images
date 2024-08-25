package lk.ijse.dep12.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class LoginController {
    public TextField txtHost;
    public TextField txtName;
    public TextField txtPort;
    public Button btnLogin;

    public void btnLoginOnAction(ActionEvent actionEvent) {

        String nickName = txtName.getText();
        String port = txtPort.getText();
        String host = txtHost.getText();

        if (host.isBlank()) {
            txtHost.requestFocus();
            txtHost.selectAll();
            return;
        }
        if (port.isBlank()) {
            txtPort.requestFocus();
            txtPort.selectAll();
            return;
        }
        if (nickName.isBlank()) {
            txtName.requestFocus();
            txtName.selectAll();
            return;
        }

        try {
            Socket remoteSocket = new Socket(host.strip(), Integer.parseInt(port));

            ((Stage) btnLogin.getScene().getWindow()).close();

            Stage mainStage = new Stage();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
            mainStage.setScene(new Scene(fxmlLoader.load()));

            MainController controller = fxmlLoader.getController();
            controller.initData(remoteSocket, nickName);

            mainStage.setTitle("DEP 12 General Chat App");
            mainStage.show();
            mainStage.centerOnScreen();

            mainStage.setOnCloseRequest((event) -> {
                try {
                    remoteSocket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        }catch (NumberFormatException n) {
            new Alert(Alert.AlertType.ERROR, "Invalid port number").show();
            txtPort.requestFocus();
            txtPort.selectAll();

        } catch (UnknownHostException u) {
            new Alert(Alert.AlertType.ERROR, "Invalid IP Address/Host").show();
            txtHost.requestFocus();
            txtHost.selectAll();

        }catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to connect with the server").show();
        }
    }
}

