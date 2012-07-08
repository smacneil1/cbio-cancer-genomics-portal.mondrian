package org.mskcc.mondrian.internal.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.mskcc.mondrian.client.CancerStudy;
import org.mskcc.mondrian.client.CaseList;
import org.mskcc.mondrian.client.DataTypeMatrix;
import org.mskcc.mondrian.client.GeneticProfile;
import org.mskcc.mondrian.client.GeneticProfile.GENETIC_PROFILE_TYPE;
import org.mskcc.mondrian.internal.MondrianApp;
import org.mskcc.mondrian.internal.colorgradient.ColorGradientRange;
import org.mskcc.mondrian.internal.colorgradient.ColorGradientTheme;
import org.mskcc.mondrian.internal.configuration.ConfigurationChangedEvent.Type;
import org.mskcc.mondrian.internal.gui.heatmap.HeatmapPanelConfiguration.CELL_DISPLAY;

/**
 * Maintains the current configuration for the plugin.
 * 
 * @author Benjamin Gross
 * @author Dazhi Jiao
 */
public class MondrianConfiguration {

	private final List<MondrianConfigurationListener> listeners;
	private ColorGradientTheme colorGradientTheme;

	private boolean networkZoom = true;
	private boolean networkZoomKey;
	private boolean displayMultipleDataTypeNodes;
	
	/**
	 * The attribute in the network that stores the geneSymbolAttribute
	 */
	private Map<Long, String> networkGeneSymbolAttrMap = new HashMap<Long, String>();

	private CELL_DISPLAY heatmapWidgetCellDisplay;

	public MondrianConfiguration() {
		colorGradientTheme = ColorGradientTheme.BLUE_RED_GRADIENT_THEME;
		listeners = new ArrayList<MondrianConfigurationListener>();
	}

	public synchronized void addConfigurationListener(MondrianConfigurationListener listener) {

		// check args
		if (listener == null)
			return;

		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removedConfigurationListener(MondrianConfigurationListener listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}

	}

	public void setColorGradientRange(ColorGradientRange colorGradientRange) {
		// check args
		if (colorGradientRange == null)
			return;

		// if (dataTypeMatrix != null) {
		// ColorGradientRange stored =
		// dataTypeMatrixToRangeMap.get(dataTypeMatrix);
		// stored.setMinValue(colorGradientRange.getMinValue());
		// stored.setCenterLowValue(colorGradientRange.getCenterLowValue());
		// stored.setCenterHighValue(colorGradientRange.getCenterHighValue());
		// stored.setMaxValue(colorGradientRange.getMaxValue());
		// notifyConfigurationChanged(this, true, false, false, false, false,
		// false, false);
		// }

	}

	public ColorGradientRange getColorGradientRange() {
		// TODO Auto-generated method stub
		return null;
	}

	public ColorGradientRange getColorGradientRange(
			DataTypeMatrix dataTypeMatrix) {
		// TODO Auto-generated method stub
		return null;
	}

	public ColorGradientTheme getColorTheme() {
		return colorGradientTheme;
	}

	public void setColorTheme(ColorGradientTheme colorGradientTheme) {
		// check args
		if (colorGradientTheme == null)
			return;

		this.colorGradientTheme = colorGradientTheme;
		notifyConfigurationChanged(new ConfigurationChangedEvent(this, Type.COLOR_THEME_CHANGED));
	}

	public String getDataTypeMatrixType() {
		// TODO Auto-generated method stub
		return null;
	}

	public DataTypeMatrix getDataTypeMatrix() {
		// TODO Auto-generated method stub
		return null;
	}

	public void remapCyNetwork() {
		// TODO Auto-generated method stub

	}

	public void setNetworkZoom(boolean networkZoom) {
		this.networkZoom = networkZoom;
	}

	public boolean getNetworkZoom() {
		return networkZoom;
	}

	public void setNetworkZoomKey(boolean networkZoomKey) {
		this.networkZoomKey = networkZoomKey;
	}

	public boolean getNetworkZoomKey() {
		return networkZoomKey;
	}

	public void setDisplayMultipleDataTypeNodes(
			boolean displayMultipleDataTypeNodes) {
		this.displayMultipleDataTypeNodes = displayMultipleDataTypeNodes;
	}

	public boolean getDisplayMultipleDataTypeNodes() {
		return displayMultipleDataTypeNodes;
	}
	
	public void notifyConfigurationChanged(ConfigurationChangedEvent evt) {
		
		// copy vector to prevent changing hile firing events
		List<MondrianConfigurationListener> list = new ArrayList<MondrianConfigurationListener>();
		synchronized (this) {
			list.addAll(listeners);
		}
		// fire events to all listeners
		for (MondrianConfigurationListener listener : list) {
			listener.configurationChanged(evt);
		}		
	}

	public void setNetworkGeneSymbolAttr(Long suid, String attr) {
		this.networkGeneSymbolAttrMap.put(suid, attr);
	}
	
	public String getNetworkGeneSymbolAttr(Long suid) {
		return this.networkGeneSymbolAttrMap.get(suid);
	}
	
	/**
	 * Returns a gene-to-node(suid) map given a network
	 * @param suid
	 * @return
	 */
	public Map<String, Long> getGeneNodeMap(Long suid) {
		MondrianApp app = MondrianApp.getInstance();
		CyNetwork network = app.getNetworkManager().getNetwork(suid);
		CyTable defaultTable = network.getDefaultNodeTable();
		List<CyRow> rows = defaultTable.getAllRows();
		Map<String, Long> geneSymbolMap = new HashMap<String, Long>(); 
		String geneSymbolField = getNetworkGeneSymbolAttr(suid);
		if (geneSymbolField != null) {
			for (CyRow cyRow : rows) {
				String geneSymbol = cyRow.get(geneSymbolField, String.class);
				Long nodeSuid = cyRow.get(CyIdentifiable.SUID, Long.class);
				geneSymbolMap.put(geneSymbol, nodeSuid);
			}
		}
		return geneSymbolMap;
	}
	
	public String getTableNamespase(CancerStudy study, GeneticProfile profile, CaseList caseList) {
		return study.getStudyId() + ":" + profile.getId() + ":" + caseList.getId();
	}
	
	/**
	 * Registers a mondrian table to a network
	 * @param network
	 * @param table
	 */
	public void registerMondrianTable(CyNetwork network, CyTable table, CancerStudy study, 
			GeneticProfile profile, CaseList caseList) {
		MondrianApp app = MondrianApp.getInstance();
		CyNetworkTableManager networkTableManager = app.getNetworkTableMangager();
		
		// Attach table to netework
		app.getTableManager().addTable(table);
		app.getNetworkTableMangager().setTable(network, CyNode.class, getTableNamespase(study, profile, caseList), table);
		
		// Add imported table and study, profile, caseList to the mondrian meta table
		CyTable metaTable = networkTableManager.getTable(network, CyTable.class, "mondrian_meta_table"); 
		// creates a table to store mondrian table metadata
		if (metaTable == null) {
			metaTable = app.getTableFactory().createTable("Mondrian Table", "Table", Long.class, true, false);
			metaTable.createColumn("study_id", String.class, false);
			metaTable.createColumn("study_name", String.class, false);
			metaTable.createColumn("study_description", String.class, false);
			metaTable.createColumn("genetic_profile_id", String.class, true);
			metaTable.createColumn("genetic_profile_name", String.class, true);
			metaTable.createColumn("genetic_profile_type", String.class, true);
			metaTable.createColumn("case_list_id", String.class, true);
			metaTable.createColumn("case_list_name", String.class, true);
			metaTable.createColumn("case_list_description", String.class, true);
			metaTable.createListColumn("case_list_cases", String.class, true);
			networkTableManager.setTable(network, CyTable.class, "mondrian_meta_table", metaTable);
			app.getTableManager().addTable(metaTable);
		}

		// register the table to the mondrian_meta_table
		CyRow row = metaTable.getRow(table.getSUID());
		row.set("study_id", study.getStudyId());
		row.set("study_name", study.getName());
		row.set("study_description", study.getDescription());
		row.set("genetic_profile_id", profile.getId());
		row.set("genetic_profile_name", profile.getName());
		row.set("genetic_profile_type", profile.getType().toString());
		row.set("case_list_id", caseList.getId());
		row.set("case_list_name", caseList.getName());
		row.set("case_list_description", caseList.getDescription());
		row.set("case_list_cases", Arrays.asList(caseList.getCases()));
		
		MondrianCyTable mondrianTable = new MondrianCyTable(study, profile, caseList, table);
		ConfigurationChangedEvent evt = new ConfigurationChangedEvent(mondrianTable, Type.CBIO_DATA_IMPORTED);
		notifyConfigurationChanged(evt);
	}
	
	/**
	 * Gets the mondrian table that contains information of CyTable objects 
	 * of all imported cBio data
	 * @param network
	 * @return
	 */
	public static CyTable getMondrianMetaTable(CyNetwork network) {
		CyNetworkTableManager networkTableManager = MondrianApp.getInstance().getNetworkTableMangager();
		return networkTableManager.getTable(network, CyTable.class, "mondrian_meta_table");
	}
	
	/**
	 * Lists all the CyTable objects that are imported for a cancer study and attached to
	 * a network
	 * @param network
	 * @param study
	 * @return
	 */
	public List<MondrianCyTable> getMondrianTables(CyNetwork network, CancerStudy study) {
		List<MondrianCyTable> tables = new ArrayList<MondrianCyTable>();
		CyTable metaTable = getMondrianMetaTable(network);
		if (metaTable == null) return tables;
		Collection<CyRow> rows = metaTable.getMatchingRows("study_id", study.getStudyId());
		for (CyRow cyRow : rows) {
			GeneticProfile profile = new GeneticProfile(cyRow.get("genetic_profile_id", String.class), 
					cyRow.get("genetic_profile_name", String.class),  
					cyRow.get("genetic_profile_description", String.class), 
					cyRow.get("genetic_profile_type", String.class));
			List<String> cases = cyRow.getList("case_list_cases", String.class);
			CaseList caseList = new CaseList(cyRow.get("case_list_id", String.class), 
					cyRow.get("case_list_name", String.class),
					cyRow.get("case_list_description", String.class),
					cases.toArray(new String[cases.size()]));
			String namespace = getTableNamespase(study, profile, caseList);
			CyNetworkTableManager networkTableManager = MondrianApp.getInstance().getNetworkTableMangager();
			CyTable table = networkTableManager.getTable(network, CyNode.class, namespace);
			tables.add(new MondrianCyTable(study, profile, caseList, table));
		}
		return tables;
	}
	
	public List<GeneticProfile> getGeneticProfiles(CyNetwork network, CancerStudy study) {
		List<GeneticProfile> profiles = new ArrayList<GeneticProfile>();
		Set<String> profileSet = new HashSet<String>();
		CyTable metaTable = getMondrianMetaTable(network);
		if (metaTable == null) return profiles;
		Collection<CyRow> rows = metaTable.getMatchingRows("study_id", study.getStudyId());
		for (CyRow cyRow : rows) {
			String profileId = cyRow.get("genetic_profile_id", String.class);
			if (profileSet.contains(profileId)) continue;
			GeneticProfile profile = new GeneticProfile(cyRow.get("genetic_profile_id", String.class), 
					cyRow.get("genetic_profile_name", String.class),  
					cyRow.get("genetic_profile_description", String.class), 
					cyRow.get("genetic_profile_type", String.class));
			profileSet.add(profileId);
			profiles.add(profile);
		}		
		return profiles;
	}
	
	public List<String> getGeneticProfileTypes(CyNetwork network, CancerStudy study) {
		Set<String> profileTypeSet = new HashSet<String>();
		CyTable metaTable = getMondrianMetaTable(network);
		if (metaTable == null) return new ArrayList<String>(profileTypeSet);
		Collection<CyRow> rows = metaTable.getMatchingRows("study_id", study.getStudyId());
		for (CyRow cyRow : rows) {
			String profileType = cyRow.get("genetic_profile_type", String.class);
			profileTypeSet.add(profileType);
		}
		List<String> list = new ArrayList<String>(profileTypeSet);
		Collections.sort(list);
		return list;
	}
	
	/**
	 * Returns all the cancer studies that were loaded for a network
	 * @param network
	 * @return
	 */
	public List<CancerStudy> getCancerStudies(CyNetwork network) {
		Set<String> idset = new HashSet<String>();
		List<CancerStudy> studies = new ArrayList<CancerStudy>();
		CyTable metaTable = getMondrianMetaTable(network);
		List<CyRow> allRows = metaTable.getAllRows();
		for (CyRow cyRow : allRows) {
			String studyId = cyRow.get("study_id", String.class);
			System.out.println(studyId);
			if (idset.contains(studyId)) continue;  // already added
			String studyName = cyRow.get("study_name", String.class);
			String studyDescription = cyRow.get("study_description", String.class);
			studies.add(new CancerStudy(studyId, studyName, studyDescription));
			idset.add(studyId);
		}
		return studies;
	}
	
	/**
	 * Returns the CyTable with the genetic profile
	 * @param network
	 * @param profileId
	 * @return
	 */
	public List<CyTable> getTableByProfile(CyNetwork network, String profileId) {
		return null;
	}
	
	public CyTable getTableByProfile(CyNetwork network, String studyId, String profileId) {
		return null;
	}

	public CELL_DISPLAY getHeatmapCellDisplay() {
		return heatmapWidgetCellDisplay;
	}
	
	/*
	private void notifyConfigurationChanged(MondrianConfiguration source,
			boolean rangeChanged, boolean networkChanged,
			boolean colorThemeChanged, boolean dataTypeChanged,
			boolean heatmapPanelConfigChanged, boolean clinicalDataChanged,
			boolean applyVizStyle) {

		// create event object
		ConfigurationChangedEvent evt = new ConfigurationChangedEvent(source,
				rangeChanged, networkChanged, colorThemeChanged,
				dataTypeChanged, heatmapPanelConfigChanged,
				clinicalDataChanged, applyVizStyle);

		// copy vector to prevent changing hile firing events
		List<MondrianConfigurationListener> l = new ArrayList<MondrianConfigurationListener>();
		synchronized (this) {
			Collections.copy(listeners, l);
		}

		// fire events to all listeners
		for (MondrianConfigurationListener listener : l) {
			listener.configurationChanged(evt);
		}
	}
	*/
}
