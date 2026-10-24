package com.shop.service;

import com.shop.dto.ItemFormDto;
import com.shop.entity.Item;
import com.shop.entity.ItemImg;
import com.shop.repository.ItemImgRepository;
import com.shop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import com.shop.dto.ItemImgDto;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;

import com.shop.dto.ItemSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.shop.dto.MainItemDto;


//상품을 등록하는  ItemService 클래스 선언
@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    private final ItemImgService itemImgService; //상품 이미지를 등록하는 부장 서비스 의존 주입

    private final ItemImgRepository itemImgRepository;

    //상품 등록 비즈니스 로직 
    public Long saveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception{

        //상품 등록
        //상품 등록 폼으로 부터 입력 받은 데이터 ItemFormDto의 정보를 이용하여 item 객체를 생성함.
        //요약 : ItemFormDto -> Item엔티티 객체로 변환
        Item item = itemFormDto.createItem();
        itemRepository.save(item);//상품 데이터를 DB에 저장(등록)함.

        //상품 이미지 등록
        //상품 등록 폼에서 상품등록시 첨부한 이미지 갯수 만큼 반복 해서
        for(int i=0;i<itemImgFileList.size();i++){
            //상품 이미지 엔티티 객체 반복해서 생성
            ItemImg itemImg = new ItemImg();
            itemImg.setItem(item);  //폼에서 상품등록시 입력한 정보가 저장된 상품 엔티티(Item)객체
                                    //ItemImg엔티티의 Item item변수에 주입(저장)

            if(i == 0)//폼에서 첨부한 이미지가 첫 번째 상품 이미지일 경우
                //대표 상품 이미지 여부 값을 'Y'로 저장합니다.
                itemImg.setRepimgYn("Y");
            else
                //폼에서 첨부한 나머지 상품 이미지는 'N'으로 저장합니다.
                itemImg.setRepimgYn("N");

            //DB에 INSERT (첨부한 상품 이미지 정보 반복해서 저장)
            //               new ItemImg()엔티티,   MultipartFile 첨부한 이미지 아이템 전달
            itemImgService.saveItemImg(itemImg, itemImgFileList.get(i));

        }//---for

        return item.getId();//등록한 상품의 아이디 반환
    }

// 등록된 상품을 불러오는 메소드
// 1. 상품 데이터를 조회해서 읽어오는 트랜잭션을 읽기(조회) 전용으로 설정합니다.
//    이럴 경우 JPA가 더티체킹(변경감지)을 수행하지 않아서 조회 성능을 향상 시킬 수 있습니다.
    @Transactional(readOnly = true)
    public ItemFormDto getItemDtl(Long itemId) { // 조회할 상품의 id를 매개변수로 받음

        // 2. 해당 상품의 이미지 정보를 조회합니다.
        // 등록순으로 조회해서 가져오기 위해서 상품 이미지 아이디를 기준으로 조회해 가져오겠습니다.
        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(itemId);
        // 상품 ID로 연관된 이미지들을 등록 순서(오름차순)로 조회

        // 3. 조회한 ItemImg 엔티티를 ItemImgDto로 변환하여 리스트에 추가합니다.
        List<ItemImgDto> itemImgDtoList = new ArrayList<>(); // 변환된 이미지 DTO를 담을 리스트
        for (ItemImg itemImg : itemImgList) {
            ItemImgDto itemImgDto = ItemImgDto.of(itemImg); // 엔티티를 DTO로 변환
            itemImgDtoList.add(itemImgDto); // 리스트에 추가
        }

        // 4. 조회할 상품의 id를 통해서 상품 엔티티를 조회합니다.
        //    존재하지 않을 때는 EntityNotFoundException을 강제로 발생시킵니다.
        Item item = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new); // 상품이 없으면 예외 발생

        ItemFormDto itemFormDto = ItemFormDto.of(item); // 상품 엔티티를 DTO로 변환

        itemFormDto.setItemImgDtoList(itemImgDtoList); // 이미지 정보 DTO 리스트를 상품 DTO에 추가

        return itemFormDto; // 최종 결과로 완성된 조회된 상품 DTO 반환
    }

    //  상품 수정 로직 실행  메소드
    //    - itemFormDto: 사용자가 수정한 상품 정보 (이름, 가격, 재고 등)
    //    - itemImgFileList: 사용자가 새로 첨부한 이미지 파일 목록
    //    updateItem()은 내부적으로 기존 상품 정보를 찾아서 새로운 정보로 업데이트합니다.
    public Long updateItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception{
        //상품 수정
        //1. 상품 수정 화면으로 부터 전달 받은 상품 아이디를 이용하여 수정할 상품 엔티티 Item 대상을  DB에서 조회합니다.
        Item item = itemRepository.findById(itemFormDto.getId())
                                  .orElseThrow(EntityNotFoundException::new);

        //2. 상품 수정 화면으로 부터 전달 받은 ItemFormDto의 각변수의 수정 반영할 정보들을
        //   DB에서 조회된 상품 엔티티 Item의 각 변수에 저장(반영,업데이트) 합니다.
        item.updateItem(itemFormDto);

        //3. 수정할 상품의 상품이미지의 아이디를 리스트로 반환 받습니다.
        List<Long> itemImgIds = itemFormDto.getItemImgIds();

        //4. 상품 수정시 참부한 이미지 아이템의 갯수 만큼 반복 해서 수정할 이미지 DB에 UPDATE ***** 중요!
        for(int i=0;i<itemImgFileList.size();i++){

            //상품 이미지 정보를 UPDATE 하기 위해서  updateItemImg()메소드 호출시 상품 이미지 아이디와,  수정시 첨부한 상품 이미지 파일 정보 전달
            itemImgService.updateItemImg(itemImgIds.get(i),itemImgFileList.get(i));
        }

        return item.getId();
    }


// 이 메서드는 관리자 페이지에서 사용할 상품 목록 조회 기능을 수행합니다.
// 상품 검색 조건(ItemSearchDto)과 페이징 처리 정보(Pageable)를 매개변수로 받아,
// 조건에 맞는 상품 목록을 페이지 단위로 조회해서 반환합니다.

// 이 메서드에서는 단순히 데이터를 조회만 하고 변경하지 않기 때문에,
// 성능 최적화를 위해 @Transactional(readOnly = true)를 사용합니다.
// 이 어노테이션은 트랜잭션을 읽기 전용으로 설정해주며,
// JPA가 영속성 컨텍스트(1차 캐시 등)를 무겁게 관리하지 않아도 되므로 성능이 향상될 수 있습니다.

// @Transactional은 메서드나 클래스에 붙여 트랜잭션 처리 범위를 지정하는 어노테이션입니다.
// - readOnly = true : 읽기 전용 트랜잭션이므로, 쓰기(저장, 수정, 삭제) 연산이 발생하면 예외가 발생할 수도 있습니다.
    @Transactional(readOnly = true)
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        // itemRepository.getAdminItemPage 메서드를 호출하여 DB로부터 상품 데이터를 조회합니다.
        // - itemSearchDto : 검색 조건(예: 상품명, 등록일, 판매 상태 등)을 담고 있는 DTO 객체
        // - pageable : 현재 페이지 번호, 페이지 크기, 정렬 조건 등을 담고 있는 객체
        // 반환 타입 Page<Item>은 Spring Data JPA에서 제공하는 페이징 처리 결과를 담는 객체입니다.
        // - Page는 단순한 리스트가 아니라, 총 페이지 수, 전체 항목 수, 현재 페이지 번호 등의 정보도 함께 제공합니다.
        return itemRepository.getAdminItemPage(itemSearchDto, pageable);
    }



// [설명]
// 이 메서드는 메인 화면(예: 쇼핑몰 첫 화면)에 보여줄 상품 리스트를 가져오는 역할을 합니다.
// 사용자가 입력한 검색 조건과 페이징 정보를 이용해서 DB에서 상품 데이터를 조회합니다.

// 데이터베이스에서 단순히 조회(Read)만 수행하므로,
// @Transactional(readOnly = true) 어노테이션을 사용해서 트랜잭션을 읽기 전용으로 설정합니다.
// 이렇게 하면 성능이 좋아지고, 불필요한 변경 감지를 하지 않기 때문에 메모리 사용도 줄어듭니다.

// ▷ @Transactional(readOnly = true)
//   - 이 메서드 내에서 데이터 변경(INSERT, UPDATE, DELETE)은 없다는 것을 선언합니다.
//   - JPA는 이 정보를 바탕으로 "쓰기 관련 리소스"를 최소화하여 성능을 향상시킵니다.

    @Transactional(readOnly = true)
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {

        // itemRepository.getMainItemPage(...) 메서드를 호출하여 실제 DB에서 데이터를 조회합니다.
        // 이 메서드는 조건에 맞는 상품 데이터를 검색하여 "페이지 단위로" 결과를 반환합니다.

        // ▷ 매개변수 설명:
        //   - itemSearchDto : 사용자가 입력한 검색 조건을 담고 있는 DTO입니다.
        //                     예를 들어 상품명, 카테고리, 등록일 같은 조건이 들어있을 수 있습니다.
        //
        //   - pageable : 페이징 처리 정보를 담고 있는 객체입니다.
        //                예를 들어 "2페이지", "한 페이지에 12개", "최신순 정렬"과 같은 정보가 담겨 있습니다.

        // ▷ 반환값 설명:
        //   - Page<MainItemDto> : 메인 페이지에 보여줄 상품 정보를 담은 MainItemDto 객체의 리스트입니다.
        //                         Page 객체는 리스트뿐 아니라, 전체 페이지 수, 현재 페이지 번호,
        //                         전체 항목 수 등의 정보도 함께 제공합니다.

        return itemRepository.getMainItemPage(itemSearchDto, pageable);
    }


}