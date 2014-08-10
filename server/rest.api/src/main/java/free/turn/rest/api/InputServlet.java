package free.turn.rest.api;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

/**
 * Servlet implementation class InputServlet
 */
public class InputServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	private static final String dbURL 	 = "jdbc:mysql://159.253.20.162/freeturn";
    private static final String user 	 = "root";
    private static final String password = "supervis";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public InputServlet()
    {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		switch(request.getParameter("req"))
		{
			case "get_list":
			{
				try
				{
					DBConnectionManager dbman = new DBConnectionManager(dbURL, user, password);
					
					response.setContentType("application/json");
					response.getWriter().write(dbman.getList());
				}
				catch(ClassNotFoundException | SQLException e)
				{
					response.getWriter().write(e.toString());
				}
				break;
			}
			case "get_points":
			{
				try
				{
					DBConnectionManager dbman = new DBConnectionManager(dbURL, user, password);
					
					response.setContentType("application/json");
					response.getWriter().write(dbman.getPoints(request.getParameter("id")));
				}
				catch(ClassNotFoundException | SQLException e)
				{
					response.getWriter().write(e.toString());
				}
				break;
			}
			default:
			{
				response.getWriter().write("Unknown content needed");
			}
		}	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		double[][] dummy = new double[0][0];  // The same type as your "newMap"
		Gson gson = new Gson();
		
		try
		{
			DBConnectionManager dbman = new DBConnectionManager(dbURL, user, password);
			
			RouteRecord record = new RouteRecord();
			record.name = request.getParameter("name");
			record.coords = gson.fromJson(request.getParameter("points[]"), dummy.getClass());
			
			dbman.SaveRecord(record);

		} catch (SQLException | ClassNotFoundException e)
		{
			response.getWriter().write(e.toString());
			return;
		}
		response.getWriter().write("I think it was written");
	}

}
