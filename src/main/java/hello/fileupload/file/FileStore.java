package hello.fileupload.file;

import hello.fileupload.domain.UploadFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class FileStore {

    @Value("${file.dir}")
    private String fileDir;

    public String getFullPath(String filename){
        return fileDir + filename;
    }

    public List<UploadFile> storeFiles(List<MultipartFile> multipartFiles) throws IOException {
        List<UploadFile> storeFileResult = new ArrayList<>();

        for( MultipartFile multipartFile: multipartFiles){
            if(!multipartFile.isEmpty()){
                storeFileResult.add(storeFile(multipartFile));
            }
        }
        return storeFileResult;
    }

    public UploadFile storeFile(MultipartFile multipartFile) throws IOException {
        if(multipartFile.isEmpty()){
            return null;
        }

        String originalFilename = multipartFile.getOriginalFilename();
        // image.png
        // 서버에 저장하는 파일명 -> 확장자는 살려야함, 이게 이미지인지 실행파일인지 등등을 구별해야되기 때문
        String storeFilename= createStoreFileName(originalFilename);
        multipartFile.transferTo(new File(getFullPath(storeFilename)));

        return new UploadFile(originalFilename, storeFilename);
    }

    private String createStoreFileName(String originalFilename) {
        String uuid= UUID.randomUUID().toString();
        String ext= extractExt(originalFilename);

        return uuid+"."+ext;
    }

    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos+1);
    }


}
