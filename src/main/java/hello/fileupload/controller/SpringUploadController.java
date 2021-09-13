package hello.fileupload.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@Slf4j
@Controller
@RequestMapping("/spring")
public class SpringUploadController {

    @Value("${file.dir}")
    private String fileDir;

    @GetMapping("/upload")
    public String newFile(){
        return "upload-form";
    }

    @PostMapping("/upload")
    public String saveFile(@RequestParam String itemName, // MultipartFile을 그대로 받아와서 사용 가능
                           @RequestParam MultipartFile file, HttpServletRequest request) throws IOException {
        log.info("request= {}", request);
        log.info("itemName= {} ", itemName);
        log.info("multipartFile= {}", file);

        if(!file.isEmpty()){
            String fullPath = fileDir + file.getOriginalFilename();
            log.info("파일 저장 fullPath= {}", fullPath);
            file.transferTo(new File(fullPath)); // 파일을 해당 경로에 저장해준다
        }
        return "upload-form";
    }
}
