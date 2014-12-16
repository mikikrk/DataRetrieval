package pl.edu.agh.kis.dataretrieval.configuration.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.edu.agh.kis.dataretrieval.configuration.ConfigData;

public class SwitchData implements ConfigData{
	
	private List<String> fields = new ArrayList<String>();
	private Map<CaseData, List<ConfigData>> cases = new HashMap<CaseData, List<ConfigData>>();
	
	public List<String> getFields() {
		return fields;
	}
	public void setFields(List<String> fields) {
		this.fields = fields;
	}
	public Map<CaseData, List<ConfigData>> getCases() {
		return cases;
	}
	public void setCases(Map<CaseData, List<ConfigData>> cases) {
		this.cases = cases;
	}
	public List<ConfigData> getFlowData(CaseData caseData) {
		return cases.get(caseData);
	}
	public List<ConfigData> getFlowData(Map<String, List<String>> caseData) {
		return cases.get(caseData);
	}

}
