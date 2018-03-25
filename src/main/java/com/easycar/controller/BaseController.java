package com.easycar.controller;

import com.easycar.entity.Page;
import com.easycar.util.Logger;
import com.easycar.util.PageData;
import com.easycar.util.UuidUtil;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class BaseController
{

    private static final long   serialVersionUID = 6357869213649815390L;
    protected            Logger logger           = Logger.getLogger(this.getClass());

    public static void logBefore(Logger logger, String interfaceName)
    {
        logger.info("");
        logger.info("start");
        logger.info(interfaceName);
    }

    public static void logAfter(Logger logger)
    {
        logger.info("end");
        logger.info("");
    }

    /**
     * 得到PageData
     */
    public PageData getPageData()
    {
        return new PageData(this.getRequest());
    }

    /**
     * 得到ModelAndView
     */
    public ModelAndView getModelAndView()
    {
        return new ModelAndView();
    }

    /**
     * 得到request对象
     */
    public HttpServletRequest getRequest()
    {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        return request;
    }

    /**
     * 得到32位的uuid
     *
     * @return
     */
    public String get32UUID()
    {

        return UuidUtil.get32UUID();
    }

    /**
     * 得到分页列表的信息
     */
    public Page getPage()
    {

        return new Page();
    }

    public String getReqData(HttpServletRequest request) throws UnsupportedEncodingException, IOException
    {
        BufferedReader br = request.getReader();//new BufferedReader(new InputStreamReader((ServletInputStream) request.getInputStream(), "utf-8"));
        StringBuffer sb = new StringBuffer("");
        String temp;
        while ((temp = br.readLine()) != null)
        {
            sb.append(temp);
        }
        br.close();
        return sb.toString();

        //return request.getParameter("params");

    }

}
