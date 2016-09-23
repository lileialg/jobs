package com.cennavi.export;

import kafka.javaapi.producer.Producer;

public interface ExportData {

	public void initConn(String pgname, String pgpasswd, String pgip, int port,
			String dbname) throws Exception;
	
	
	
	public  byte[] export(String layerName,int x,int y,int z) throws Exception;
	
	public byte[] orgnizeData(String layerName, int x, int y, int z,byte[] bytes);
	
	public void closeSource() throws Exception;
	
	public void export(Producer producer,String tileFile,String pgname, String pgpasswd, String pgip, int port,
			String dbname,int zoom,String layerName,String topic) throws Exception;
}
