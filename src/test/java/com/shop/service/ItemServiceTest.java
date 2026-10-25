package com.shop.service;

import com.shop.constant.ItemSellStatus;
import com.shop.dto.ItemFormDto;
import com.shop.entity.Item;
import com.shop.entity.ItemImg;
import com.shop.repository.ItemImgRepository;
import com.shop.repository.ItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ========================================================================
 * ItemServiceTest - 상품 서비스 기능 테스트
 * ========================================================================
 *
 * [이 테스트가 검증하는 것]
 * 관리자가 상품을 등록하는 기능이 올바르게 동작하는지 확인합니다.
 * 상품 정보(이름, 가격, 설명 등)와 함께 상품 이미지 파일이
 * 정상적으로 DB에 저장되는지를 검증합니다.
 *
 * 일상 비유:
 *   중고나라에 물건을 올릴 때를 생각해보세요.
 *   제목, 가격, 설명을 적고 사진도 첨부한 후 "등록" 버튼을 누르면,
 *   내가 입력한 정보 그대로 게시글이 올라가야 합니다.
 *   이 테스트는 그 과정이 올바르게 동작하는지 확인합니다.
 *
 * [어노테이션 설명]
 * @SpringBootTest - 실제 애플리케이션과 동일한 환경에서 테스트합니다.
 * @Transactional - 테스트 후 DB 변경사항을 자동으로 롤백(원상복구)합니다.
 * @TestPropertySource - 테스트 전용 설정 파일을 사용합니다.
 * ========================================================================
 */
@SpringBootTest
@Transactional
@TestPropertySource(locations="classpath:application-test.properties")
class ItemServiceTest {

    @Autowired
    ItemService itemService;             // 상품 등록/수정 등 비즈니스 로직을 처리하는 서비스

    @Autowired
    ItemRepository itemRepository;       // 상품 데이터를 DB에 저장/조회하는 도구

    @Autowired
    ItemImgRepository itemImgRepository; // 상품 이미지 데이터를 DB에 저장/조회하는 도구

    /**
     * 테스트용 가짜 이미지 파일 목록을 생성하는 헬퍼 메서드입니다.
     *
     * 실제로 이미지 파일을 업로드하지 않고, MockMultipartFile(가짜 파일)을 만들어서
     * 파일 업로드를 흉내냅니다.
     *
     * 비유:
     *   영화 촬영에서 실제 폭발 대신 CG(컴퓨터 그래픽)를 사용하듯,
     *   실제 이미지 파일 대신 가짜 파일 객체를 사용해서 테스트합니다.
     *   이렇게 하면 테스트가 빠르고, 실제 파일 시스템에 영향을 주지 않습니다.
     *
     * @return 5개의 가짜 이미지 파일이 담긴 리스트
     */
    List<MultipartFile> createMultipartFiles() throws Exception{

        List<MultipartFile> multipartFileList = new ArrayList<>();

        for(int i=0;i<5;i++){
            String path = "C:/shop/item/";
            String imageName = "image" + i + ".jpg";
            // MockMultipartFile: 실제 파일 없이 파일 업로드를 테스트할 수 있는 가짜 파일 객체
            MockMultipartFile multipartFile =
                    new MockMultipartFile(path, imageName, "image/jpg", new byte[]{1,2,3,4});
            multipartFileList.add(multipartFile);
        }

        return multipartFileList;
    }

    /**
     * 상품 등록 기능 테스트
     *
     * [테스트 시나리오]
     * 1. 상품 정보(이름, 가격, 설명, 판매상태, 재고)를 설정합니다.
     * 2. 가짜 이미지 파일 5개를 생성합니다.
     * 3. 상품 등록 서비스를 호출하여 상품 + 이미지를 DB에 저장합니다.
     * 4. 저장된 상품 정보가 입력한 정보와 일치하는지 하나씩 비교합니다.
     * 5. 첫 번째 이미지의 파일명이 올바르게 저장되었는지 확인합니다.
     *
     * 비유:
     *   온라인 쇼핑몰 관리자가 새 상품을 등록한 후,
     *   상품 목록 페이지에 들어가서 "내가 입력한 정보가 정확히 반영되었는지"
     *   하나씩 확인하는 과정입니다.
     *
     * @Test - JUnit에게 이 메서드가 테스트임을 알립니다.
     * @DisplayName - 테스트 결과에 "상품 등록 테스트"라고 표시됩니다.
     * @WithMockUser(username = "admin", roles = "ADMIN")
     *   - 이 테스트를 실행할 때, "admin"이라는 이름의 관리자(ADMIN) 권한을 가진
     *     가짜 사용자가 로그인한 것으로 설정합니다.
     *   - 상품 등록은 관리자만 할 수 있으므로, 관리자 권한이 필요합니다.
     *   - 비유: VIP 전용 라운지에 들어가려면 VIP 카드가 필요한 것처럼,
     *     관리자 전용 기능을 테스트하려면 관리자 자격증이 필요합니다.
     */
    @Test
    @DisplayName("상품 등록 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void saveItem() throws Exception {
        // [준비] 상품 등록 화면에서 입력받는 상품 데이터를 세팅
        ItemFormDto itemFormDto = new ItemFormDto();
        itemFormDto.setItemNm("테스트상품");
        itemFormDto.setItemSellStatus(ItemSellStatus.SELL);  // 판매 상태: 판매중
        itemFormDto.setItemDetail("테스트 상품 입니다.");
        itemFormDto.setPrice(1000);                           // 가격: 1,000원
        itemFormDto.setStockNumber(100);                      // 재고: 100개

        // [준비] 가짜 이미지 파일 5개 생성
        List<MultipartFile> multipartFileList = createMultipartFiles();

        // [실행] 상품 등록 서비스 호출 - 상품 정보 + 이미지를 DB에 저장
        Long itemId = itemService.saveItem(itemFormDto, multipartFileList);

        // [검증 준비] DB에서 저장된 이미지 목록과 상품 정보를 다시 조회
        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(itemId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new);

        // [검증] 입력한 상품 데이터와 실제 DB에 저장된 데이터가 일치하는지 비교
        assertEquals(itemFormDto.getItemNm(), item.getItemNm());                 // 상품명 확인
        assertEquals(itemFormDto.getItemSellStatus(), item.getItemSellStatus()); // 판매상태 확인
        assertEquals(itemFormDto.getItemDetail(), item.getItemDetail());         // 상세설명 확인
        assertEquals(itemFormDto.getPrice(), item.getPrice());                   // 가격 확인
        assertEquals(itemFormDto.getStockNumber(), item.getStockNumber());       // 재고 확인

        // [검증] 첫 번째 이미지의 원본 파일명이 올바르게 저장되었는지 확인
        assertEquals(multipartFileList.get(0).getOriginalFilename(), itemImgList.get(0).getOriImgName());
    }

}
