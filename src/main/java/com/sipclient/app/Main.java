package com.sipclient.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import com.sipclient.sip.core.SipManager;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Parent root = FXMLLoader.load(
                getClass().getResource("/fxml/MainView.fxml"));

        Scene scene = new Scene(root, 800, 370);

        stage.setTitle("SIP Client");

        stage.setScene(scene);

        stage.show();
  
    }

    public static void main(String[] args) {
        launch(args);
    }
}