package lk.ijse.dep12.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import lk.ijse.dep12.shared.image.to.ImageFile;

import java.io.*;
import java.net.Socket;

public class MainController {
    public TextArea txtDisplay;
    public TextField txtMessage;
    public Button btnSend;
    public Button btnAddImage;
    public VBox vBoxMain;
    private String nickName;
    private Socket remoteSocket;
    private ObjectOutputStream oos;

    public void initData(Socket remoteSocket, String nickName) throws IOException {
        this.nickName = nickName;
        this.remoteSocket = remoteSocket;
        appendText("You: Entered into the chat room \n");

        oos = new ObjectOutputStream(remoteSocket.getOutputStream());

        new Thread(() -> {

            while (true) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(remoteSocket.getInputStream());

                    Object object = ois.readObject();
                    if (object instanceof ImageFile) {
                        System.out.println("This is a image file");

                        File output = new File(new File(System.getenv("HOME"), "Downloads"), "temp" + System.currentTimeMillis());
                        ImageFile image = (ImageFile) object;
                        byte[] imageBytes = image.getImageBytes();
                        System.out.println(output.getName());
                        FileOutputStream fos = new FileOutputStream(output);
                        fos.write(imageBytes);

                        setImageToChat(output);

                    } else if (object instanceof String) {
                        System.out.println("Message Received");
                        String message = (String) object;
                        appendText(message);
                    }

                } catch (IOException e) {
                    if (remoteSocket.isConnected())
                        throw new RuntimeException(e);
                } catch (ClassNotFoundException c) {
                    throw new RuntimeException(c);
                }
            }
        }).start();
    }

    private void setImageToChat(File image) {
        Platform.runLater(() -> {
            ImageView imageView = new ImageView(image.toURI().toString());  //give path as a URL
            imageView.setFitWidth(200);     //set width of imageview
            imageView.setPreserveRatio(true);
            vBoxMain.getChildren().add(imageView);
        });
    }

    private void appendText(String message) {
        Platform.runLater(() -> {
            Label messageLabel = new Label(message);
            vBoxMain.getChildren().add(messageLabel);
        });
    }

    public void btnSendOnAction(ActionEvent actionEvent) throws IOException {
        appendText("You: " + txtMessage.getText().strip() + "\n");
        String message = nickName + ": " + txtMessage.getText().strip();
        oos.writeObject(message);
        txtMessage.clear();
        txtMessage.requestFocus();
        System.out.println("Message Send");
    }

    public void btnAddImageOnAction(ActionEvent actionEvent) throws IOException {

        FileChooser fileChooser = new FileChooser();

        //applying an extension filter to get jpeg images
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPEG Image", "*.jpeg", "*.jpg"));

        //for gif
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GiF Image", "*.gif"));

        //for all files
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));

        //to select a single file
        File file = fileChooser.showOpenDialog(btnAddImage.getScene().getWindow());
        // if we don't want as a modal window, can put null

       if (file == null) {
           txtMessage.setText("No file selected");
       }else {
           //setting image in own chat window
           setImageToChat(file);
       }

        try(FileInputStream fis = new FileInputStream(file)) {

            byte[] imageBytes = new byte[(int) file.length()];
            int read = fis.read(imageBytes);
            ImageFile image = new ImageFile(imageBytes);
            oos.writeObject(image);
            System.out.println("Image Send");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
