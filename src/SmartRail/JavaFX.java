package SmartRail;


import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.stage.Stage;


public class JavaFX extends Application
{
  void runDisplay(String[] args)
  {
    launch(args);
  }

  @FXML
  private Button buildMap;
  @FXML
  private AnchorPane anchorPane;

  @FXML
  private void clicked() throws Exception
  {
    Stage stage = (Stage) buildMap.getScene().getWindow();
    Parent showMap = FXMLLoader.load(getClass().getResource("JavafxRes/GUI.fxml"));
    stage.setScene(new Scene(showMap));
  }

  private void welcomeScreen(Stage primaryStage) throws Exception
  {
    Parent welcomeScene = FXMLLoader.load(getClass().getResource("welcome.fxml"));
    Scene s = new Scene(welcomeScene);
    primaryStage.setTitle("Welcome to Smart rail");
    primaryStage.setScene(s);

    primaryStage.show();
  }

  @Override
  public void start(Stage primaryStage) throws Exception
  {
    welcomeScreen(primaryStage);
  }
}
