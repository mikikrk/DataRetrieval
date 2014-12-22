package pl.edu.agh.kis.dataretrieval.configuration.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
		for (Entry<CaseData, List<ConfigData>> caseEntry: cases.entrySet()){
			if (caseData.equals(caseEntry.getKey())){
				return caseEntry.getValue();
			}
		}
		return null;
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
