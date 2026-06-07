package com.project.software_engineering.component;

import tools.jackson.databind.ObjectMapper;
import com.project.software_engineering.domain.Category;
import com.project.software_engineering.domain.ItemStatus;
import com.project.software_engineering.domain.Location;
import com.project.software_engineering.dto.AIDto;
import com.project.software_engineering.dto.DefaultDto;
import com.project.software_engineering.dto.ItemDto;
import com.project.software_engineering.service.AIService;
import com.project.software_engineering.service.ItemService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ====================================================================
 * [Component Test] ItemRestController
 * ====================================================================
 * 테스트 전략: Controller 레이어 인터페이스 스펙 검증
 *  - @SpringBootTest + @AutoConfigureMockMvc + @ActiveProfiles("test"):
 *    H2 인메모리 DB 기반으로 Spring 컨텍스트 로드, 실제 Security 설정 활성화
 *  - Service 레이어는 @MockitoBean으로 고립 처리
 *  - 검증 항목:
 *    1) 정상 파라미터 바인딩 (요청 → 응답 HTTP 스펙 검증)
 *    2) 인터페이스 오용 (필수 파라미터 누락, 잘못된 타입 전송)
 *    3) 인증/인가 실패 시 올바른 HTTP 상태 코드 반환
 *
 * [NOTE] Spring Boot 4.x + OAuth2 Security 설정 복잡성으로 인해
 *        @WebMvcTest 슬라이스 대신 @SpringBootTest + H2 조합 사용.
 *        Service 레이어는 여전히 @MockitoBean으로 완전히 격리됨.
 * ====================================================================
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("[Component Test] ItemRestController - API 인터페이스 스펙 검증")
class ItemRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Service 레이어를 Mock으로 대체 (Component Test의 핵심 - Controller만 실제 구현 사용)
    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private AIService aiService;

    // =================================================================
    // TC-C-01 ~ TC-C-03: POST /api/item — 아이템 등록 API
    // =================================================================
    @Nested
    @DisplayName("POST /api/item - 아이템 등록")
    class CreateItemApiTests {

        @Test
        @DisplayName("TC-C-01: [정상] 올바른 multipart 요청 시 → 200과 함께 생성된 ID를 반환한다")
        
        void postItem_validRequest_returns200WithId() throws Exception {
            // ── Setup ──
            ItemDto.CreateReqDto createDto = ItemDto.CreateReqDto.builder()
                    .title("AirPods 분실")
                    .content("GLC 301호에서 분실했습니다.")
                    .location(Location.GLC)
                    .status(ItemStatus.LOST)
                    .category(Category.ELECTRONICS)
                    .startTime(LocalDateTime.of(2026, 6, 6, 10, 0))
                    .endTime(LocalDateTime.of(2026, 6, 6, 12, 0))
                    .build();

            DefaultDto.CreateResDto mockResponse = DefaultDto.CreateResDto.builder()
                    .id(42L).build();

            given(itemService.create(any(ItemDto.CreateReqDto.class), any(), anyLong()))
                    .willReturn(mockResponse);

            MockMultipartFile paramPart = new MockMultipartFile(
                    "params", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(createDto)
            );

            // ── Call ──
            ResultActions result = mockMvc.perform(multipart("/api/item")
                    .file(paramPart)
                    .with(user("testUser").roles("USER"))  // OAuth2 필터 우회: SecurityContext 직접 주입
                    .with(csrf())
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andDo(print());

            // ── Assertion ──
            result.andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(42L));
        }

        @Test
        @DisplayName("TC-C-02: [인터페이스 오용] params 파트 누락 시 → 400 Bad Request를 반환한다")
        
        void postItem_missingParamsPart_returns400() throws Exception {
            // ── Setup: params 파트를 포함하지 않음 ──

            // ── Call ──
            ResultActions result = mockMvc.perform(multipart("/api/item")
                    .with(user("testUser").roles("USER"))
                    .with(csrf())
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andDo(print());

            // ── Assertion ──
            // @RequestPart(required=true)인 params가 없으므로 400 반환 기대
            result.andExpect(status().isBadRequest());

            // Service 레이어는 호출되어서는 안 됨
            then(itemService).should(never()).create(any(), any(), any());
        }

        @Test
        @DisplayName("TC-C-03: [인증 없음] 인증 없이 접근 시 → @PreAuthorize(permitAll())이므로 요청이 처리된다")
        void postItem_withoutAuthentication_permitAllAllowsAccess() throws Exception {
            // ── Setup ──
            ItemDto.CreateReqDto createDto = ItemDto.CreateReqDto.builder()
                    .title("테스트")
                    .status(ItemStatus.FOUND)
                    .build();
            DefaultDto.CreateResDto mockResponse = DefaultDto.CreateResDto.builder().id(1L).build();
            given(itemService.create(any(), any(), anyLong())).willReturn(mockResponse);

            MockMultipartFile paramPart = new MockMultipartFile(
                    "params", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(createDto)
            );

            // ── Call ──
            ResultActions result = mockMvc.perform(multipart("/api/item")
                    .file(paramPart)
                    .with(csrf()))
                    .andDo(print());

            // ── Assertion ──
            // permitAll()이므로 비인증 사용자도 접근 가능
            result.andExpect(status().isOk());
        }
    }

    // =================================================================
    // TC-C-04 ~ TC-C-05: GET /api/item — 아이템 상세 조회 API
    // =================================================================
    @Nested
    @DisplayName("GET /api/item - 아이템 상세 조회")
    class GetItemDetailApiTests {

        @Test
        @DisplayName("TC-C-04: [정상] 유효한 id 파라미터로 요청 시 → 200과 상세 정보를 반환한다")
        
        void getItemDetail_validId_returns200WithDetail() throws Exception {
            // ── Setup ──
            ItemDto.DetailResDto mockDetail = ItemDto.DetailResDto.builder()
                    .id(42L)
                    .title("갤럭시 S24 분실")
                    .content("도서관에서 잃어버렸습니다.")
                    .status(ItemStatus.LOST)
                    .category(Category.ELECTRONICS)
                    .location(Location.LIBRARY)
                    .imageUrls(List.of("https://s3.amazonaws.com/bucket/img.jpg"))
                    .build();

            given(itemService.detail(any(DefaultDto.DetailReqDto.class), any()))
                    .willReturn(mockDetail);

            // ── Call ──
            ResultActions result = mockMvc.perform(get("/api/item")
                    .param("id", "42")
                    .with(user("testUser").roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());

            // ── Assertion ──
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(42L))
                    .andExpect(jsonPath("$.title").value("갤럭시 S24 분실"))
                    .andExpect(jsonPath("$.status").value("LOST"))
                    .andExpect(jsonPath("$.imageUrls").isArray())
                    .andExpect(jsonPath("$.imageUrls", hasSize(1)));
        }

        @Test
        @DisplayName("TC-C-05: [인증 없음] 비인증 사용자 접근 시 → 401/302/403 반환한다")
        void getItemDetail_withoutAuth_returnsUnauthorized() throws Exception {
            // ── Setup: 인증 없음 (@WithMockUser 미사용) ──

            // ── Call ──
            ResultActions result = mockMvc.perform(get("/api/item")
                    .param("id", "42"))
                    .andDo(print());

            // ── Assertion ──
            // @PreAuthorize("hasRole('USER')")이므로 비인증 시 401 또는 302 리다이렉트
            result.andExpect(status().is(anyOf(
                    equalTo(401), equalTo(302), equalTo(403)
            )));
        }
    }

    // =================================================================
    // TC-C-07 ~ TC-C-09: GET /api/item/list — 아이템 목록 조회 API
    // =================================================================
    @Nested
    @DisplayName("GET /api/item/list - 아이템 목록 조회")
    class GetItemListApiTests {

        @Test
        @DisplayName("TC-C-07: [정상] 필터 없이 목록 조회 시 → 200과 리스트를 반환한다")
        
        void getItemList_noFilter_returns200WithList() throws Exception {
            // ── Setup ──
            List<ItemDto.DetailResDto> mockList = List.of(
                    ItemDto.DetailResDto.builder().id(1L).title("지갑 분실").status(ItemStatus.LOST).build(),
                    ItemDto.DetailResDto.builder().id(2L).title("우산 습득").status(ItemStatus.FOUND).build()
            );
            given(itemService.list(any(ItemDto.ListReqDto.class), any())).willReturn(mockList);

            // ── Call ──
            ResultActions result = mockMvc.perform(get("/api/item/list")
                    .with(user("testUser").roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());

            // ── Assertion ──
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].title").value("지갑 분실"))
                    .andExpect(jsonPath("$[1].title").value("우산 습득"));
        }

        @Test
        @DisplayName("TC-C-08: [정상] title 필터로 목록 조회 시 → 해당 파라미터가 Service에 바인딩된다")
        
        void getItemList_withTitleFilter_bindsTitleParam() throws Exception {
            // ── Setup ──
            given(itemService.list(any(ItemDto.ListReqDto.class), any()))
                    .willReturn(Collections.emptyList());

            // ── Call ──
            ResultActions result = mockMvc.perform(get("/api/item/list")
                    .param("title", "AirPods")
                    .param("location", "GLC")
                    .with(user("testUser").roles("USER")))
                    .andDo(print());

            // ── Assertion ──
            result.andExpect(status().isOk());

            // 파라미터가 Service에 올바르게 전달되었는지 검증
            then(itemService).should(times(1)).list(
                    argThat(req -> "AirPods".equals(req.getTitle()) && Location.GLC == req.getLocation()),
                    any()
            );
        }

        @Test
        @DisplayName("TC-C-09: [경계값] 목록 결과가 비어있는 경우 → 200과 빈 배열을 반환한다")
        
        void getItemList_emptyResult_returns200WithEmptyArray() throws Exception {
            // ── Setup ──
            given(itemService.list(any(), any())).willReturn(Collections.emptyList());

            // ── Call ──
            ResultActions result = mockMvc.perform(get("/api/item/list")
                    .with(user("testUser").roles("USER")))
                    .andDo(print());

            // ── Assertion ──
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    // =================================================================
    // TC-C-10 ~ TC-C-11: GET /api/item/{id}/matches — AI 매칭 조회 API
    // =================================================================
    @Nested
    @DisplayName("GET /api/item/{id}/matches - AI 매칭 결과 조회")
    class GetMatchesApiTests {

        @Test
        @DisplayName("TC-C-10: [정상] 유효한 itemId로 매칭 조회 시 → 200과 매칭 결과를 반환한다")
        
        void getMatches_validId_returns200WithMatches() throws Exception {
            // ── Setup ──
            AIDto.LostItemRegisterResDto mockResponse = AIDto.LostItemRegisterResDto.builder()
                    .lost_id(42L)
                    .matches(List.of(
                            AIDto.MatchItemResDto.builder().found_id(10L).score(0.92).build(),
                            AIDto.MatchItemResDto.builder().found_id(20L).score(0.85).build()
                    ))
                    .build();

            given(aiService.getMatchesForLostItem(42L)).willReturn(mockResponse);

            // ── Call ──
            ResultActions result = mockMvc.perform(get("/api/item/42/matches")
                    .with(user("testUser").roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());

            // ── Assertion ──
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.lost_id").value(42L))
                    .andExpect(jsonPath("$.matches").isArray())
                    .andExpect(jsonPath("$.matches", hasSize(2)))
                    .andExpect(jsonPath("$.matches[0].found_id").value(10L))
                    .andExpect(jsonPath("$.matches[0].score").value(0.92));
        }

        @Test
        @DisplayName("TC-C-11: [인터페이스 오용] Path Variable에 문자열 전달 시 → 400 Bad Request를 반환한다")
        
        void getMatches_invalidPathVariable_returns400() throws Exception {
            // ── Setup: {id}가 Long인데 문자열 전달 ──

            // ── Call ──
            ResultActions result = mockMvc.perform(get("/api/item/notANumber/matches")
                    .with(user("testUser").roles("USER")))
                    .andDo(print());

            // ── Assertion ──
            result.andExpect(status().isBadRequest());
        }
    }

    // =================================================================
    // TC-C-12 ~ TC-C-13: PUT /api/item — 아이템 수정 API
    // =================================================================
    @Nested
    @DisplayName("PUT /api/item - 아이템 수정")
    class UpdateItemApiTests {

        @Test
        @DisplayName("TC-C-12: [정상] 유효한 JSON 본문으로 수정 요청 시 → 200을 반환한다")
        
        void putItem_validRequest_returns200() throws Exception {
            // ── Setup ──
            ItemDto.UpdateReqDto dto = ItemDto.UpdateReqDto.builder()
                    .id(42L)
                    .title("수정된 제목")
                    .status(ItemStatus.COMPLETE)
                    .build();

            willDoNothing().given(itemService).update(any(ItemDto.UpdateReqDto.class), any());

            // ── Call ──
            ResultActions result = mockMvc.perform(put("/api/item")
                    .with(user("testUser").roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
                    .with(csrf()))
                    .andDo(print());

            // ── Assertion ──
            result.andExpect(status().isOk());
            then(itemService).should(times(1)).update(
                    argThat(req -> req.getId().equals(42L) && "수정된 제목".equals(req.getTitle())),
                    any()
            );
        }

        @Test
        @DisplayName("TC-C-13: [인터페이스 오용] 빈 JSON 본문으로 수정 요청 시 → Service는 호출되나 id=null로 전달된다")
        
        void putItem_emptyBody_serviceCalledWithNullId() throws Exception {
            // ── Setup ──
            willDoNothing().given(itemService).update(any(), any());

            // ── Call ──
            ResultActions result = mockMvc.perform(put("/api/item")
                    .with(user("testUser").roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}")
                    .with(csrf()))
                    .andDo(print());

            // ── Assertion ──
            result.andExpect(status().isOk());
            then(itemService).should(times(1)).update(
                    argThat(req -> req.getId() == null),
                    any()
            );
        }
    }

    // =================================================================
    // TC-C-14: DELETE /api/item — 아이템 삭제 API
    // =================================================================
    @Nested
    @DisplayName("DELETE /api/item - 아이템 삭제")
    class DeleteItemApiTests {

        @Test
        @DisplayName("TC-C-14: [정상] 유효한 id로 삭제 요청 시 → 200을 반환한다")
        
        void deleteItem_validId_returns200() throws Exception {
            // ── Setup ──
            DefaultDto.DeleteReqDto dto = DefaultDto.DeleteReqDto.builder().id(42L).build();
            willDoNothing().given(itemService).delete(any(DefaultDto.DeleteReqDto.class), any());

            // ── Call ──
            ResultActions result = mockMvc.perform(delete("/api/item")
                    .with(user("testUser").roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
                    .with(csrf()))
                    .andDo(print());

            // ── Assertion ──
            result.andExpect(status().isOk());
            then(itemService).should(times(1)).delete(
                    argThat(req -> req.getId().equals(42L)), any()
            );
        }
    }

    // Hamcrest anyOf matcher 헬퍼 메서드
    private static org.hamcrest.Matcher<Integer> anyOf(
            org.hamcrest.Matcher<Integer>... matchers) {
        return org.hamcrest.core.AnyOf.anyOf(matchers);
    }

    private static org.hamcrest.Matcher<Integer> equalTo(int value) {
        return org.hamcrest.core.Is.is(value);
    }
}
