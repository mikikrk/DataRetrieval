package pl.edu.agh.kis.dataretrieval.configuration.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.edu.agh.kis.dataretrieval.configuration.ConfigData;

public class SwitchData implements ConfigData{
	
	private Map<CaseData, List<ConfigData>> cases = new HashMap<CaseData, List<ConfigData>>();
	private Map<String, FormFieldData> refFields = new HashMap<String, FormFieldData>();
	
	public Map<CaseData, List<ConfigData>> getCases() {
		return cases;
	}
	public void addCases(Map<CaseData, List<ConfigData>> cases) {
		this.cases.putAll(cases);
	}
	public List<ConfigData> getFlowData(CaseData caseData) {
		return cases.get(caseData);
	}
	public Map<String, FormFieldData> getRefFields() {
		return refFields;
	}
	public void addRefFields(Map<String, FormFieldData> refFields) {
		this.refFields.putAll(refFields);
	}
	public void addRefField(String fieldName, FormFieldData field) {
		this.refFields.put(fieldName, field);
	}

}
