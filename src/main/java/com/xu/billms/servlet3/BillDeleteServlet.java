package com.xu.billms.servlet3;

import com.xu.billms.service.BillService;
import com.xu.billms.service.impl.BillServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 根据id进行修改
 *
 * @author xu
 * @date 27/9/2019 上午10:32
 */
@WebServlet("/billdelete")
public class BillDeleteServlet extends HttpServlet {

    private BillService billService = new BillServiceImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        billService.deleteById(id);
        resp.sendRedirect(req.getContextPath()+"/billlist");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }
}
