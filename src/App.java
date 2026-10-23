import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import database.UserRepository;
import models.Session;
import models.User;
import ui.WindowChrome;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        boolean restoredSession = false;
        int rememberedUserId = Session.getRememberedUserId();
        if (rememberedUserId > 0) {
            try {
                User user = new UserRepository().findById(rememberedUserId);
                if (user != null) {
                    Session.signIn(user);
                    restoredSession = true;
                } else {
                    Session.clear();
                }
            } catch (Exception exception) {
                // Keep the remembered session so it can be restored when the database is available.
                exception.printStackTrace();
            }
        }

        String view = restoredSession ? "/views/home.fxml" : "/views/login.fxml";
        FXMLLoader loader = new FXMLLoader(getClass().getResource(view));

        primaryStage.setTitle("Student Management System");
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setResizable(restoredSession);
        if (restoredSession) {
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(650);
        }
        Scene scene = new Scene(WindowChrome.wrap(primaryStage, loader.load()));
        primaryStage.setScene(scene);
        if (restoredSession) {
            primaryStage.setMaximized(true);
        } else {
            primaryStage.sizeToScene();
            primaryStage.centerOnScreen();
        }
        primaryStage.show();
    }

    public static void main(String[] args) {

        database.CreateTable.createTables();

        launch(args);
    }
}
