package epredes;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import epredes.programas.ListagemDiretorioHtml;
import epredes.programas.Obtemconfig;

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
		String filename = null;
		if (path.contains("?"))
			filename = "./content".concat(path.split("?")[0]);
		else
			filename = "./content".concat(path);

		if (method.equals("POST")) {
			StringBuilder body = new StringBuilder();
			int length = Integer.parseInt(headers.get("Content-Length"));
			for (int i = 0; i < length; i++) {
				char c = (char) br.read();
				body.append(c);
			}

			String args = body.toString();
			System.out.println(args);
			handlePost(socket, requestLine, path, args);
		} else {
			handleGet(socket, requestLine, filename, headers);
		}

		// fecha as cadeias e noite.
		br.close();
		this.socket.close();
	}

	private void handlePost(Socket socket, String requestLine, String filename, String args) throws IOException {
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		RetornoExecucao execute = Executor.execute(filename.substring(1), args);

		String content = execute.getContent();
		os.writeBytes(content);
		LogRequest.logar(String.format("[%s] - Adress: %s:%s REQ:%s BYTES OUT: %d", SDF.format(new Date()),
				socket.getRemoteSocketAddress(), socket.getPort(), requestLine, content.getBytes().length));
		os.flush();
		os.close();
	}

	private void handleGet(Socket socket, String requestLine, String filename, Map<String, String> headers)
			throws IOException {
		String statusLine = null;
		String contentLineType = null;
		String entityBody = null;
		boolean fileexists = true;
		InputStream inputStream = null;
		File file = new File(filename);

		if (file.isDirectory()) {
			filename = filename + "/index.html";
			file = new File(filename);
			if (file.exists()) {
				inputStream = new FileInputStream(file);
			} else {
				if (new Obtemconfig().Executa(null).getContent().equals("s")) {
					RetornoExecucao executa = new ListagemDiretorioHtml().Executa(null);
					inputStream = new ByteArrayInputStream(executa.getContent().getBytes());
				} else {
					fileexists = false;
					inputStream = new FileInputStream(new File("./content/404.html"));
				}
			}
		} else {
			try {
				inputStream = new FileInputStream(filename);
			} catch (Exception e) {
				fileexists = false;
				inputStream = new FileInputStream(new File("./content/404.html"));
			}
		}

		if (requestLine.contains("gerenciador") && (!headers.containsKey("Authorization"))) {
			statusLine = "HTTP/1.1 401 Not authorized";
			contentLineType = "WWW-Authenticate: Basic";
		} else if (fileexists) {
			statusLine = "HTTP/1.1 200 OK";
			contentLineType = "Content-type: " + contentType(filename);
		} else {
			statusLine = "HTTP/1.1 404 Not Found";
			contentLineType = "Content-type: text/html";
		}

		String saida = statusLine + CRLF + contentLineType + CRLF + CRLF;

		LogRequest.logar(String.format("[%s] - Adress: %s:%s REQ:%s BYTES OUT: %d", SDF.format(new Date()),
				socket.getRemoteSocketAddress(), socket.getPort(), requestLine, saida.getBytes().length));

		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		os.writeBytes(saida);

		if (inputStream != null) {
			sendBytes(inputStream, os);
			inputStream.close();
		}
		os.close();

	}

	private static void sendBytes(InputStream fis, DataOutputStream os) throws IOException {
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