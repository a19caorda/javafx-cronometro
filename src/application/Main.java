package application;

import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * ThreadState es una enumeracion para poder manejar el estado del hilo que se
 * encarga de contar el tiempo.
 */
enum ThreadState {
  Paused, Stopped, Started
}

public class Main extends Application {

  /**
   * canRun es una referencia atómica a un ThreadState, es atómico para que se
   * pueda sincronizar bien entre hilos.
   */
  AtomicReference<ThreadState> canRun = new AtomicReference<>(ThreadState.Paused);

  /**
   * task es la encargada de hacer una tarea (sumar 1 a un contador y que se pare un
   * milisegundo) en otro hilo y que se comunique con el hilo principal.
   *
   * Cada vez que se llama el método `updateMessage` se actualiza la propiedad
   * mensaje que más adelante se unirá a la propiedad texto de la etiqueta.
   */
  Task<Void> task = new Task<Void>() {

    int time = 0;

    @Override
    protected Void call() throws Exception {
      updateMessage(millisToString(time++));
      while (true) {

        switch (canRun.get()) {
          case Started:
            updateMessage(millisToString(time++));
            Thread.sleep(1);
            break;
          case Stopped:
            time = 0;
            break;
          case Paused:
            break;
        }

      }
    }

  };
  Thread chronoThead = new Thread(task);

  @Override
  public void start(Stage primaryStage) throws InterruptedException {
    /**
     * Empiezo el hilo que maneja el tiempo, como el estado actual `canRun` es
     * Paused, no va a ocurrir nada.
     */
    chronoThead.start();

    GridPane root = new GridPane();

    Label mtext = new Label();
    /**
     * Aquí conectamos la propiedad texto del Label a la propiedad mensaje de la
     * tarea.
     */
    mtext.textProperty().bind(task.messageProperty());

    Button bStart = new Button("Start");
    Button bPause = new Button("Pause");
    Button bStop = new Button("Stop");

    /**
     * A cada botón le damos una acción cuando se le haga click, cada acción cambia
     * la referencia atómica al estado del hilo.
     */
    bStart.setOnAction(actionEvent -> {
      canRun.set(ThreadState.Started);
    });
    bPause.setOnAction(actionEvent -> {
      canRun.set(ThreadState.Paused);
    });
    bStop.setOnAction(actionEvent -> {
      canRun.set(ThreadState.Stopped);
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

  /**
   * 
   * millisToString se encarga de transformar los milisegundos a una cadena de
   * texto con el formato HH:MM:SS.mmm
   * 
   * @param millis
   * @return La cadena formateada
   */
  String millisToString(int millis) {

    return String.format("%02d:%02d:%02d.%03d", 
        millis / 3_600_000, // Horas
        millis / 60_000 % 60, // Minutos
        millis / 1000 % 60, // Segundos
        millis % 1000); // Milisegundos

  }

  public static void main(String[] args) {
    launch(args);
  }
}
