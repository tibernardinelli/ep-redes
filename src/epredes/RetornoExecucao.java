package epredes;

public class RetornoExecucao {
	
	private String statusCode;
	private String contentType;
	private String content;

	public RetornoExecucao(String statusCode, String contentType, String content) {
		super();
		this.statusCode = statusCode;
		this.contentType = contentType;
		this.content = content;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	
	
	
}