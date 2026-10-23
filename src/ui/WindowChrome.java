package ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.TextAlignment;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.application.Platform;
import javafx.stage.Stage;

/** Builds a small, draggable title bar for undecorated application windows. */
public final class WindowChrome {
    private WindowChrome() {}

    public static Parent wrap(Stage stage, Parent content) {
        BorderPane shell = new BorderPane(content);
        shell.getStyleClass().add("window-shell");
        shell.getStylesheets().add(WindowChrome.class.getResource("/css/style.css").toExternalForm());

        HBox titleBar = new HBox(10);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setNodeOrientation(javafx.geometry.NodeOrientation.LEFT_TO_RIGHT);
        titleBar.getStyleClass().add("window-chrome");

        HBox controls = new HBox(3);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.getStyleClass().add("window-controls");

        Button close = controlButton("×", "window-control window-close");
        close.setAccessibleText("Close window");
        close.setTooltip(new javafx.scene.control.Tooltip("إغلاق"));
        close.setOnAction(event -> stage.close());

        Button maximize = controlButton(stage.isMaximized() ? "❐" : "□", "window-control");
        maximize.setAccessibleText("Maximize or restore window");
        maximize.setTooltip(new javafx.scene.control.Tooltip("تكبير أو استعادة"));
        maximize.setVisible(stage.isResizable());
        maximize.setManaged(stage.isResizable());
        maximize.setOnAction(event -> stage.setMaximized(!stage.isMaximized()));
        stage.maximizedProperty().addListener((observable, wasMaximized, isMaximized) ->
                maximize.setText(isMaximized ? "❐" : "□"));

        Button minimize = controlButton("—", "window-control");
        minimize.setAccessibleText("Minimize window");
        minimize.setTooltip(new javafx.scene.control.Tooltip("تصغير"));
        minimize.setOnAction(event -> stage.setIconified(true));
        controls.getChildren().addAll(minimize, maximize, close);

        Label mark = new Label("SM");
        mark.getStyleClass().add("window-mark");
        Label title = new Label(stage.getTitle());
        title.getStyleClass().add("window-title");
        titleBar.getChildren().addAll(mark, title);
        I18n.setText(title, stage.getTitle());
        stage.titleProperty().addListener((observable, oldTitle, newTitle) -> I18n.setText(title, newTitle));

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Button language = new Button(I18n.isArabic() ? "English" : "العربية");
        language.getStyleClass().add("language-toggle");
        language.setAccessibleText("Change language");
        language.setTooltip(new javafx.scene.control.Tooltip("Switch language"));
        language.setFocusTraversable(false);
        language.setOnAction(event -> I18n.toggle());
        titleBar.getChildren().addAll(spacer, language, controls);
        I18n.register(stage);
        Platform.runLater(() -> I18n.apply(stage));
        double[] dragOffset = new double[2];
        titleBar.setOnMousePressed(event -> {
            if (isControl(event.getTarget(), titleBar) || stage.isMaximized()) return;
            dragOffset[0] = event.getScreenX() - stage.getX();
            dragOffset[1] = event.getScreenY() - stage.getY();
        });
        titleBar.setOnMouseDragged(event -> {
            if (isControl(event.getTarget(), titleBar) || stage.isMaximized()) return;
            stage.setX(event.getScreenX() - dragOffset[0]);
            stage.setY(event.getScreenY() - dragOffset[1]);
        });
        titleBar.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !isControl(event.getTarget(), titleBar) && stage.isResizable()) {
                stage.setMaximized(!stage.isMaximized());
            }
        });

        shell.setTop(titleBar);
        return shell;
    }

    private static Button controlButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().setAll(styleClass.split(" "));
        button.setFocusTraversable(false);
        button.setAlignment(Pos.CENTER);
        button.setTextAlignment(TextAlignment.CENTER);
        button.setMinSize(32, 32);
        button.setPrefSize(32, 32);
        button.setMaxSize(32, 32);
        return button;
    }

    private static boolean isControl(Object target, Node titleBar) {
        Node current = target instanceof Node node ? node : null;
        while (current != null && current != titleBar) {
            if (current instanceof Button) return true;
            current = current.getParent();
        }
        return false;
    }
}
