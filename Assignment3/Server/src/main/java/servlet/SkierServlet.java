package servlet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import service.SendMsg;
import servlet.vo.ErrorVO;
import servlet.vo.ResortVerticalVO;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "SkierServlet", value = "/skiers")
public class SkierServlet extends HttpServlet {

    private Gson gson = new Gson();


    @Override
    public void init(ServletConfig config) {
        System.out.println("initialize the rabbitmq configuration");
        SendMsg.init();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {


        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();
        System.out.println(urlPath);
        if(Validation.checkSkiersVerticalForSpecificDayGet(urlPath) ) {
            // todo process /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}


        } else if(Validation.checkSkiersVerticalGet(urlPath, req)) {
            // todo process  /skiers/{skierID}/vertical
            String json = gson.toJson(new ResortVerticalVO());
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");
            res.getWriter().write(json);

        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write(gson.toJson(ErrorVO.builder().message("missing paramterers").build()));
        }


    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {


        String urlPath = req.getPathInfo();

        // check we have a URL!
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)
        if(!Validation.checkSkiersNewLiftPost(urlPath)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write(gson.toJson(ErrorVO.builder().message("missing paramterers").build()));
            return;
        } else {
            res.setStatus(HttpServletResponse.SC_CREATED);
            // do any sophisticated processing with urlParts which contains all the url params
            // get the request body
            String body = GetPostBody.getPostBody(req);
            // convert the string back to hashmap
            Type type = new TypeToken<HashMap<String, Integer>>(){}.getType();
            Map<String, Integer> param = gson.fromJson(body, type);
            if(!Validation.checkSkiersNewLiftPostBodyRequest(param)) {
                 res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                 res.getWriter().write(gson.toJson(ErrorVO.builder().message("missing paramterers").build()));
                 return;
            }
            String[] parms = urlPath.split("/");

            param.put("resortID", Integer.parseInt(parms[1]));
            param.put("seasonID", Integer.parseInt(parms[3]));
            param.put("dayID", Integer.parseInt(parms[5]));
            param.put("skierID", Integer.parseInt(parms[7]));
            String newMsg = gson.toJson(param);

            // ExecutorService executor = Executors.newFixedThreadPool(5);
            SendMsg.send(newMsg);
            // res.getWriter().write(body);
        }

    }


    @Override
    public void destroy() {
        System.out.println("close the rabbitmq channel");
        SendMsg.shutdown();
    }
}
