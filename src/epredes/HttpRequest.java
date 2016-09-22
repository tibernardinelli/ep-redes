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
import java.util.HashMap;
import java.util.Map;
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

		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		// Lê a requisição (primeira linha) e printa no console
		String requestLine = br.readLine();
		// if (requestLine == null) return ;

		System.out.println();
		System.out.println(requestLine);

		// Lê o header da requisição (se houver) e printa no console.
		String headerline = null;
		Map<String, String> headers = new HashMap<>();
		while ((headerline = br.readLine()).length() != 0) {
			System.out.println(headerline);
			String[] splited = headerline.split(": ");
			headers.put(splited[0], splited[1]);
		}

		// obtem o path do arquivo
		StringTokenizer stringTokenizer = new StringTokenizer(requestLine);
		String method = stringTokenizer.nextToken();
		String path = stringTokenizer.nextToken();
		String filename = "./content".concat(path);

		if (method.equals("POST")) {
			StringBuilder body = new StringBuilder();
			int length = Integer.parseInt(headers.get("Content-Length"));
			for (int i = 0; i < length; i++ ){
				char c = (char)br.read();
				body.append(c);
			}
			
			String args = body.toString();
			System.out.println(args);
			handlePost(socket, requestLine, path, args);
		} else {
			handleGet(socket, requestLine, filename);
		}

		// fecha as cadeias e noite.
		br.close();
		this.socket.close();
	}

	private void handlePost(Socket socket, String requestLine, String filename, String args) throws IOException {
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		RetornoExecucao execute = Executor.execute(filename.substring(1), args);
		
		String content = execute.getContent();
		//String statusLine = "200" + CRLF + "Content-Type: application/json" + CRLF + "Content-Lenght: " + content.length() + CRLF+ CRLF;
		//os.writeBytes(statusLine);
		os.writeBytes(content + CRLF);
		LogRequest.logar(String.format("[%s] - Adress: %s:%s REQ:%s BYTES OUT: %d", SDF.format(new Date()),
				socket.getInetAddress().getHostAddress(), socket.getPort(), requestLine, content.getBytes().length));
		os.flush();
		os.close();
	}

	private void handleGet(Socket socket, String requestLine, String filename) throws IOException {

		// verifica se arquivo existe
		boolean fileexists = true;

		FileInputStream fileInputStream = null;

		File file = new File(filename);
		if (file.isDirectory()) {
			filename = filename + "/index.html";
			file = new File(filename);
			if (file.exists()) {
				fileInputStream = new FileInputStream(file);
			} else
				fileexists = false;
		} else {
			try {
				fileInputStream = new FileInputStream(filename);
			} catch (Exception e) {
				fileexists = false;
			}
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
			entityBody = "<html><head><title>Not Found</title></head><body>bad bad server, no donut for you!</body></html>"
					+ CRLF;
		}

		String saida = statusLine + CRLF + contentLineType + CRLF + CRLF;

		LogRequest.logar(String.format("[%s] - Adress: %s:%s REQ:%s BYTES OUT: %d", SDF.format(new Date()),
				socket.getInetAddress().getHostAddress(), socket.getPort(), requestLine, saida.getBytes().length));

		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		os.writeBytes(saida);
		if (fileexists) {
			sendBytes(fileInputStream, os);
			fileInputStream.close();
		} else {
			os.writeBytes(entityBody);
			os.flush();
		}
		os.close();

	}

	private static void sendBytes(FileInputStream fis, DataOutputStream os) throws IOException {
		byte[] buffer = new byte[1024];
		int bytes = 0;
		while ((bytes = fis.read(buffer)) != -1) {
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
		if (filename.endsWith(".css"))
			return "text/css";
		if (filename.endsWith(".js"))
			return "application/javascript";
		return "application/octet-stream";
	}
}