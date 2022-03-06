package servlet;

import sun.lwawt.macosx.CSystemTray;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class Validation {

    public static boolean checkRequiredField(HttpServletRequest request, String field) {
        String fieldValue = request.getParameter(field);
        if(fieldValue == null || fieldValue.trim().isEmpty()) {
            return false;
        }
        return true;
    }
    public static boolean checkSkiersVerticalGet(String urlPath, HttpServletRequest request) {
        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            return false;
        }
        String[] urlPathArr = urlPath.split("/");
        int len = urlPathArr.length;
        if(len == 2 && urlPathArr[1].equals("vertical") && checkRequiredField(request, "resort")) return true;
        return false;
    }

    public static boolean checkSkiersNewLiftPost(String urlPath) {
        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            return false;
        }
        String[] urlPathArr = urlPath.split("/");
        int len = urlPathArr.length;
        if(len == 8 && urlPathArr[2].equals("seasons") && urlPathArr[4].equals("days") && urlPathArr[6].equals("skiers"))  return true;
        return false;
    }

    public static boolean checkSkiersNewLiftPostBodyRequest(Map<String, Integer> param) {
        if(param.containsKey("liftID") && param.containsKey("time") && param.containsKey("waitTime")) {
            return true;
        }
        System.out.println(param.containsKey("liftID") +" " + param.containsKey("time")  + " " + param.containsKey("waitTime"));
        return false;
    }

    public static boolean checkSkiersVerticalForSpecificDayGet(String urlPath) {
        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            return false;
        }
        String[] urlPathArr = urlPath.split("/");
        int len = urlPathArr.length;
        if(len == 7 && urlPathArr[1].equals("seasons") && urlPathArr[3].equals("day") && urlPathArr[5].equals("skier"))  return true;
        return false;
    }

    public static boolean checkResortsGet(String urlPath) {
        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            return true;
        }
        return false;
    }

    public static boolean checkResortsSeasonsGet(String urlPath) {
        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            return false;
        }
        String[] urlPathArr = urlPath.split("/");
        int len = urlPathArr.length;
        if(len == 2 && urlPathArr[1].equals("seasons") && isInteger(urlPathArr[0]))  return true;
        return false;
    }

    public static boolean checkResortsNewSeasonPost(String urlPath) {
        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            return false;
        }
        String[] urlPathArr = urlPath.split("/");
        int len = urlPathArr.length;
        if(len == 2 && urlPathArr[1].equals("seasons") && isInteger(urlPathArr[0]))  return true;
        return false;
    }

    private static boolean isInteger(String num) {
        try {
            Integer.parseInt(num);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }



}
