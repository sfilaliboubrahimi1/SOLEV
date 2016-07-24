

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.WatchEvent;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import edu.gsu.dmlab.databases.interfaces.IImageDBConnection;
import edu.gsu.dmlab.databases.interfaces.ITrackDBConnection;
import edu.gsu.dmlab.datatypes.EventType;
import edu.gsu.dmlab.datatypes.GenericEvent;
import edu.gsu.dmlab.datatypes.Track;
import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.exceptions.InvalidConfigException;
import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.imageproc.ColorMap;
import edu.gsu.dmlab.imageproc.PolyDrawing;
import edu.gsu.dmlab.imageproc.ColorMap.COLORMAP;

@SuppressWarnings("serial")
public class SeqDisp extends JFrame implements Runnable {

	IImageDBConnection imageDB;
	ArrayList<ITrack> trks;
	ArrayList<IEvent> evnts;
	int evntsIdx;
	int trksIdx;
	JLabel lbl;
	Thread runner = null;
	int pause = 2;
	int wavelength =0;

	SeqDisp(IImageDBConnection imageDB, ArrayList<ITrack>TRArray, int wavelength) throws ParseException {
	//	System.out.println("INSIDE CONST SIIIZE of track 0 should be 2"+TRArray.get(0).size());
		this.imageDB = imageDB;
		this.trks =TRArray;
		this.evnts = this.trks.get(0).getEvents();
	//	System.out.println("IMPORTANT SIZE of TRArray in track 0"+TRArray.get(0).size());
	//	System.out.println("IMPORTANT SIZE of EVNTS in track 0"+this.evnts.size());
		this.evntsIdx = 0;
		this.trksIdx = 1;
		this.wavelength=wavelength;
		}

	public void start() {
		if (this.runner == null) {
			this.runner = new Thread(this);
			this.runner.start();
		}
	}

	@Override
	public void paint(Graphics screen) {
		super.paint(screen);
		try {
			System.out.println("The Track Size"+trks.size());
			System.out.println("The track index"+this.trksIdx);
			System.out.println("The EVENT index"+evntsIdx);
			System.out.println("Events SIZE"+evnts.size()+"\n\n");
			int id;
			if (this.trksIdx <= trks.size()) {
				System.out.println("Track Index less than the Track Size");
				if (evntsIdx < evnts.size()) {
					id = this.evntsIdx++;
				} else {
					if(this.trksIdx < trks.size())this.evnts = this.trks.get(this.trksIdx++).getEvents();
					this.evntsIdx = 0;
					id = 0;
				}
			} else {
				this.trksIdx = 0;
				this.evnts = this.trks.get(trksIdx++).getEvents();
				this.evntsIdx = 0;
				id = 0;
			}

			BufferedImage img = this.imageDB.getFirstImage(this.evnts.get(id).getTimePeriod(), wavelength);
		//	System.out.println("The time period is as follows"+this.evnts.get(id).getTimePeriod());
			System.out.println("Track size is"+trks.size());
			if (img != null) {
			/*	if(){
					BufferedImage img2 = ColorMap.applyColorMap(img, COLORMAP.SDO_AIA_304);
					double scale = 256.0 / 4096.0;
					PolyDrawing.drawEvents(img2, this.evnts, Color.CYAN, scale);
					ImageIcon icon = new ImageIcon(img2);
					this.lbl.setIcon(icon);
					}
				
		        else{*/
				
					BufferedImage img2 = ColorMap.applyColorMap(img, COLORMAP.SDO_AIA_171);
					double scale = 256.0 / 4096.0;
					//this.evnts.get(0).
					PolyDrawing.drawEvent(img2, this.evnts.get(id), Color.CYAN, scale);
					ImageIcon icon = new ImageIcon(img2);
					this.lbl.setIcon(icon);
			//	}
				
			}
		} catch (SQLException | IOException | InvalidConfigException e) {
			e.printStackTrace();
		}
	}
int soukaina=1;
	public void run() {
		Thread thisThread = Thread.currentThread();

		this.setLayout(new FlowLayout());
		this.setSize(300, 300);
		this.lbl = new JLabel();

		this.add(this.lbl);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		

		while (this.runner == thisThread) {
			repaint();
			try {
				Thread.sleep(pause);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			   final BufferedImage image = new BufferedImage(this.getWidth(),
			            this.getHeight(), BufferedImage.TYPE_INT_ARGB);
			    this.paint(image.getGraphics());
			   
			    File outputfile = new File("C:\\Users\\spouki\\Documents\\GSU\\Project\\SS171"+soukaina+".png");
			    try {
					ImageIO.write(image, "png", outputfile);
					soukaina++;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			
		}
	}

	public void stop() {
		if (runner != null) {
			runner = null;
		}
	}
}
