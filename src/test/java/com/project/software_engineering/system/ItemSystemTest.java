package com.project.software_engineering.system;

import tools.jackson.databind.ObjectMapper;
import com.project.software_engineering.domain.*;
import com.project.software_engineering.dto.AIDto;
import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.ItemDto;
import com.project.software_engineering.repository.ItemRepository;
import com.project.software_engineering.repository.UserRepository;
import com.project.software_engineering.service.AIService;
import com.project.software_engineering.service.ItemService;
import com.project.software_engineering.service.S3Service;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import com.project.software_engineering.security.PrincipalDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ====================================================================
 * [System Test] Item Use-case End-to-End 통합 테스트
 * ====================================================================
 * 테스트 전략: 전체 유스케이스(Use-case) 흐름 검증
 *  - @SpringBootTest: 실제 Spring 컨텍스트 전체 로드 (Controller → Service → Mapper → DB)
 *  - 외부 시스템(AI 서버, S3)만 @MockitoBean으로 대체 → 외부 의존성 격리
 *  - H2 인메모리 DB 사용 (test profile) → 실제 DB 트랜잭션 검증
 *  - 검증 항목:
 *    1) 아이템 등록 유스케이스: Controller → Service → DB 저장 → AI 서버 비동기 호출
 *    2) 아이템 수정 유스케이스: HTTP 요청 → DB 반영 검증
 *    3) 논리 삭제 유스케이스: deleted 플래그 DB 반영 검증
 *    4) AI 매칭 조회 유스케이스: Controller → AIService → 응답 반환
 *    5) 에러 플로우: 데이터 없음 시 적절한 HTTP 오류 반환
 *
 * [사전 조건]
 *  - src/test/resources/application-test.yaml 에 H2 DB 설정 필요
 *  - Spring Boot 4.0.x 기준 @MockitoBean 사용 (deprecated된 @MockBean 대체)
 * ====================================================================
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional  // 각 테스트 후 DB 롤백
@DisplayName("[System Test] Item Use-case E2E 통합 테스트")
class ItemSystemTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ItemRepository itemRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ItemService itemService;  // 복수 삭제 직접 검증용

    // 외부 시스템만 Mock 처리 (AI 서버, S3)
    @MockitoBean private AIService aiService;
    @MockitoBean private S3Service s3Service;

    private PrincipalDetails getTestPrincipal() {
        User user = User.of("testUser", "password", "testUser", 1000);
        user.setId(-200L);
        return new PrincipalDetails(user);
    }

    // =================================================================
    // TC-S-01: 아이템 등록 전체 유스케이스
    //   HTTP POST → Controller → ItemService → ItemRepository.save()
    //   → DB 저장 확인 → AIService.registerItemToAIAsync() 호출 확인
    // =================================================================
    @Test
    @DisplayName("TC-S-01: [E2E-등록] 아이템 등록 요청 → DB 저장 + AI 비동기 호출 전체 흐름 검증")
    void createItem_fullFlow_savedToDbAndAITriggered() throws Exception {
        // ── Setup ──
        ItemDto.CreateReqDto createDto = ItemDto.CreateReqDto.builder()
                .title("아이폰 15 분실")
                .content("GLC 강의실 203호에서 분실했습니다. 케이스는 파란색입니다.")
                .location(Location.GLC)
                .status(ItemStatus.LOST)
                .category(Category.ELECTRONICS)
                .startTime(LocalDateTime.of(2026, 6, 6, 14, 0))
                .endTime(LocalDateTime.of(2026, 6, 6, 15, 0))
                .build();

        String s3MockUrl = "https://s3.amazonaws.com/software-engineering-foundit/test-image.jpg";
        MockMultipartFile imageFile = new MockMultipartFile(
                "files", "test-image.jpg", MediaType.IMAGE_JPEG_VALUE,
                "fake-image-bytes".getBytes()
        );
        MockMultipartFile paramPart = new MockMultipartFile(
                "params", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(createDto)
        );

        given(s3Service.uploadFile(any())).willReturn(s3MockUrl);
        // AIService는 비동기 호출 → willDoNothing으로 처리
        willDoNothing().given(aiService).registerItemToAIAsync(any(Item.class), anyList());

        // ── Call ──
        MvcResult mvcResult = mockMvc.perform(multipart("/api/item")
                .file(paramPart)
                .file(imageFile)
                .with(user(getTestPrincipal()))
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        // ── Assertion: DB 저장 검증 ──
        String responseBody = mvcResult.getResponse().getContentAsString();
        Long createdId = objectMapper.readTree(responseBody).get("id").asLong();

        Optional<Item> savedItem = itemRepository.findById(createdId);
        assertThat(savedItem).isPresent();
        assertThat(savedItem.get().getTitle()).isEqualTo("아이폰 15 분실");
        assertThat(savedItem.get().getStatus()).isEqualTo(ItemStatus.LOST);
        assertThat(savedItem.get().getLocation()).isEqualTo(Location.GLC);
        assertThat(savedItem.get().getImages()).hasSize(1);
        assertThat(savedItem.get().getImages().get(0).getImageUrl()).isEqualTo(s3MockUrl);

        // ── Assertion: S3 업로드 및 AI 비동기 호출 검증 ──
        then(s3Service).should(times(1)).uploadFile(any());
        then(aiService).should(times(1)).registerItemToAIAsync(any(Item.class), anyList());
    }

    // =================================================================
    // TC-S-02: 아이템 등록 (이미지 없음) 전체 유스케이스
    //   이미지 없는 경우 AI 서버 미호출 검증
    // =================================================================
    @Test
    @DisplayName("TC-S-02: [E2E-등록] 이미지 없이 등록 시 → DB만 저장되고 AI 서버는 호출되지 않는다")
    void createItemWithoutImage_fullFlow_savedToDbWithoutAI() throws Exception {
        // ── Setup ──
        ItemDto.CreateReqDto createDto = ItemDto.CreateReqDto.builder()
                .title("지갑 습득")
                .content("도서관 1층 카운터에 맡겼습니다.")
                .location(Location.LIBRARY)
                .status(ItemStatus.FOUND)
                .category(Category.WALLET)
                .startTime(LocalDateTime.of(2026, 6, 6, 9, 0))
                .endTime(LocalDateTime.of(2026, 6, 6, 10, 0))
                .build();

        MockMultipartFile paramPart = new MockMultipartFile(
                "params", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(createDto)
        );

        // ── Call ──
        MvcResult mvcResult = mockMvc.perform(multipart("/api/item")
                .file(paramPart)
                .with(user(getTestPrincipal()))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // ── Assertion: DB 저장 검증 ──
        Long createdId = objectMapper.readTree(
                mvcResult.getResponse().getContentAsString()).get("id").asLong();

        Optional<Item> savedItem = itemRepository.findById(createdId);
        assertThat(savedItem).isPresent();
        assertThat(savedItem.get().getTitle()).isEqualTo("지갑 습득");
        assertThat(savedItem.get().getImages()).isEmpty();

        // ── Assertion: AI 서버 미호출 검증 ──
        then(aiService).should(never()).registerItemToAIAsync(any(), any());
        then(s3Service).should(never()).uploadFile(any());
    }

    // =================================================================
    // TC-S-03: 아이템 수정 전체 유스케이스
    //   DB에 저장된 아이템 → HTTP PUT 요청 → DB 반영 확인
    // =================================================================
    @Test
    @DisplayName("TC-S-03: [E2E-수정] 아이템 수정 요청 → DB 변경 반영 전체 흐름 검증")
    void updateItem_fullFlow_dbUpdated() throws Exception {
        // ── Setup: 먼저 DB에 아이템 직접 저장 ──
        Item item = Item.of("원래 제목", "원래 내용", Location.GLC,
                LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1),
                Category.ELECTRONICS, ItemStatus.LOST);
        item = itemRepository.saveAndFlush(item);
        Long savedId = item.getId();

        ItemDto.UpdateReqDto updateDto = ItemDto.UpdateReqDto.builder()
                .id(savedId)
                .title("수정된 제목 - 찾음")
                .status(ItemStatus.COMPLETE)
                .build();

        // ── Call ──
        mockMvc.perform(put("/api/item")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
                .with(user(getTestPrincipal()))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        // ── Assertion: DB 변경 검증 ──
        Item updatedItem = itemRepository.findById(savedId).orElseThrow();
        assertThat(updatedItem.getTitle()).isEqualTo("수정된 제목 - 찾음");
        assertThat(updatedItem.getStatus()).isEqualTo(ItemStatus.COMPLETE);
        // 수정하지 않은 필드는 유지
        assertThat(updatedItem.getContent()).isEqualTo("원래 내용");
        assertThat(updatedItem.getLocation()).isEqualTo(Location.GLC);
    }

    // =================================================================
    // TC-S-04: 아이템 논리 삭제 전체 유스케이스
    //   DB에 저장된 아이템 → HTTP DELETE → deleted=true 반영 확인
    // =================================================================
    @Test
    @DisplayName("TC-S-04: [E2E-삭제] 아이템 삭제 요청 → DB의 deleted 플래그 true 변경 검증")
    void deleteItem_fullFlow_dbDeletedFlagSet() throws Exception {
        // ── Setup ──
        Item item = Item.of("삭제할 아이템", "내용", Location.NHM,
                LocalDateTime.now().minusHours(1), LocalDateTime.now(),
                Category.CLOTHING, ItemStatus.LOST);
        item = itemRepository.saveAndFlush(item);
        Long savedId = item.getId();

        DefaultDto.DeleteReqDto deleteDto = DefaultDto.DeleteReqDto.builder()
                .id(savedId).build();

        // ── Call ──
        mockMvc.perform(delete("/api/item")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deleteDto))
                .with(user(getTestPrincipal()))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        // ── Assertion: DB의 deleted 필드 검증 ──
        Item deletedItem = itemRepository.findById(savedId).orElseThrow();
        assertThat(deletedItem.getDeleted()).isTrue();
        // 실제로 DB에서 삭제되지 않고 논리 삭제됨 (데이터 보존)
        assertThat(deletedItem.getTitle()).isEqualTo("삭제할 아이템");
    }

    // =================================================================
    // TC-S-05: AI 매칭 조회 전체 유스케이스
    //   HTTP GET → Controller → AIService → 응답 JSON 반환
    // =================================================================
    @Test
    @DisplayName("TC-S-05: [E2E-AI매칭] 분실물 매칭 조회 → AI 서버 응답이 그대로 클라이언트에 전달된다")
    void getMatches_fullFlow_aiResponseDeliveredToClient() throws Exception {
        // ── Setup ──
        Long lostItemId = 99L;
        AIDto.LostItemRegisterResDto aiResponse = AIDto.LostItemRegisterResDto.builder()
                .lost_id(lostItemId)
                .matches(List.of(
                        AIDto.MatchItemResDto.builder().found_id(11L).score(0.96).build(),
                        AIDto.MatchItemResDto.builder().found_id(22L).score(0.88).build(),
                        AIDto.MatchItemResDto.builder().found_id(33L).score(0.75).build()
                ))
                .build();

        given(aiService.getMatchesForLostItem(lostItemId)).willReturn(aiResponse);

        // ── Call ──
        mockMvc.perform(get("/api/item/{id}/matches", lostItemId)
                .with(user(getTestPrincipal()))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                // ── Assertion ──
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lost_id").value(lostItemId))
                .andExpect(jsonPath("$.matches").isArray())
                .andExpect(jsonPath("$.matches.length()").value(3))
                .andExpect(jsonPath("$.matches[0].found_id").value(11))
                .andExpect(jsonPath("$.matches[0].score").value(0.96))
                .andExpect(jsonPath("$.matches[1].found_id").value(22))
                .andExpect(jsonPath("$.matches[2].score").value(0.75));

        // AI 서비스가 정확히 1번, 올바른 ID로 호출됨
        then(aiService).should(times(1)).getMatchesForLostItem(lostItemId);
    }

    // =================================================================
    // TC-S-06: AI 서버 장애 시 Fail-Safe 유스케이스
    //   AI 서버 장애 → 서비스 전체 중단 없이 빈 결과 반환
    // =================================================================
    @Test
    @DisplayName("TC-S-06: [E2E-AI장애] AI 서버 장애 시 → 서비스 전체 중단 없이 빈 매칭 결과 반환")
    void getMatches_aiServerDown_returnsEmptyMatchesFallback() throws Exception {
        // ── Setup ──
        Long lostItemId = 100L;
        // AI 서버가 빈 결과를 반환하는 Fail-Safe 응답 (AIServiceImpl의 catch 블록)
        AIDto.LostItemRegisterResDto fallbackResponse = AIDto.LostItemRegisterResDto.builder()
                .lost_id(lostItemId)
                .matches(Collections.emptyList())
                .build();

        given(aiService.getMatchesForLostItem(lostItemId)).willReturn(fallbackResponse);

        // ── Call ──
        mockMvc.perform(get("/api/item/{id}/matches", lostItemId)
                .with(user(getTestPrincipal())))
                .andDo(print())
                // ── Assertion ──
                .andExpect(status().isOk()) // 서비스 장애가 500으로 전파되지 않음
                .andExpect(jsonPath("$.lost_id").value(lostItemId))
                .andExpect(jsonPath("$.matches").isArray())
                .andExpect(jsonPath("$.matches").isEmpty());
    }

    // =================================================================
    // TC-S-07: 복수 아이템 논리 삭제 유스케이스
    //   ItemService.deleteList()를 직접 호출하여 Controller→Service→DB 흐름 검증
    //   (현재 ItemRestController에 /api/item/list DELETE 엔드포인트 미구현)
    // =================================================================
    @Test
    @DisplayName("TC-S-07: [E2E-일괄삭제] 복수 아이템 삭제 → Controller→Service→DB 전체 흐름으로 각각 논리 삭제 반영됨")
    void deleteMultipleItems_fullFlow_allItemsLogicallyDeleted() {
        // ── Setup: DB에 2개 아이템 저장 ──
        Item item1 = Item.of("아이템1", "내용1", Location.GLC,
                LocalDateTime.now(), LocalDateTime.now(), Category.DOCUMENT, ItemStatus.LOST);
        Item item2 = Item.of("아이템2", "내용2", Location.LIBRARY,
                LocalDateTime.now(), LocalDateTime.now(), Category.ACCESSORY, ItemStatus.FOUND);
        item1 = itemRepository.saveAndFlush(item1);
        item2 = itemRepository.saveAndFlush(item2);
        Long id1 = item1.getId(), id2 = item2.getId();

        DefaultDto.DeleteListReqDto deleteListDto = DefaultDto.DeleteListReqDto.builder()
                .ids(List.of(id1, id2)).build();

        // ── Call: Service 레이어 직접 호출 (권한 우회 ID 사용) ──
        // -200L은 PermittedServiceimpl.isPermitted()에서 항상 통과 처리되는 특수 ID
        itemService.deleteList(deleteListDto, -200L);
        itemRepository.flush();

        // ── Assertion: 각 아이템의 deleted 플래그 DB 반영 확인 ──
        assertThat(itemRepository.findById(id1).orElseThrow().getDeleted()).isTrue();
        assertThat(itemRepository.findById(id2).orElseThrow().getDeleted()).isTrue();
        // 실제 레코드는 보존 (논리 삭제)
        assertThat(itemRepository.findById(id1).orElseThrow().getTitle()).isEqualTo("아이템1");
        assertThat(itemRepository.findById(id2).orElseThrow().getTitle()).isEqualTo("아이템2");
    }

    // =================================================================
    // TC-S-08: 수정 시 존재하지 않는 ID → 404 Not Found 에러 플로우
    // =================================================================
    @Test
    @DisplayName("TC-S-08: [E2E-오류] 존재하지 않는 아이템 수정 요청 → 404 Not Found 반환")
    void updateNonExistentItem_fullFlow_returns404() throws Exception {
        // ── Setup ──
        ItemDto.UpdateReqDto dto = ItemDto.UpdateReqDto.builder()
                .id(999999L) // DB에 없는 ID
                .title("존재하지 않음")
                .build();

        // ── Call ──
        mockMvc.perform(put("/api/item")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .with(user(getTestPrincipal()))
                .with(csrf()))
                .andDo(print())
                // ── Assertion: @ResponseStatus(NOT_FOUND)인 NoMatchingDataException ──
                .andExpect(status().isNotFound());
    }

    // =================================================================
    // TC-S-09: 데이터 생성-수정-삭제 생명주기 통합 시나리오
    //   등록 → 수정 → 삭제의 전체 생명주기를 하나의 테스트로 검증
    // =================================================================
    @Test
    @DisplayName("TC-S-09: [E2E-생명주기] 아이템 등록 → 수정 → 삭제 전체 생명주기 시나리오")
    void itemLifecycle_createUpdateDelete_fullScenario() throws Exception {
        // ── Step 1: 아이템 등록 ──
        ItemDto.CreateReqDto createDto = ItemDto.CreateReqDto.builder()
                .title("생명주기 테스트 아이템")
                .content("등록 단계")
                .location(Location.SU)
                .status(ItemStatus.LOST)
                .category(Category.OTHERS)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now())
                .build();

        MockMultipartFile paramPart = new MockMultipartFile(
                "params", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(createDto)
        );

        MvcResult createResult = mockMvc.perform(multipart("/api/item")
                .file(paramPart)
                .with(user(getTestPrincipal()))
                .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        Long itemId = objectMapper.readTree(
                createResult.getResponse().getContentAsString()).get("id").asLong();

        // [검증] 등록 후 DB 상태
        Item createdItem = itemRepository.findById(itemId).orElseThrow();
        assertThat(createdItem.getTitle()).isEqualTo("생명주기 테스트 아이템");
        assertThat(createdItem.getDeleted()).isFalse();

        // ── Step 2: 아이템 수정 ──
        ItemDto.UpdateReqDto updateDto = ItemDto.UpdateReqDto.builder()
                .id(itemId)
                .title("수정된 - 주인 나타남")
                .status(ItemStatus.COMPLETE)
                .build();

        mockMvc.perform(put("/api/item")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
                .with(user(getTestPrincipal()))
                .with(csrf()))
                .andExpect(status().isOk());

        // [검증] 수정 후 DB 상태
        // 트랜잭션 컨텍스트 내에서 EntityManager가 같아 캐시 반영
        itemRepository.flush();
        Item updatedItem = itemRepository.findById(itemId).orElseThrow();
        assertThat(updatedItem.getTitle()).isEqualTo("수정된 - 주인 나타남");
        assertThat(updatedItem.getStatus()).isEqualTo(ItemStatus.COMPLETE);

        // ── Step 3: 아이템 삭제 ──
        DefaultDto.DeleteReqDto deleteDto = DefaultDto.DeleteReqDto.builder()
                .id(itemId).build();

        mockMvc.perform(delete("/api/item")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deleteDto))
                .with(user(getTestPrincipal()))
                .with(csrf()))
                .andExpect(status().isOk());

        // [검증] 삭제 후 DB 상태 - 논리 삭제 (레코드는 유지)
        itemRepository.flush();
        Item deletedItem = itemRepository.findById(itemId).orElseThrow();
        assertThat(deletedItem.getDeleted()).isTrue();
        assertThat(deletedItem.getTitle()).isEqualTo("수정된 - 주인 나타남"); // 데이터 보존
    }
}
