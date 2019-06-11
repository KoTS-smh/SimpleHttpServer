import java.io.File;
import java.io.IOException;

public class SimpleHttpServerTest {

    public static void main(String args[]) throws IOException {
        File file = new File("");
        String filePath = file.getCanonicalPath();
        SimpleHttpServer.setPort(9000);
        SimpleHttpServer.setBasePath(filePath+"/resources");
        SimpleHttpServer.start();
    }
}
