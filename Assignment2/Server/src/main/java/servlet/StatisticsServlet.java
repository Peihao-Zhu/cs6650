package servlet;

import com.google.gson.Gson;
import servlet.vo.EndpointStatsVO;
import servlet.vo.EndpointsStat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "StatisticsServlet", value = "/statistics")
public class StatisticsServlet extends HttpServlet {
    private Gson gson = new Gson();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();
        res.setStatus(HttpServletResponse.SC_OK);
        // do any sophisticated processing with urlParts which contains all the url params
        // TODO: process url params in `urlParts`

        List<EndpointsStat> endpointsStats = new ArrayList<>();
        endpointsStats.add(EndpointsStat.builder().URL("/resort").operation("GET").max(100).mean(10).build());
        EndpointStatsVO endpointStatsVO = EndpointStatsVO.builder().endpointStats(endpointsStats).build();

        String json = gson.toJson(endpointStatsVO);
        res.setStatus(HttpServletResponse.SC_OK);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(json);

    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String body = GetPostBody.getPostBody(req);
        res.setStatus(HttpServletResponse.SC_CREATED);
        // do any sophisticated processing with urlParts which contains all the url params
        // TODO: process url params in `urlParts`

        res.getWriter().write(body);
    }
}
