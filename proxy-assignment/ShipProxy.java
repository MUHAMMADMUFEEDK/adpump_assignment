
import java.io.*;
import java.net.*;

public class ShipProxy {
    public static void main(String[] args) throws IOException {
        int listenPort = 8080;
        String proxyServerHost = "proxy-server";
        int proxyServerPort = 9000;

        Socket proxySocket = new Socket(proxyServerHost, proxyServerPort);
        BufferedWriter proxyOut = new BufferedWriter(new OutputStreamWriter(proxySocket.getOutputStream()));
        BufferedReader proxyIn = new BufferedReader(new InputStreamReader(proxySocket.getInputStream()));


        ServerSocket localProxySocket = new ServerSocket(listenPort);
        System.out.println("Ship Proxy running on port " + listenPort);

        while (true) {
            Socket clientSocket = localProxySocket.accept();
            new Thread(() -> handleClient(clientSocket, proxyOut, proxyIn)).start();
        }
    }

    private static void handleClient(Socket clientSocket, BufferedWriter proxyOut, BufferedReader proxyIn) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        ) {
            String requestLine = in.readLine();
            if (requestLine == null || !requestLine.startsWith("GET")) {
                out.write("HTTP/1.1 400 Bad Request\r\n\r\n");
                out.flush();
                return;
            }

            String[] tokens = requestLine.split(" ");
            String fullUrl = tokens[1];

            synchronized (proxyOut) {
                proxyOut.write(fullUrl + "\n");
                proxyOut.flush();

                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = proxyIn.readLine()) != null && !line.isEmpty()) {
                    responseBuilder.append(line).append("\n");
                }


                out.write("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n");
                out.write(responseBuilder.toString());
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}