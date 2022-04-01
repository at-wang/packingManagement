package cn.itwang.packingmanagement.utils;

import org.apache.poi.ss.usermodel.Workbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 下载工具类
 */
public class DownloadUtils {
    public static void download(ByteArrayOutputStream byteArrayOutputStream, HttpServletResponse response, String returnName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        returnName = response.encodeURL(new String(returnName.getBytes(),"iso8859-1"));//保存的文件名,必须和页面编码一致,否则乱码
        response.addHeader("Content-Disposition","attachment;filename="+returnName);
        response.setContentLength(byteArrayOutputStream.size());
        response.addHeader("Content-Length", "" + byteArrayOutputStream.size());
        ServletOutputStream outputstream = response.getOutputStream();	//取得输出流
        byteArrayOutputStream.writeTo(outputstream);					//写到输出流
        byteArrayOutputStream.close();									//关闭
        outputstream.flush();											//刷数据
    }
}
