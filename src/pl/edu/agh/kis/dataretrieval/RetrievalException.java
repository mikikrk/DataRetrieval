package pl.edu.agh.kis.dataretrieval;

public class RetrievalException extends Exception {
	private String message;

	public RetrievalException(String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
