package application;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) {
		Parent root = null;
		try {
			root = FXMLLoader.load(getClass().getResource("Gui.fxml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Generator pravych nahodnych cisel");
		primaryStage.setResizable(false);
		// primaryStage.getIcons().add(new
		// Image("https://www.proprofs.com/quiz-school/topic_images/p1cn9d01dks6j4631h4fs0hdoj3.jpg"));

		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}