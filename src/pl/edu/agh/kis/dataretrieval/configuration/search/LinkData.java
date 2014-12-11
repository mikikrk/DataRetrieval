package pl.edu.agh.kis.dataretrieval.configuration.search;

public class LinkData {
	private String type;
	private String benchmark;
	
	public LinkData(String type, String benchmark) {
		super();
		this.type = type;
		this.benchmark = benchmark;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBenchmark() {
		return benchmark;
	}

	public void setBenchmark(String benchmark) {
		this.benchmark = benchmark;
	}
}
