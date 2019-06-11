import threadPool.SimpleThreadPool;
import threadPool.ThreadPool;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleHttpServer {

    //处理HttpRequest的线程池
    private static ThreadPool<HttpRequestHandler> threadPool = new SimpleThreadPool<>(10);
    //server的根路径
    private static String basePath;

    private static ServerSocket serverSocket;

    private static int port = 8080;

    public static void setPort(int port){
        if (port > 0) {
            SimpleHttpServer.port = port;
        }
    }

    public static void setBasePath(String basePath){
        if (basePath != null && new File(basePath).exists() && new File(basePath).isDirectory()){
            SimpleHttpServer.basePath = basePath;
        }
    }

    public static void start() throws IOException {
        serverSocket = new ServerSocket(port);
        Socket socket = null;
        while ((socket = serverSocket.accept()) != null){
            threadPool.execute(new HttpRequestHandler(socket));
        }
        serverSocket.close();
    }

    static class HttpRequestHandler implements Runnable{
        private Socket socket;

        public HttpRequestHandler(Socket socket){
            this.socket = socket;
        }
        @Override
        public void run() {
            String line = null;
            BufferedReader br = null;
            BufferedReader reader = null;
            PrintWriter out = null;
            InputStream in = null;
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String header = reader.readLine();
                String filePath = basePath + header.split(" ")[1];
                out = new PrintWriter(socket.getOutputStream());
                if (filePath.endsWith("jpg") || filePath.endsWith("ico")) {
                    in = new FileInputStream(filePath);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    int i = 0;
                    while ((i = in.read()) != -1){
                        byteArrayOutputStream.write(i);
                    }
                    byte[] array = byteArrayOutputStream.toByteArray();
                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: image/jpeg");
                    out.println("Content-Length: "+array.length);
                    out.println("");
                    socket.getOutputStream().write(array,0,array.length);
                } else {
                    if (header.split(" ")[1].equals("/")){
                        filePath += "index.html";
                    }
                    br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
                    out = new PrintWriter(socket.getOutputStream());

                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: text/html; charset=UTF-8");
                    out.println("");
                    while ((line = br.readLine()) != null){
                        out.println(line);
                    }
                }
                out.flush();
            } catch (IOException e) {
                out.println("HTTP/1.1 500");
                out.println("");
                out.println("<html><head><title>Example</title></head><body><p>Error 500</p></body></html>");
                out.flush();
            } finally {
                close(br,reader,out,in,socket);
            }
        }
    }

    private static void close(Closeable... closeables){
        if (closeables != null){
            for (Closeable closeable:closeables){
                try {
                    closeable.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
