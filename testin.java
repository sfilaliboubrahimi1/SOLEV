
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

import org.joda.time.DateTime;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTReader;

public class testin {

	/*public static void main(String[]args){
		double leftratio=0;
	    double rightratio=0.5;
	      int length=400;
	      int count=1;
	    	  for(int p=0;rightratio<=1;p++){
	    	  double ratio=0.1;
	    	  // FIND FIRST THE CORRECT INDEX
	    	  int li=(int)(length*leftratio);
	    	  int ri=(int)(length*rightratio);
	    	  System.out.println(count+"The index are"+li+"and"+ri);
	    	  count++;
	    	  leftratio+=ratio;
	    	  rightratio+=ratio;
		
	    	  }
	}
	*/
	public static void main(String[]args) throws SQLException, ParseException{
		ArrayList<ArrayList<Polygon>> TrackPols =new ArrayList<ArrayList<Polygon>>();
		ArrayList<ArrayList<DateTime>> TrackTime =new ArrayList<ArrayList<DateTime>>();
		ArrayList<DateTime> tempDate =new ArrayList<>();
		ArrayList<Polygon> temp =new ArrayList<>();
		Polygon polygons[] = new Polygon[2];
		Polygon[] Densified_polygons= new Polygon[2];
		ArrayList<double[]> TS= new ArrayList<>();
		
		 Connection c = null;
	      try {
	         Class.forName("org.postgresql.Driver");
	         c = DriverManager
	            .getConnection("jdbc:postgresql://localhost:5433/spoca",
	            "postgres", "root");
	      } catch (Exception e) {
	         e.printStackTrace();
	         System.err.println(e.getClass().getName()+": "+e.getMessage());
	         System.exit(0);
	      }
	      System.out.println("Opened database successfully");

	     int choice=10;
	      String sql = "select * from arin";
	      Statement stmt = c.createStatement();
	      ResultSet rs = stmt.executeQuery(sql);
	      	      
	      int i=0;
	      int old_trnum=0;
	      int count=0;
	      
	      //STEP 5: Extract data from result set
	      while(rs.next()){
	         //Retrieve by column name
	       //  int arid  = rs.getInt("arid");
	         int trnum = rs.getInt("trnum");
	         String dt = rs.getString("datetime");
	         DateTime datetime= DateTime.parse(dt.substring(0,29));
	         System.out.println("THE TIME IS"+datetime);
	         String wkbString = rs.getString("pol");
	         byte[] aux = WKBReader.hexToBytes(wkbString);
	         Geometry geom = new WKBReader().read(aux);	
	         
	         
	         String wktString = geom.toText();
	        // geometry in WKT format
	         System.out.printf("trnum = "+trnum);
	         System.out.printf("WKT = %s\n", wktString);
	         GeometryFactory gf= new GeometryFactory();
	         WKTReader reader = new WKTReader(gf);
	        // System.out.println("trnum is"+trnum+"old trnum"+old_trnum);
	         if(old_trnum==trnum){
	         temp.add((Polygon) reader.read(wktString));
	         tempDate.add(datetime);
	         	if(count==(choice-1)){ TrackPols.add(temp);
	         	TrackTime.add(tempDate);         		
	         	}
	         	
	         }
	         else{
	        	 TrackPols.add(temp);
	        	 TrackTime.add(tempDate);
	        	 
	        	 temp= new ArrayList<>();
	        	 tempDate= new ArrayList<>();
	        	 
	        	 temp.add((Polygon) reader.read(wktString));
	        	 tempDate.add(datetime);
	         }
	         count++;
	         old_trnum=trnum;
	         ////////////////////////////////////////////////////////////////////////
	            }//end sql scan
	}
	
}
