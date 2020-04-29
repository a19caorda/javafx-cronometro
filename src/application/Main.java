package application;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

enum ThreadState {
  ThreadPaused, ThreadStopped, ThreadStarted
}

public class Main extends Application {

  AtomicReference<ThreadState> canRun = new AtomicReference<>(ThreadState.ThreadStopped);

  Task<Void> task = new Task<Void>() {

    int time = 0;

    @Override
    protected Void call() throws Exception {
      updateMessage(millisToString(time++));
      while (true) {

        switch (canRun.get()) {
        case ThreadStarted:
          updateMessage(millisToString(time++));
          Thread.sleep(1);
          break;
        case ThreadStopped:
          time = 0;
          break;
        case ThreadPaused:
          break;
        }

      }
    }

  };
  Thread chronoThead = new Thread(task);

  @Override
  public void start(Stage primaryStage) throws InterruptedException {
    chronoThead.start();
    GridPane root = new GridPane();

    Label mtext = new Label();
    mtext.textProperty().bind(task.messageProperty());

    Button bStart = new Button("Start");
    bStart.setOnAction(actionEvent -> {
      canRun.set(ThreadState.ThreadStarted);

    });

    Button bPause = new Button("Pause");
    bPause.setOnAction(actionEvent -> {
      canRun.set(ThreadState.ThreadPaused);
    });

    Button bStop = new Button("Stop");
    bStop.setOnAction(actionEvent -> {
      canRun.set(ThreadState.ThreadStopped);
    });

    root.add(mtext, 0, 0, 4, 1);
    root.add(bStart, 0, 1);
    root.add(bPause, 1, 1);
    root.add(bStop, 2, 1);

    Scene scene = new Scene(root, 500, 300);
    scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
    primaryStage.setTitle("Cronómetro");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  String millisToString(int millis) {

    return String.format("%02d:%02d:%02d.%03d", millis / 3_600_000, // Horas
        millis / 60_000 % 60, // Minutos
        millis / 1000 % 60, // Segundos
        millis % 1000); // Milisegundos

  }

  public static void main(String[] args) {
    launch(args);
  }
}
