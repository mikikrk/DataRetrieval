package pl.edu.agh.kis.dataretrieval.configuration.search;

import java.util.ArrayList;
import java.util.LinkedList;

public class ContentData {
	private String benchmark;
	private String benchmarkType;
	private Integer benchmarkNo;
	private String benchmarkAttr;
	
	private String dataType;
	private String dataPattern;
	
	private String searchPath;
	private String searchDataType;
	
	private boolean array;
	private String searchNextPath;
	
	private String terminatorNode;
	private String terminatorNodeType;
	private String terminatorPath;
	
	private LinkedList<String> searchPathNodes;
	private LinkedList<String> searchNextNodes;
	private LinkedList<String> searchTerminatorNodes;

	
	private LinkData nextPageLink;
}
