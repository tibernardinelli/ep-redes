package epredes;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public final class WebServer {

    public static void main(String[] args) throws Exception {
        int port = 6789;
        LogRequest.logar("Iniciando Server em http://localhost:6789/");
        ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            HttpRequest httpRequest = new HttpRequest(serverSocket.accept());
            Thread thread = new Thread(httpRequest);
            thread.start();
        }
    }

}
