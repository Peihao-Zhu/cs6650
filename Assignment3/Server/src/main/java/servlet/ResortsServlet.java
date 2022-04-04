package servlet;

import com.google.gson.Gson;
import servlet.vo.ErrorVO;
import servlet.vo.Resort;
import servlet.vo.ResortsVO;
import servlet.vo.SeasonsVO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "ResortsServlet", value = "/resorts")
public class ResortsServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        String urlPath = req.getPathInfo();

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");


        // check we have a URL!
        if (Validation.checkResortsGet(urlPath)) {
            List<Resort> resorts = new ArrayList<>();
            resorts.add(Resort.builder().resortName("name").resortID(1).build());
            String json = gson.toJson(ResortsVO.builder().resorts(resorts).build());
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write(json);
        } else if(Validation.checkResortsSeasonsGet(urlPath)) {
            List<String> seasons = new ArrayList<>();
            String json = gson.toJson(SeasonsVO.builder().seasons(seasons).build());
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write(json);
        } else {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(gson.toJson(ErrorVO.builder().message("missing paramterers").build()));
        }
      //  System.out.println(urlPath + " "+ urlPath.length());




    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        // get the request body
        String body = GetPostBody.getPostBody(req);

        String urlPath = req.getPathInfo();

        // check we have a URL!
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)
        if(Validation.checkResortsNewSeasonPost(urlPath) && body != null) {
            res.setStatus(HttpServletResponse.SC_CREATED);
            // do any sophisticated processing with urlParts which contains all the url params
            // TODO: process url params in `urlParts
            res.getWriter().write(body);

        } else {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(gson.toJson(ErrorVO.builder().message("missing paramterers").build()));
            return;
        }

    }
}
