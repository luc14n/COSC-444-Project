import java.io.IOException;
import java.lang.Thread;

public class Run {
    public static void main(String[] args) throws IOException {
        Main pokedex = new Main();
        Thread mainThread = new Thread(pokedex);
        mainThread.start();
    }
}
