package com.soa.rs.triviacreator.gui.create;

public interface TriviaCreateModelListener {
	
	public void updateLoad();
	
	public void updateSave();
	
	public void saveFailed(String msg);
	
	public void loadFailed(String msg);

}
