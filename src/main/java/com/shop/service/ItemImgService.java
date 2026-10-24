package com.shop.service;

import com.shop.entity.ItemImg;
import com.shop.repository.ItemImgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;
import jakarta.persistence.EntityNotFoundException;


//상품 이미지를 업로드하고, 상품 이미지 정보를 저장하는 ItemImgService 클래스 선언
@Service
@RequiredArgsConstructor
@Transactional
public class ItemImgService {

    //@Value 어노테이션을 통해 application.properties 파일에 등록한
    //#상품 이미지 업로드 경로
    //itemImgLocation=C:/shop/item
    //itemImgLocation 값을 불러와서 itemImgLocation 변수에 저장해 줍니다.
    @Value("${itemImgLocation}")
    private String itemImgLocation;

    private final ItemImgRepository itemImgRepository;

    private final FileService fileService;

    // 상품 이미지 정보를 저장하는 메서드
    // - itemImg: 저장할 상품 이미지 정보를 담고 있는 엔티티 객체
    // - itemImgFile: 사용자가 첨부한 이미지 파일 (MultipartFile 형태)
    public void saveItemImg(ItemImg itemImg, MultipartFile itemImgFile) throws Exception{

        // 사용자가 첨부한 이미지 파일의 원래(원본) 이름을 가져옵니다. (예: cat.jpg)
        String oriImgName = itemImgFile.getOriginalFilename();

        // 서버(로컬 컴퓨터)에 실제로 저장될 이미지 파일명
        // - 보통 UUID 같은 고유한 이름으로 저장됨
        String imgName = "";

        //업로드 결과 로컬에 저장된 상품 이미지 파일을 불러오는 경로
        // 웹 브라우저에서 이미지를 불러올 수 있는 URL 경로
        String imgUrl = "";

        // 사용자가 이미지 파일을 첨부했다면 (즉, oriImgName이 비어있지 않다면)
        if(!StringUtils.isEmpty(oriImgName)){
            // 파일을 서버에 업로드합니다.
            // - itemImgLocation: 이미지 저장 경로 (예: C:/shop/item)
            // - oriImgName: 사용자가 첨부한 파일 이름
            // - itemImgFile.getBytes(): 첨부한 실제 이미지 데이터 (byte 배열)
            //
            // uploadFile() 메서드는 이미지를 저장한 후, 저장된 파일명을 반환합니다.
            imgName = fileService.uploadFile(itemImgLocation, oriImgName, itemImgFile.getBytes());
                                            //C:/shop/item  , 업로드한 상품 이미지 파일의 원본 이름,  첨부된 이미지 파일의 내용이 담긴  바이트 배열(byte[])

            // 저장된 이미지 파일을 웹에서 접근할 수 있도록 URL 경로를 설정합니다.
            // 예: "/images/item/파일명"
            //
            // 이 경로는 WebMvcConfig에서 "/images/**"로 매핑되어 있어야 하며,
            // application.properties 파일의 uploadPath 설정과 일치해야 합니다.
            imgUrl = "/images/item/" + imgName;
        }

        // 이미지 정보를 ItemImg 엔티티에 저장합니다.
        // - oriImgName: 원본 파일 이름
        // - imgName: 서버에 저장된 파일 이름
        // - imgUrl: 웹에서 이미지를 불러올 수 있는 경로
        itemImg.updateItemImg(oriImgName, imgName, imgUrl);

        // 최종적으로 상품 이미지 정보를 DB에 저장합니다. (INSERT 수행)
        itemImgRepository.save(itemImg);
    }

// 상품 이미지 정보를 수정하는 메서드입니다.
// 사용자가 상품 정보를 수정할 때 새로운 이미지를 첨부한 경우, 기존 이미지 파일은 삭제하고 새 이미지 파일을 업로드하며,
// 해당 상품 이미지 정보를 DB에서 찾아 새 이미지 정보로 갱신(업데이트)합니다.
// 매개변수:
// - itemImgId: 수정하고자 하는 상품 이미지의 고유 ID (DB에서 어떤 이미지를 수정할 것인지 구분하는 값)
// - itemImgFile: 사용자가 새로 첨부한 이미지 파일 (MultipartFile 타입으로 업로드된 이미지 파일을 의미)
    public void updateItemImg(Long itemImgId, MultipartFile itemImgFile) throws Exception {

        // 1. 사용자가 새로운 이미지 파일을 첨부했는지 확인합니다.
        //    isEmpty()는 첨부된 파일이 없으면 true를 반환하므로, "!"를 붙여서 파일이 존재할 경우만 아래 코드를 실행합니다.
        if (!itemImgFile.isEmpty()) {

            // 2. DB에서 수정 대상인 상품 이미지 정보를 가져옵니다.
            //    itemImgId로 DB를 조회해서 일치하는 ItemImg 엔티티 객체를 찾아옵니다.
            //    만약 해당 ID에 해당하는 이미지가 없다면, 예외(EntityNotFoundException)를 발생시켜 오류를 던집니다.
            ItemImg savedItemImg = itemImgRepository.findById(itemImgId)
                    .orElseThrow(EntityNotFoundException::new);

            // 3. 기존에 서버에 저장되어 있는 이미지 파일이 있다면 삭제해 줍니다.
            //    getImgName()은 서버에 저장된 파일 이름을 의미합니다.
            //    StringUtils.isEmpty()는 null 또는 빈 문자열("")인 경우 true를 반환합니다.
            if (!StringUtils.isEmpty(savedItemImg.getImgName())) {
                // 파일 삭제를 위해, 저장 경로(itemImgLocation)와 기존 파일 이름을 이어붙여 전체 경로를 만듭니다.
                // 예: "C:/shop/item/기존파일이름.jpg"
                fileService.deleteFile(itemImgLocation + "/" + savedItemImg.getImgName());

                // fileService.deleteFile()은 주어진 경로의 파일을 실제로 파일 시스템에서 삭제하는 메서드입니다.
                // 내부적으로는 java.io.File 또는 java.nio.file 패키지를 이용해 삭제를 수행할 수 있습니다.
            }

            // 4. 사용자가 새로 첨부한 이미지 파일의 원본 파일명을 가져옵니다.
            //    getOriginalFilename()은 사용자가 업로드한 실제 파일명을 반환합니다.
            //    예: 사용자가 "cat.jpg"를 업로드했다면 "cat.jpg"가 oriImgName에 저장됩니다.
            String oriImgName = itemImgFile.getOriginalFilename();

            // 5. 업로드한 이미지 파일의 실제 내용을 byte 배열(byte[]) 형태로 추출하여 서버에 저장합니다.
            //    uploadFile() 메서드를 호출해서 이미지를 저장하고, 저장된 실제 파일명을 반환받습니다.
            //    이 파일명은 서버에서 중복되지 않도록 UUID 등을 붙여서 자동 생성할 수도 있습니다.
            String imgName = fileService.uploadFile(
                    itemImgLocation,        // 서버에 파일을 저장할 디렉터리 경로 (예: "C:/shop/item")
                    oriImgName,             // 사용자가 업로드한 원본 파일명 (예: "cat.jpg")
                    itemImgFile.getBytes()  // 파일 내용을 바이트 배열로 변환한 것 (파일 자체의 데이터)
            );

            // fileService.uploadFile()은 실제로 파일을 서버 디스크에 저장하는 역할을 합니다.
            // 내부적으로는 디렉터리 생성, 파일 이름 처리, FileOutputStream 등을 사용할 수 있습니다.

            // 6. 저장된 파일을 웹 페이지에서 접근할 수 있도록 URL을 만들어 줍니다.
            //    "/images/item/" 경로는 정적 리소스 경로 설정에 따라 외부 접근이 가능하도록 지정된 경로입니다.
            //    imgName은 서버에 저장된 실제 파일명입니다.
            //    예: "/images/item/UUID_1234.jpg"
            String imgUrl = "/images/item/" + imgName;

            // 7. 기존에 조회한 상품 이미지 객체(ItemImg)의 속성 값을 새 이미지 정보로 갱신합니다.
            //    updateItemImg() 메서드는 해당 엔티티의 필드 값을 변경하는 메서드입니다.

            // 여기서 중요한 점은  상품 등록 할때 처험 itemImgRepository.save()메소드 로직을 작성해서 호출하지 않는다는 것입니다.
            //  ItemImg 엔티티 <--savedItemImg  는 현재 Item 엔티티에 영속상태이므로 데이터를 변경해서
            //  ItemImg클래스의 변수에 저장하는 것만으로 이 시점에는 DB에 반영되지는 않지만,
            //  트랜잭션이 끝나는 시점에 JPA가 변경 내용을 감지하고 DB에 UPDATE(UPDATE 쿼리가 실행 된다는 의미)합니다.
            savedItemImg.updateItemImg(oriImgName, imgName, imgUrl);

            // 즉, 실제로 DB에 UPDATE 쿼리가 실행되는 시점은 이 메서드가 끝나고 트랜잭션이 종료될 때입니다.
            // (JPA의 "변경 감지" 기능을 통해 자동 반영됩니다.)
        }//-- if

        // 참고: 만약 itemImgFile이 비어 있다면 아무 작업도 하지 않고 메서드가 끝납니다.
    }


}