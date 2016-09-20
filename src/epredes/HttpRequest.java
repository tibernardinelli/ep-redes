package epredes;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public class HttpRequest implements Runnable {

	
	private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss SSS");
    private static final String CRLF = "\n";
    private Socket socket;

    public HttpRequest(Socket socket) {
        this.socket = socket;     
    }

    @Override
    public void run() {
        try {
            processMessage();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void processMessage() throws Exception {
        // obtem a referencia para os sockets de entrada e saída e ajustar o
        // filtro de entrada.

        BufferedReader br = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

        // Lê a requisição (primeira linha) e printa no console
        String requestLine = br.readLine();
       // if (requestLine == null) return ;
        
        System.out.println();
        System.out.println(requestLine);      
        
        // Lê o header da requisição (se houver) e printa no console.
        String headerline = null;
        while ((headerline = br.readLine()).length() != 0) {
        	System.out.println(headerline);
        }

        // obtem o path do arquivo
        StringTokenizer stringTokenizer = new StringTokenizer(requestLine);
        String method = stringTokenizer.nextToken();
        String filename = ".".concat(stringTokenizer.nextToken());

        if (method.equals("POST")){
        	String st = br.readLine();
        	System.out.println(st);
        }
        
        // verifica se arquivo existe
        boolean fileexists = true;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(filename);
        } catch (Exception e) {
            fileexists = false;
        }

        // prepara retorno
        String statusLine = null;
        String contentLineType = null;
        String entityBody = null;

        if (fileexists) {
            statusLine = "HTTP/1.1 200 OK";
            contentLineType = "Content-type: " + contentType(filename);
        } else {
            statusLine = "HTTP/1.1 404 Not Found";
            contentLineType = "Content-type: text/html";
            entityBody = "<html><head><title>Not Found</title></head><body>bad bad server, no donut for you!</body></html>" + CRLF;
        }

        String saida = statusLine + CRLF + contentLineType + CRLF + CRLF;

        LogRequest.logar(String.format("[%s] - Adress: %s:%s REQ:%s BYTES OUT: %d", SDF.format(new Date()), socket.getInetAddress().getHostAddress(), socket.getPort(), requestLine, saida.getBytes().length));
        
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        os.writeBytes(saida);
        if (fileexists){
            sendBytes(fileInputStream, os);
            fileInputStream.close();
        } else {
            os.writeBytes(entityBody);
            os.flush();
        }
        
        // fecha as cadeias e noite.
        os.close();
        br.close();
        this.socket.close();
    }

    private static void sendBytes(FileInputStream fis,
            DataOutputStream os) throws Exception{
        byte[] buffer = new byte[1024];
        int bytes = 0;
        while ((bytes = fis.read(buffer)) != -1){
            os.write(buffer, 0, bytes);
        }
    }

    private String contentType(String filename) {
        if (filename.endsWith(".htm") || filename.endsWith(".html"))
            return "text/html";
        if (filename.endsWith(".gif"))
            return "image/gif";
        if (filename.endsWith(".jpeg"))
            return "image/jpeg";
        return "application/octet-stream";
    }
}