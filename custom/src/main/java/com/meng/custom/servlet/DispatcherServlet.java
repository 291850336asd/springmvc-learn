package com.meng.custom.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DispatcherServlet extends HttpServlet {

    Context context=new Context();

    @Override
    public void init() throws ServletException {
        context.init();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       String uri = req.getRequestURI();
       System.out.println("请求的uri : " + uri);
       String projectName = req.getContextPath();
       String path = uri.replaceAll(projectName, "");
       //获取对应的请求方法
        Method method = context.getRequestMethods(path);
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = resp.getWriter();
        if(method == null){
            writer.write("url map not find!");
            writer.flush();
            writer.close();
            return;
        }

        //未设置method  和controller关联 ，只支持Controller和RequestMapping value一致
        String contrllerRequestName = path.split("/")[1];
        Object controller = context.getControllerInsance("/"+contrllerRequestName);
        if(controller != null){
            Object object = null;
            try {
                object = method.invoke(controller, new Object[]{req, resp});
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                object = e.getMessage();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                object = e.getMessage();
            }
            writer.write((String)object);
            writer.flush();
            writer.close();
        } else {
            writer.write("未设置method  和controller关联 ，只支持Controller和RequestMapping value一致!");
            writer.flush();
            writer.close();
            return;
        }

    }
}
