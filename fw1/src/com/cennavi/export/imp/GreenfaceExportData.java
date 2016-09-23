package com.cennavi.export.imp;

import java.io.FileInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;

import com.Pbf;
import com.cennavi.export.ExportData;
import com.cennavi.export.ExportDataAbs;
import com.mercator.TileUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

public class GreenfaceExportData extends ExportDataAbs implements ExportData {

	@Override
	public byte[] export(String layerName, int x, int y, int z) throws Exception {

		String sql = "select pid,kind,form,name_zh,name_en,st_astext(geom) wkt from green_face a where st_intersects(a.geom,st_geometryfromtext(?,4326)) = true";

		PreparedStatement pstmt = this.conn.prepareStatement(sql);

		String wkt = TileUtils.parseXyz2Bound(x, y, z);

		pstmt.setString(1, wkt);

		ResultSet resultSet = pstmt.executeQuery();

		resultSet.setFetchSize(3000);

		
		List<Map<String, Object>> attrs = new ArrayList<Map<String, Object>>();

		List<Geometry> geoms = new ArrayList<Geometry>();

		while (resultSet.next()) {

			Geometry geom = new WKTReader()
					.read(resultSet.getString("wkt"));

			Map<String, Object> attr = new HashMap<String, Object>();

			attr.put("pid", resultSet.getInt("pid"));
			
			attr.put("kind", resultSet.getInt("kind"));
			
			attr.put("form", resultSet.getInt("form"));
			
			attr.put("name_zh", resultSet.getString("name_zh"));
			
			attr.put("name_en", resultSet.getString("name_en"));
			
			attrs.add(attr);
			
			geoms.add(geom);

		}

		resultSet.close();

		pstmt.close();
		
		if (attrs.size()>0){
			Pbf pbf = new Pbf();

			byte[] bytes = pbf.encode(x, y, z, layerName, attrs, geoms);
			
			bytes = this.orgnizeData(layerName, x, y, z, bytes);
			
			return bytes;
		}
			
		return null;
		

	}

	@Override
	public void export(Producer producer, String tileFile,String pgname, String pgpasswd, String pgip, int port,
			String dbname,int zoom,String layerName,String topic) throws Exception{
		
		this.initConn(pgname, pgpasswd, pgip, port, dbname);
		
		Scanner scanner = new Scanner(new FileInputStream(tileFile));
		
		while(scanner.hasNextLine()){
			String[] splits = scanner.nextLine().split(",");
			
			int x = Integer.parseInt(splits[0]);
			
			int y = Integer.parseInt(splits[1]);
			
			byte[] data = this.export(layerName, x, y, zoom);
			
			if (data != null){
				producer.send(new KeyedMessage<Integer, byte[]>(topic, data));
			}
		}
		
		this.closeSource();
	}



}
