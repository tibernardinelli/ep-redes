package epredes;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public final class WebServer {

    public static void main(String[] args) throws Exception {
        //ajustar o número da porta
        int port = 6789;
        LogRequest.logar("Iniciando Server em http://localhost:6789/");
        //Cria um socket do tipo Server (escuta) 
        ServerSocket serverSocket = new ServerSocket(port);
        //Escuta a requisição do serviço HTTP de forma infiníta
        while (true) {
            //escuta a requisição da conexão HTTP
            //Construir o objeto para processar a mensagem da requisição HTTP
            HttpRequest httpRequest = new HttpRequest(serverSocket.accept());
            //Processa a requisição em nova thread
            Thread thread = new Thread(httpRequest);
            //Inicia a Thread
            thread.start();
        }
    }

}
