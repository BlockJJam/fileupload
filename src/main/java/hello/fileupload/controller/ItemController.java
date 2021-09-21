package hello.fileupload.controller;

import hello.fileupload.domain.Item;
import hello.fileupload.domain.ItemRepository;
import hello.fileupload.domain.UploadFile;
import hello.fileupload.file.FileStore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemController {
    private final ItemRepository itemRepository;
    private final FileStore fileStore;

    /**
     * item form html파일 전송 기능
     * @param form
     * @return
     */
    @GetMapping("/items/new")
    public String newItem(@ModelAttribute ItemForm form) {
        return "item-form";
    }

    /**
     * 입력받은 form 정보를 통해 일반 파일과 이미지 파일 목록을 저장하는 기능
     * @param form
     * @param redirectAttributes
     * @return
     * @throws IOException
     */
    @PostMapping("/items/new")
    public String saveItem(@ModelAttribute ItemForm form, RedirectAttributes redirectAttributes) throws IOException {
        UploadFile attachFile = fileStore.storeFile(form.getAttachFile());
        List<UploadFile> storeImageFiles = fileStore.storeFiles(form.getImageFiles());

        //database에 저장
        Item item = new Item();
        item.setItemName(form.getItemName());
        item.setAttachFile(attachFile);
        item.setImageFiles(storeImageFiles);
        itemRepository.save(item);

        redirectAttributes.addAttribute("itemId", item.getId());
        return "redirect:/items/{itemId}";
    }

    /**
     * 저장한 파일 정보를 보여주는 html 파일 전송 기
     * @param id
     * @param model
     * @return
     */
    @GetMapping("/items/{id}")
    public String items(@PathVariable Long id, Model model){
        Item item = itemRepository.findById(id);
        model.addAttribute("item", item);
        return "item-view";
    }

    /**
     * 파일명을 가지고 filestore에서 파일전체 경로로 파일을 전송해주는 기능
     * (보안에 좀 취약한 점이 있다)
     * @param filename
     * @return
     * @throws MalformedURLException
     */
    @ResponseBody
    @GetMapping("/images/{filename}")
    public Resource downloadImage(@PathVariable String filename) throws MalformedURLException {
        // "file:/Users/tys/study/.../682......-c011281.img -> 해당 경로의 파일을 return
        return new UrlResource("file:"+ fileStore.getFullPath(filename));
    }

    /**
     * 첨부파일에 있는 파일 ID를 가져와서 해당 파일을 클라이언트 PC에 다운로드하는 기능
     * @param itemId
     * @return
     * @throws MalformedURLException
     */
    @GetMapping("/attach/{itemId}")
    public ResponseEntity<Resource> downloadAttach(@PathVariable Long itemId) throws MalformedURLException {
        Item item = itemRepository.findById(itemId);
        String storeFileName = item.getAttachFile().getStoreFileName();
        String uploadFileName = item.getAttachFile().getUploadFileName();

        UrlResource urlResource = new UrlResource("file:"+fileStore.getFullPath(storeFileName));

        log.info("uploadFileName = {}", uploadFileName);

        // browser가 헤더의 해당 값을 보고, 파일을 내려받도록 해준다.
        // 한글이 깨지는 것까지 신경써준다면
        String encodedUploadFileName = UriUtils.encode(uploadFileName, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedUploadFileName +"\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,contentDisposition)
                .body(urlResource);
    }
}
