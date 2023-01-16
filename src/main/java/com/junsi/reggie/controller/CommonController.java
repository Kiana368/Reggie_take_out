package com.junsi.reggie.controller;

import com.junsi.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * 用于处理文件上传&下载
 */
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {
    @Value("${reggie.path}") // 把配置文件中的参数值传入
    private String basePath;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) { // 参数名必须与前端中网页要求name一致
        // file是一个临时文件，需要转存，否则本次请求后会自动删除
        log.info(file.toString());

        // 文件原始文件名, 若上传文件重名，则会覆盖
        String originalfileFilename = file.getOriginalFilename();
        // 获取文件后缀名
        String suffix = originalfileFilename.substring(originalfileFilename.lastIndexOf('.'));

        // 随机生成文件名，使用UUID，防止重名造成文件覆盖
        String fileName = UUID.randomUUID().toString();

        // 创建一个目录对象
        File dir = new File(basePath);
        if (!dir.exists()) {
            // 不存在则需要创建
            dir.mkdirs();
        }

        try {
            // 把文件转存到相应位置
            file.transferTo(new File(basePath + fileName + suffix));
        } catch (IOException e){
            e.printStackTrace();
        }
        return R.success(fileName + suffix);
    }


    /**
     * 文件下载 （到浏览器来显示)
     * @param name
     * @return
     */
    @GetMapping("/download")
    public void download(HttpServletResponse response, String name) {
        try {
            // 输入流，通过输入流读取文件
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

            // 输出流，通过输出流将文件写回浏览器，在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();
            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while((len = fileInputStream.read(bytes)) != -1) { // 没有读完时
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }

            // 关闭资源
            fileInputStream.close();
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
