import java.io.*;
import java.net.*;

public class ProxyServer {
    public static void main(String[] args) throws IOException {
        int port = 9000;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Proxy Server listening on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());
            new Thread(new ProxyServerHandler(clientSocket)).start();
        }
    }
}

class ProxyServerHandler implements Runnable {
    private Socket clientSocket;

    public ProxyServerHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        ) {
            String request;
            while ((request = in.readLine()) != null) {
                if (request.trim().isEmpty()) continue;
                System.out.println("Received Request: " + request);
                String response = fetchFromInternet(request);
                out.write(response);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String fetchFromInternet(String requestUrl) throws IOException {
        URL url = new URL(requestUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line).append("\n");
        }
        in.close();
        return response.toString();
    }
}