package com.cennavi.export;

import java.sql.Connection;
import java.sql.DriverManager;

public abstract class ExportDataAbs {

	protected Connection conn;
	
	public void initConn(String pgname, String pgpasswd, String pgip, int port,
			String dbname) throws Exception {
		
		Class.forName("");
		
		String url = "jdbc:postgresql://"+pgip+":"+port+"/"+dbname;
		
		conn = DriverManager.getConnection(url, pgname, pgpasswd);
		
	}
	
	public void closeSource() throws Exception{
		
		if (conn != null){
			conn.close();
		}
	}
	
	public byte[] orgnizeData(String layerName, int x, int y, int z,byte[] bytes) {

		String name = layerName;
		
		for(int i=0;i<(20-layerName.length());i++){
			name = "0" + name;
		}
		
		name += String.format("%08d", x);
		
		name += String.format("%08d", y);
		
		name += String.format("%02d", z);
		
		byte[] nameByte = name.getBytes();
		
		byte[] data = new byte[nameByte.length+bytes.length];
		
		for(int i=0;i<nameByte.length;i++){
			data[i] = nameByte[i];
		}
		
		for(int i=0;i<bytes.length;i++){
			data[i+nameByte.length] = bytes[i];
		}
		
		return data;
	}
}
