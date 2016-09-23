package com.cennavi.tiles;

import java.io.FileOutputStream;
import java.io.PrintWriter;

import com.mercator.MercatorProjection;

public class ExportChinaTiles {
	
	public static final double minLon = 73.66;
	
	public static final double minLat = 3.86;
	
	public static final double maxLon = 135.05;
	
	public static final double maxLat = 53.55;
	

	public static void main(String[] args) throws Exception {
		
		String dir = args[0];
		
		for(int i=3;i<=18;i++){
			PrintWriter out = new PrintWriter(new FileOutputStream(dir+"/"+i));
			
			generate(i, out);
			
			out.flush();
			
			out.close();
		}

	}
	
	
	
	public static void generate(int zoom,PrintWriter out){
		
		long minX = MercatorProjection.longitudeToTileX(minLon, (byte) zoom);
		
		long maxX = MercatorProjection.longitudeToTileX(maxLon, (byte) zoom); 
		
		long minY = MercatorProjection.latitudeToTileY(maxLat, (byte)zoom);
		
		long maxY = MercatorProjection.latitudeToTileY(minLat, (byte)zoom);
		
		for(long i=minX;i<=maxX;i++){
			for(long j=minY;j<=maxY;j++){
				out.println(i+","+j);
			}
		}
		
	}
	

}
