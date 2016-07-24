

import java.awt.Polygon;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.joda.time.Interval;

import edu.gsu.dmlab.util.CoordinateSystemConverter;
import edu.gsu.dmlab.databases.interfaces.ITrackDBConnection;
import edu.gsu.dmlab.datatypes.EventType;
import edu.gsu.dmlab.datatypes.GenericEvent;
import edu.gsu.dmlab.datatypes.Track;
import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.geometry.Point2D;

public class FITrackDBConnection_V2/* implements ITrackDBConnection */{
/*
	DataSource dsourc = null;
	// HashMap<EventType, BaseEventConfig> confMap;

	public FITrackDBConnection_V2(DataSource dsourc) {
		if (dsourc == null)
			throw new IllegalArgumentException("DataSource cannot be null in TrackDB constructor.");
		this.dsourc = dsourc;
		// this.confMap = confMap;
	}

	@Override
	public ArrayList<IEvent> getAllEvents(EventType type) {
		ArrayList<IEvent> results = new ArrayList<IEvent>();
		try {
			Connection con = null;
			String queryString = "SELECT eventID, startTime, endTime,  center, ccode FROM hekevents_fi";

			queryString += " where (startTime between '2012-01-01 00:00:00' and '2015-12-31 23:59:59');";

			try {
				con = this.dsourc.getConnection();
				con.setAutoCommit(true);

				PreparedStatement evnts_prep_stmt = con.prepareStatement(queryString);
				ResultSet rs = evnts_prep_stmt.executeQuery();

				while (rs.next()) {
					int id = rs.getInt(1);
					Timestamp startTime = rs.getTimestamp(2);
					Timestamp endTime = rs.getTimestamp(3);
					String centerString = rs.getString(4);
					String shapeString = rs.getString(5);

					Point2D center = this.getPoint(this.removeBrackets(centerString));
					Polygon poly = this.getPoly(shapeString);

					Interval range = new Interval(startTime.getTime(),
							endTime.getTime() - (2 * 60 * 60 * 1000 + 1800000));

					IEvent ev = new GenericEvent(id, range, center, poly.getBounds(), poly, EventType.FILAMENT);
					results.add(ev);
				}

			} catch (SQLException ex) {
				ex.printStackTrace();
			} finally {
				if (con != null) {
					con.close();
				}
			}

		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return results;
	}

	public ArrayList<ITrack> getTracks(EventType type, int expId) {
		ArrayList<ITrack> results = new ArrayList<ITrack>();
		try {
			Connection con = null;
			
			String queryString ="SELECT * from ( ";
			queryString += "SELECT eventID, startTime, endTime,  center, ccode, trk_id FROM hekevents_fi ";
			queryString += "INNER JOIN fi_tracks_2 on fi_tracks_2.evnt_id = eventID ";
			queryString += "where (startTime between '2012-01-01 00:00:00' and '2015-12-31 23:59:59') ";
			queryString += "order by startTime asc ";
			queryString += ")as t1 ";
			queryString += "group by trk_id, eventID;";
			

			try {
				con = this.dsourc.getConnection();
				con.setAutoCommit(true);

				PreparedStatement evnts_prep_stmt = con.prepareStatement(queryString);
				ResultSet rs = evnts_prep_stmt.executeQuery();
				int lastTrackId = 0;
				IEvent lastEvent = null;

				while (rs.next()) {
					int id = rs.getInt(1);
					Timestamp startTime = rs.getTimestamp(2);
					Timestamp endTime = rs.getTimestamp(3);
					String centerString = rs.getString(4);
					String shapeString = rs.getString(5);
					int trackId = rs.getInt(6);

					Point2D center = this.getPoint(this.removeBrackets(centerString));
					Polygon poly = this.getPoly(shapeString);

					Interval range = new Interval(startTime.getTime(),
							endTime.getTime() - (2 * 60 * 60 * 1000 + 1800000));

					IEvent ev = new GenericEvent(id, range, center, poly.getBounds(), poly, EventType.FILAMENT);
					if (lastTrackId == 0) {
						ITrack track = new Track(ev);
						lastTrackId = trackId;
						lastEvent = ev;
						results.add(track);
					} else {
						if (lastTrackId != trackId) {
							ITrack track = new Track(ev);
							lastTrackId = trackId;
							lastEvent = ev;
							results.add(track);
						} else {
							ev.setPrevious(lastEvent);
							lastEvent.setNext(ev);
							lastEvent.updateTimePeriod(
									new Interval(lastEvent.getTimePeriod().getStart(), ev.getTimePeriod().getStart()));
							lastEvent = ev;
						}

					}
				}

			} catch (SQLException ex) {
				ex.printStackTrace();
			} finally {
				if (con != null) {
					con.close();
				}
			}

		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return results;
	}

	@Override
	public void insertTracks(ArrayList<ITrack> tracks, int expId) {
		String dropExistsString = "DROP TABLE IF EXISTS `fi_tracks_" + expId + "`;";
		String createString = this.createString(expId);

		Connection con = null;
		try {
			try {
				con = this.dsourc.getConnection();
				con.setAutoCommit(true);
				PreparedStatement dropAndMakeTableStmt = con.prepareStatement(dropExistsString);
				dropAndMakeTableStmt.execute();

				dropAndMakeTableStmt = con.prepareStatement(createString);
				dropAndMakeTableStmt.execute();

				String queryString = "INSERT INTO `fi_tracks_" + expId;

				queryString += "` VALUES(?, ?);";

				PreparedStatement trk_ins_prep_stmt = con.prepareStatement(queryString);
				for (int i = 0; i < tracks.size(); i++) {
					ArrayList<IEvent> evnts = tracks.get(i).getEvents();
					for (int j = 0; j < evnts.size(); j++) {
						trk_ins_prep_stmt.setInt(1, evnts.get(j).getId());
						trk_ins_prep_stmt.setInt(2, i + 1);
						trk_ins_prep_stmt.addBatch();
					}
				}
				trk_ins_prep_stmt.executeBatch();

			} catch (SQLException ex) {
				ex.printStackTrace();
			} finally {
				if (con != null) {
					con.close();
				}
			}

		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	private String createString(int expId) {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE `fi_tracks_");
		sb.append(expId);
		sb.append("` (");
		sb.append("`evnt_id` int unsigned NOT NULL, ");
		sb.append("`trk_id` int NOT NULL, ");
		sb.append("CONSTRAINT `trk_event_id_fk_");
		sb.append(expId);
		sb.append(
				"` FOREIGN KEY (`evnt_id`) REFERENCES `hekevents_fi` (`eventID`) ON DELETE CASCADE ON UPDATE NO ACTION ");
		sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
		return sb.toString();
	}

	@Override
	public ArrayList<ITrack> getAllTracks(EventType type, int expId) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * getPoly: returns a objectList of 2D points extracted from the input
	 * string the objectList of points are assumed to create a polygon, they are
	 * not tested
	 * 
	 * @param pointsString
	 *            :the string to extract the points from
	 * @return :returns the objectList of 2D points
	 */
/*	private Polygon getPoly(String pointsString) {
		pointsString = this.removeBrackets(pointsString);
		String[] pointsStrings = pointsString.split(",");
		String xy;
		ArrayList<Integer> xPoints = new ArrayList<Integer>();
		ArrayList<Integer> yPoints = new ArrayList<Integer>();
		for (int i = 0; i < pointsStrings.length; i++) {
			xy = pointsStrings[i];
			Point2D pnt = this.getPoint(xy);
			xPoints.add((int) pnt.x);
			yPoints.add((int) pnt.y);
		}
		int[] xArr = new int[xPoints.size()];
		int[] yArr = new int[yPoints.size()];
		for (int i = 0; i < xArr.length; i++) {
			xArr[i] = xPoints.get(i);
			yArr[i] = yPoints.get(i);
		}

		Polygon poly = new Polygon(xArr, yArr, xArr.length);
		return poly;
	}

	/**
	 * getPoint :returns a 2D point from the input string
	 * 
	 * @param xy
	 *            :input string containing x and y coordinate
	 * @return :the 2D point extracted from the string
	 */
	private Point2D getPoint(String xy) {
		int spaceIdx = xy.indexOf(' ');
		double x = Double.parseDouble(xy.substring(0, spaceIdx));
		double y = Double.parseDouble(xy.substring(spaceIdx));
		return CoordinateSystemConverter.convertHPCToPixXY(new edu.gsu.dmlab.geometry.Point2D(x, y));
	}

	/**
	 * removeBrackets : used to remove anything preceeding or following a ( or a
	 * )
	 * 
	 * @param in
	 *            : the string to trim up
	 * @return : the trimmed string
	 */
	/*private String removeBrackets(String in) {
		int begin = in.indexOf('(');
		int end = in.lastIndexOf(')');
		while (begin >= 0 || end >= 0) {
			in = in.substring(begin + 1, end);
			begin = in.indexOf('(');
			end = in.lastIndexOf(')');
		}
		return in;
	}
*/
}
