package pl.edu.agh.kis.dataretrieval.flow.webprocessors.exceptions;

import java.util.List;
import java.util.Map;

import pl.edu.agh.kis.dataretrieval.RetrievalException;
import pl.edu.agh.kis.dataretrieval.database.DbFieldData;

public class NotAllFieldsRetrievedException extends RetrievalException {

	Map<String, List<DbFieldData>> siteData;
	
	public NotAllFieldsRetrievedException(String message, 
			Map<String, List<DbFieldData>> siteData) {
		super(message);
		this.siteData = siteData;
	}

	public Map<String, List<DbFieldData>> getSiteData() {
		return siteData;
	}
}
