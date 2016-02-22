package webapp;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.queryparser.classic.ParseException;

import IndexerRetriver.Retriever;

@WebServlet(name="search", urlPatterns={"/search"})
public class SearchServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Retriever ret = new Retriever();

	public SearchServlet() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String param = (String) req.getParameter("q");
		System.out.println("Attribute is "+param);

		try {
			List<Map<String,String>> search = ret.search(param);
			req.setAttribute("results", search);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("req value is "+ req.getAttribute("results"));
		req.getRequestDispatcher("index.jsp").forward(req, resp);
	}
}