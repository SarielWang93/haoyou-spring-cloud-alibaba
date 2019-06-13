package com.haoyou.spring.cloud.alibaba.manager.controller;

import com.haoyou.spring.cloud.alibaba.manager.init.InitData;
import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;

@RestController
public class ManagerController {
    private final static Logger logger = LoggerFactory.getLogger(ManagerController.class);

    @Autowired
    private InitData initData;
    @Autowired
    protected RedisObjectUtil redisObjectUtil;

    /**
     * 对外接口，用于刷新缓存
     * @param response
     * @return
     */
    @GetMapping(value = "refreshCatch")
    public String refreshCatch(HttpServletResponse response){
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods","GET,POST");

        if(initData.doInit()){
            logger.info("刷新静态缓存信息，成功！！");
            return "success";
        }
        return "err";

    }


//    /**
//     * 文件下载
//     * @param name
//     * @param request
//     * @param response
//     * @throws FileNotFoundException
//     */
//    @RequestMapping("/download/{version}/{name}")
//    public void getDownload(@PathVariable String version,@PathVariable String name, HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException {
//        // Get your file stream from wherever.
//        String fullPath = ResourceUtils.getURL("classpath:").getPath() + "static/"+version+"/"+name;
//        logger.info("下载文件:"+name);
//        File downloadFile = new File(fullPath);
//        if(downloadFile.length()==0){
//            return;
//        }
//        ServletContext context = request.getServletContext();
//        // get MIME type of the file
//        String mimeType = context.getMimeType(fullPath);
//        if (mimeType == null) {
//            // set to binary type if MIME mapping not found
//            mimeType = "application/octet-stream";
//        }
//
//        // set content attributes for the response
//        response.setContentType(mimeType);
//        // response.setContentLength((int) downloadFile.length());
//
//        // set headers for the response
//        String headerKey = "Content-Disposition";
//        String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
//        response.setHeader(headerKey, headerValue);
//        // 解析断点续传相关信息
//        response.setHeader("Accept-Ranges", "bytes");
//        long downloadSize = downloadFile.length();
//        long fromPos = 0, toPos = 0;
//        if (request.getHeader("Range") == null) {
//            response.setHeader("Content-Length", downloadSize + "");
//        } else {
//            // 若客户端传来Range，说明之前下载了一部分，设置206状态(SC_PARTIAL_CONTENT)
//            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
//            String range = request.getHeader("Range");
//            String bytes = range.replaceAll("bytes=", "");
//            String[] ary = bytes.split("-");
//            fromPos = Long.parseLong(ary[0]);
//            if (ary.length == 2) {
//                toPos = Long.parseLong(ary[1]);
//            }
//            long size;
//            if (toPos > fromPos) {
//                size = toPos - fromPos;
//            } else {
//                size = downloadSize - fromPos;
//            }
//            response.setHeader("Content-Length", size + "");
//            downloadSize = size;
//        }
//        // Copy the stream to the response's output stream.
//        RandomAccessFile in = null;
//        OutputStream out = null;
//
//        try {
//            in = new RandomAccessFile(downloadFile, "rw");
//            // 设置下载起始位置
//            if (fromPos > 0) {
//                in.seek(fromPos);
//            }
//            // 缓冲区大小
//            int bufLen = (int)(downloadSize < 2048 ? downloadSize : 2048);
//            byte[] buffer = new byte[bufLen];
//            int num;
//            int count = 0; // 当前写到客户端的大小
//            out = response.getOutputStream();
//            logger.debug("开始传输");
//            while ((num = in.read(buffer)) != -1) {
//                out.write(buffer, 0, num);
//                count += num;
//                //处理最后一段，计算不满缓冲区的大小
//                if (downloadSize - count < bufLen) {
//                    bufLen = (int) (downloadSize-count);
//                    if(bufLen==0){
//                        break;
//                    }
//                    buffer = new byte[bufLen];
//                }
//            }
//            response.flushBuffer();
//            logger.debug("传输完毕");
//        } catch (IOException e) {
//            logger.info("数据被暂停或中断。");
////            e.printStackTrace();
//        } finally {
//            if (null != out) {
//                try {
//                    out.close();
//                } catch (IOException e) {
//                    logger.info("数据被暂停或中断。");
////                    e.printStackTrace();
//                }
//            }
//            if (null != in) {
//                try {
//                    in.close();
//                } catch (IOException e) {
//                    logger.info("数据被暂停或中断。");
////                    e.printStackTrace();
//                }
//            }
//        }
//    }

}
