package epredes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicInteger;

public class LogRequest {

	private static AsynchronousFileChannel channel;
	private static CompletionHandler<Integer, Object> handler;
	private static LogRequest logRequest;
	private static AtomicInteger count = new AtomicInteger(0);

	private LogRequest() throws IOException {

		channel = AsynchronousFileChannel.open(Paths.get("requests.log"), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE,
				StandardOpenOption.WRITE);
		handler = new CompletionHandler<Integer, Object>() {

			@Override
			public void completed(Integer result, Object attachment) {
				System.out.println("Attachment: " + attachment + " " + result + " bytes written");
				System.out.println("CompletionHandler Thread ID: " + Thread.currentThread().getId());
			}

			@Override
			public void failed(Throwable e, Object attachment) {
				System.err.println("Attachment: " + attachment + " failed with:");
				e.printStackTrace();
			}
		};
	}
	
	private void _logar(String message){
		message = message + "\n";
		
		channel.write(ByteBuffer.wrap(message.getBytes()), count.getAndAdd(message.length()));
	}

	public static void logar(String message){
		getInstance()._logar(message);
	}
	
	private static LogRequest getInstance() {
		if (logRequest == null)
			try {
				logRequest = new LogRequest();
			} catch (IOException e) {
				e.printStackTrace();
			}
		return logRequest;
	}

	public static void main(String[] args) {
			LogRequest.getInstance().logar("teste1");
			LogRequest.getInstance().logar("teste10");
			LogRequest.getInstance().logar("teste3");
			LogRequest.getInstance().logar("teste2");		
	}

}
