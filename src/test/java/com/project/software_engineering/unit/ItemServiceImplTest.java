package com.project.software_engineering.unit;

import com.project.software_engineering.domain.*;
import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.ItemDto;
import com.project.software_engineering.exception.NoMatchingDataException;
import com.project.software_engineering.mapper.ItemMapper;
import com.project.software_engineering.repository.ItemRepository;
import com.project.software_engineering.repository.UserRepository;
import com.project.software_engineering.service.AIService;
import com.project.software_engineering.service.PermittedService;
import com.project.software_engineering.service.S3Service;
import com.project.software_engineering.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * ====================================================================
 * [Unit Test] ItemServiceImpl
 * ====================================================================
 * 테스트 전략: 서비스 레이어 비즈니스 로직 검증
 *  - ItemRepository, PermittedService, S3Service, AIService, UserRepository,
 *    ItemMapper 등 모든 협력 객체를 Mock 처리 (완전 고립 테스트)
 *  - Setup → Call → Assertion 구조 준수
 *  - 등가 분할: 정상/경계/오류 케이스 포함
 * ====================================================================
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("[Unit Test] ItemServiceImpl - 분실물/습득물 비즈니스 로직")
class ItemServiceImplTest {

    @Mock private PermittedService permittedService;
    @Mock private ItemRepository itemRepository;
    @Mock private UserRepository userRepository;
    @Mock private ItemMapper itemMapper;
    @Mock private AIService aiService;
    @Mock private S3Service s3Service;

    @InjectMocks
    private ItemServiceImpl itemService;

    // 공통 픽스처
    private static final Long REQ_USER_ID = -200L; // 권한 우회 ID
    private static final Long ITEM_ID = 100L;

    /**
     * 테스트용 Item 엔티티를 생성하는 헬퍼 메서드
     */
    private Item buildItem(Long id, ItemStatus status) {
        Item item = Item.of("테스트 아이템", "테스트 내용", Location.GLC,
                LocalDateTime.now().minusHours(1), LocalDateTime.now(),
                Category.ELECTRONICS, status);
        ReflectionTestUtils.setField(item, "id", id);
        return item;
    }

    // =================================================================
    // TC-U-11 ~ TC-U-15: create() — 아이템 등록
    // =================================================================
    @Nested
    @DisplayName("create() - 아이템 등록 로직")
    class CreateTests {

        @Test
        @DisplayName("TC-U-11: [정상] 이미지 없이 아이템 등록 시 → DB에 저장되고 ID가 반환된다")
        void create_withoutImages_savesItemAndReturnsId() {
            // ── Setup ──
            ItemDto.CreateReqDto dto = ItemDto.CreateReqDto.builder()
                    .title("지갑 분실")
                    .content("GLC 강의실에서 지갑을 잃어버렸습니다.")
                    .location(Location.GLC)
                    .status(ItemStatus.LOST)
                    .category(Category.WALLET)
                    .startTime(LocalDateTime.now().minusHours(1))
                    .endTime(LocalDateTime.now())
                    .build();

            Item savedItem = buildItem(ITEM_ID, ItemStatus.LOST);
            given(itemRepository.save(any(Item.class))).willReturn(savedItem);

            // ── Call ──
            DefaultDto.CreateResDto result = itemService.create(dto, null, REQ_USER_ID);

            // ── Assertion ──
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(ITEM_ID);

            // S3 업로드 및 AI 서버 호출이 발생하지 않아야 함
            then(s3Service).should(never()).uploadFile(any());
            then(aiService).should(never()).registerItemToAIAsync(any(), any());
        }

        @Test
        @DisplayName("TC-U-12: [정상] 이미지와 함께 아이템 등록 시 → S3 업로드 후 AI 서버에 비동기 등록된다")
        void create_withImages_uploadsToS3AndCallsAIAsync() {
            // ── Setup ──
            ItemDto.CreateReqDto dto = ItemDto.CreateReqDto.builder()
                    .title("갤럭시 S24 분실")
                    .content("도서관에서 핸드폰을 잃어버렸습니다.")
                    .location(Location.LIBRARY)
                    .status(ItemStatus.LOST)
                    .category(Category.ELECTRONICS)
                    .startTime(LocalDateTime.now().minusHours(2))
                    .endTime(LocalDateTime.now())
                    .build();

            // 이미지 Mock
            MultipartFile mockFile = mock(MultipartFile.class);
            given(mockFile.isEmpty()).willReturn(false);
            String s3Url = "https://s3.amazonaws.com/software-engineering-foundit/image.jpg";
            given(s3Service.uploadFile(mockFile)).willReturn(s3Url);

            // DB 저장 Mock - 이미지가 포함된 Item 반환
            Item savedItem = buildItem(ITEM_ID, ItemStatus.LOST);
            Images image = new Images(s3Url);
            savedItem.addImage(image);
            given(itemRepository.save(any(Item.class))).willReturn(savedItem);

            // ── Call ──
            DefaultDto.CreateResDto result = itemService.create(dto, List.of(mockFile), REQ_USER_ID);

            // ── Assertion ──
            assertThat(result.getId()).isEqualTo(ITEM_ID);
            then(s3Service).should(times(1)).uploadFile(mockFile);
            then(aiService).should(times(1)).registerItemToAIAsync(eq(savedItem), anyList());
        }

        @Test
        @DisplayName("TC-U-13: [경계값] 빈 이미지 파일 포함 시 → S3 업로드를 건너뛴다")
        void create_withEmptyImageFile_skipsS3Upload() {
            // ── Setup ──
            ItemDto.CreateReqDto dto = ItemDto.CreateReqDto.builder()
                    .title("테스트")
                    .status(ItemStatus.FOUND)
                    .build();

            MultipartFile emptyFile = mock(MultipartFile.class);
            given(emptyFile.isEmpty()).willReturn(true); // 빈 파일

            Item savedItem = buildItem(ITEM_ID, ItemStatus.FOUND);
            given(itemRepository.save(any(Item.class))).willReturn(savedItem);

            // ── Call ──
            itemService.create(dto, List.of(emptyFile), REQ_USER_ID);

            // ── Assertion ──
            then(s3Service).should(never()).uploadFile(any());
        }
    }

    // =================================================================
    // TC-U-16 ~ TC-U-19: update() — 아이템 수정
    // =================================================================
    @Nested
    @DisplayName("update() - 아이템 정보 수정 로직")
    class UpdateTests {

        @Test
        @DisplayName("TC-U-16: [정상] 존재하는 아이템 수정 시 → 필드가 변경되고 저장된다")
        void update_existingItem_updatesFieldsAndSaves() {
            // ── Setup ──
            Item existingItem = buildItem(ITEM_ID, ItemStatus.LOST);
            ItemDto.UpdateReqDto dto = ItemDto.UpdateReqDto.builder()
                    .id(ITEM_ID)
                    .title("수정된 제목")
                    .status(ItemStatus.COMPLETE)
                    .build();

            given(itemRepository.findById(ITEM_ID)).willReturn(Optional.of(existingItem));
            given(itemRepository.save(any(Item.class))).willReturn(existingItem);

            // ── Call ──
            itemService.update(dto, REQ_USER_ID);

            // ── Assertion ──
            assertThat(existingItem.getTitle()).isEqualTo("수정된 제목");
            assertThat(existingItem.getStatus()).isEqualTo(ItemStatus.COMPLETE);
            then(itemRepository).should(times(1)).save(existingItem);
        }

        @Test
        @DisplayName("TC-U-17: [오류] 존재하지 않는 아이템 수정 시 → NoMatchingDataException이 발생한다")
        void update_nonExistentItem_throwsNoMatchingDataException() {
            // ── Setup ──
            ItemDto.UpdateReqDto dto = ItemDto.UpdateReqDto.builder()
                    .id(999L)
                    .title("존재하지 않는 아이템")
                    .build();

            given(itemRepository.findById(999L)).willReturn(Optional.empty());

            // ── Call & Assertion ──
            assertThatThrownBy(() -> itemService.update(dto, REQ_USER_ID))
                    .isInstanceOf(NoMatchingDataException.class)
                    .hasMessage("no data");

            then(itemRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("TC-U-18: [경계값] null 필드만 포함된 수정 요청 시 → 기존 값이 유지된다")
        void update_allNullFields_preservesExistingValues() {
            // ── Setup ──
            Item existingItem = buildItem(ITEM_ID, ItemStatus.LOST);
            existingItem.setTitle("기존 제목");

            ItemDto.UpdateReqDto dto = ItemDto.UpdateReqDto.builder()
                    .id(ITEM_ID)
                    // title, content, status 등 모두 null
                    .build();

            given(itemRepository.findById(ITEM_ID)).willReturn(Optional.of(existingItem));
            given(itemRepository.save(any(Item.class))).willReturn(existingItem);

            // ── Call ──
            itemService.update(dto, REQ_USER_ID);

            // ── Assertion ──
            assertThat(existingItem.getTitle()).isEqualTo("기존 제목"); // 원래 값 유지
            assertThat(existingItem.getStatus()).isEqualTo(ItemStatus.LOST);
        }
    }

    // =================================================================
    // TC-U-20 ~ TC-U-21: delete() / deleteList() — 논리 삭제
    // =================================================================
    @Nested
    @DisplayName("delete() - 논리 삭제 로직")
    class DeleteTests {

        @Test
        @DisplayName("TC-U-20: [정상] 논리 삭제 시 → deleted 필드가 true로 변경된다")
        void delete_existingItem_setsDeletedTrue() {
            // ── Setup ──
            Item existingItem = buildItem(ITEM_ID, ItemStatus.LOST);
            existingItem.setDeleted(false);
            DefaultDto.DeleteReqDto dto = DefaultDto.DeleteReqDto.builder().id(ITEM_ID).build();

            given(itemRepository.findById(ITEM_ID)).willReturn(Optional.of(existingItem));
            given(itemRepository.save(any(Item.class))).willReturn(existingItem);

            // ── Call ──
            itemService.delete(dto, REQ_USER_ID);

            // ── Assertion ──
            assertThat(existingItem.getDeleted()).isTrue();
        }

        @Test
        @DisplayName("TC-U-21: [정상] 복수 삭제 시 → 각 아이템에 대해 delete가 개별 호출된다")
        void deleteList_multipleIds_deletesEachItem() {
            // ── Setup ──
            Item item1 = buildItem(1L, ItemStatus.LOST);
            Item item2 = buildItem(2L, ItemStatus.FOUND);
            DefaultDto.DeleteListReqDto dto = DefaultDto.DeleteListReqDto.builder()
                    .ids(List.of(1L, 2L)).build();

            given(itemRepository.findById(1L)).willReturn(Optional.of(item1));
            given(itemRepository.findById(2L)).willReturn(Optional.of(item2));
            given(itemRepository.save(any(Item.class))).willAnswer(inv -> inv.getArgument(0));

            // ── Call ──
            itemService.deleteList(dto, REQ_USER_ID);

            // ── Assertion ──
            then(itemRepository).should(times(2)).findById(anyLong());
            then(itemRepository).should(times(2)).save(any(Item.class));
        }
    }
}
