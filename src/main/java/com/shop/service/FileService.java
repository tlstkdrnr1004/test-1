package com.shop.service;

import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

/**
 * ============================================
 * 파일 업로드/삭제 서비스 (FileService)
 * ============================================
 * 서버에 파일(이미지)을 저장하거나 삭제하는 기능을 담당하는 서비스 클래스입니다.
 *
 * 비유: "동네 사진관"과 같습니다.
 * 1. 손님(사용자)이 사진(파일)을 가져오면
 * 2. 사진관 주인이 고유한 접수 번호(UUID)를 붙여서
 * 3. 사진 보관함(서버 폴더)에 정리해서 넣어줍니다.
 * 4. 나중에 손님이 "그 사진 버려주세요"라고 하면 보관함에서 꺼내 삭제합니다.
 *
 * 왜 고유 번호(UUID)를 붙이나요?
 * - 여러 손님이 같은 이름의 사진(예: "photo.jpg")을 가져올 수 있기 때문입니다.
 * - 접수 번호를 붙이면 "a3f2b1c4-photo.jpg"처럼 절대 겹치지 않는 이름이 됩니다.
 */
@Service  // Spring에게 "이 클래스는 비즈니스 로직을 처리하는 서비스야"라고 알려줍니다
@Log      // Lombok: log.info("메시지") 형태로 로그를 남길 수 있게 해줍니다
public class FileService {

    /**
     * 파일을 서버에 업로드(저장)합니다.
     *
     * 비유: 사진관에서 손님이 가져온 사진을 접수하는 과정입니다.
     * 1) 사진에 고유 접수번호(UUID)를 부여하고
     * 2) 원래 확장자(.jpg, .png 등)를 유지한 채 새 이름을 만들고
     * 3) 보관함(서버 폴더)에 저장한 뒤
     * 4) 새 이름(접수번호 + 확장자)을 손님에게 알려줍니다
     *
     * @param uploadPath       - 파일이 저장될 서버 폴더 경로 (예: "C:/shop/item")
     *                           비유: 사진 보관함의 위치 (몇 번 서랍인지)
     * @param originalFileName - 사용자가 업로드한 원래 파일 이름 (예: "my_photo.jpg")
     *                           비유: 손님이 가져온 사진의 원래 이름
     * @param fileData         - 파일의 실제 데이터 (byte 배열)
     *                           비유: 사진 그 자체 (디지털 데이터)
     * @return savedFileName   - 서버에 저장된 새 파일 이름 (예: "a3f2b1c4-xxxx.jpg")
     *                           비유: 접수 번호가 붙은 새 이름 (이 이름으로 나중에 찾을 수 있음)
     * @throws Exception       - 파일 저장 중 오류 발생 시 예외를 던집니다
     */
    public String uploadFile(String uploadPath, String originalFileName, byte[] fileData) throws Exception{

        // ✅ [1단계] 고유한 접수번호(UUID) 생성
        // UUID(Universally Unique Identifier) = 전 세계에서 유일한 식별자
        // 비유: 주민등록번호처럼 절대 겹치지 않는 고유 번호입니다.
        //       예: "550e8400-e29b-41d4-a716-446655440000"
        //       이렇게 하면 "photo.jpg"를 100명이 올려도 파일명이 겹치지 않습니다.
        UUID uuid = UUID.randomUUID();

        // ✅ [2단계] 원본 파일에서 확장자 추출
        // "my_photo.jpg" → 마지막 "." 위치를 찾아서 → ".jpg"만 꺼냄
        // 비유: 사진이 컬러인지 흑백인지(=파일 형식)를 확인하는 것
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));

        // ✅ [3단계] UUID + 확장자를 합쳐서 새 파일 이름 생성
        // 예: "550e8400-e29b-41d4-a716-446655440000" + ".jpg" = "550e8400-...-.jpg"
        // 비유: 접수번호 + 원래 형식 = 보관함에 붙일 라벨
        String savedFileName = uuid.toString() + extension;

        // ✅ [4단계] 저장될 전체 경로 생성
        // 예: "C:/shop/item" + "/" + "550e8400-...-.jpg" = "C:/shop/item/550e8400-...-.jpg"
        String fileUploadFullUrl = uploadPath + "/" + savedFileName;

        // ✅ [5단계] 파일 출력 스트림 생성 → 실제 파일 데이터를 서버에 기록
        // FileOutputStream: 데이터를 파일로 "흘려보내는" 파이프(통로)
        // 비유: 사진 보관함 서랍을 열고(FileOutputStream 생성),
        //       사진을 넣고(write), 서랍을 닫는(close) 과정입니다.
        FileOutputStream fos = new FileOutputStream(fileUploadFullUrl);

        fos.write(fileData);   // 파일 데이터(사진)를 서버 폴더에 기록합니다
        fos.close();           // 파일 출력 스트림을 닫습니다 (서랍 닫기)

        return savedFileName;  // ✅ [6단계] 저장된 파일 이름을 반환 (이 이름으로 나중에 조회/삭제 가능)
    }

    /**
     * 서버에 저장된 파일을 삭제합니다.
     *
     * 비유: 사진관에서 "이 사진 폐기해주세요"라는 요청을 처리하는 것입니다.
     * 보관함에서 해당 사진을 찾아보고, 있으면 삭제하고, 없으면 "이미 없습니다"라고 알려줍니다.
     *
     * @param filePath - 삭제할 파일의 전체 경로 (예: "C:/shop/item/550e8400-...-.jpg")
     *                   비유: 보관함에서 찾을 사진의 위치 (서랍 번호 + 라벨)
     * @throws Exception - 파일 삭제 중 오류 발생 시 예외를 던집니다
     */
    public void deleteFile(String filePath) throws Exception{
        // ✅ 파일 경로를 기반으로 File 객체 생성 (아직 삭제한 것이 아니라, "이 파일을 가리키는 리모컨"을 만든 것)
        File deleteFile = new File(filePath);

        // ✅ 해당 파일이 실제로 존재하는지 확인
        if(deleteFile.exists()) {
            deleteFile.delete();                        // 파일이 있으면 삭제 실행
            log.info("파일을 삭제하였습니다.");            // 삭제 성공 로그 출력
        } else {
            log.info("파일이 존재하지 않습니다.");         // 파일이 없으면 안내 로그 출력
        }
    }

}