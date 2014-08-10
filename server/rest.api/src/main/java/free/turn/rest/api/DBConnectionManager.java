package free.turn.rest.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DBConnectionManager 
{
    private Connection connection;
    
    public DBConnectionManager(String dbURL, String user, String pwd) throws ClassNotFoundException, SQLException
    {
        Class.forName("com.mysql.jdbc.Driver");
        this.connection = DriverManager.getConnection(dbURL, user, pwd);
    }
    public void SaveRecord(RouteRecord record) throws SQLException
    {
    	try
    	{
    		this.connection.setAutoCommit(false);
			PreparedStatement ps = this.connection.prepareStatement("INSERT INTO routs(name) VALUES(?);", PreparedStatement.RETURN_GENERATED_KEYS);
			ps.setString(1, record.name);
			ps.executeUpdate();
			ResultSet ids = ps.getGeneratedKeys();
			
			String id = "";
			if(ids.next())
				id = ids.getString(1);
			
			PreparedStatement ps2 = this.connection.prepareStatement("INSERT INTO points(r_id, lat, lon) VALUES(?,?,?);");
			
			for(int j = 0; j < record.coords.length; j++)
			{
				ps2.setString(1, id);
				ps2.setDouble(2, record.coords[j][0]);
				ps2.setDouble(3, record.coords[j][1]);
				ps2.addBatch();
			}
			ps2.executeBatch();
			
			this.connection.commit();
		}
    	catch (SQLException e)
    	{
    		this.connection.rollback();
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public String getList()
    {
       	try
    	{    		
			Statement st = this.connection.createStatement();
			String sql = "Select id, name from routs;";
			ResultSet rs = st.executeQuery(sql);
			
			JSONObject main = new JSONObject();
			JSONArray routes = new JSONArray();
			while(rs.next())
			{
				JSONObject route  = new JSONObject();
				route.put("id", 	rs.getInt("id"));
				route.put("name", 	rs.getString("name"));
				
				routes.put(route);
			}
			main.put("main", routes);
			return main.toString();
    	}
    	catch (JSONException | SQLException e)
    	{
			e.printStackTrace();
		}
       	return "";
    }
    public String getPoints(String route_id)
    {
    	try
    	{    		
			Statement st = this.connection.createStatement();
			String sql = "Select lat, lon from points where r_id=" + route_id + ";";
			ResultSet rs = st.executeQuery(sql);
			
			
			JSONObject main = new JSONObject();
			JSONArray points = new JSONArray();
			while(rs.next())
			{
				JSONObject point  = new JSONObject();
				
				point.put("lat",   rs.getString("lat"));
				point.put("lon",   rs.getString("lon"));
				
				points.put(point);
			}
			main.put("main", points);
	    	return main.toString();
		}
    	catch (JSONException | SQLException e)
    	{
			e.printStackTrace();
		}
    	return "";
    }
}
