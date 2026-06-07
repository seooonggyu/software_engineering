package com.project.software_engineering.unit;

import com.project.software_engineering.domain.Images;
import com.project.software_engineering.domain.Item;
import com.project.software_engineering.domain.ItemStatus;
import com.project.software_engineering.dto.AIDto;
import com.project.software_engineering.service.impl.AIServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * ====================================================================
 * [Unit Test] AIServiceImpl
 * ====================================================================
 * 테스트 전략: 소프트웨어 공학 단위 테스트 원칙 적용
 *  - 외부 AI 서버(RestTemplate)를 Mockito로 완전히 고립(Isolation) 처리
 *  - Setup → Call → Assertion 구조 엄격 준수
 *  - 등가 분할(Equivalence Partitioning):
 *    정상 케이스 / 경계값(빈 목록, null) / 오류 케이스(타임아웃, 서버 다운) 구분
 * ====================================================================
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("[Unit Test] AIServiceImpl - AI 서버 연동 비즈니스 로직")
class AIServiceImplTest {

    // ── 협력 객체(SUT가 의존하는 외부 컴포넌트)를 Mock으로 대체 ──
    @Mock
    private RestTemplate restTemplate;

    // ── 테스트 대상 객체(SUT) ──
    @InjectMocks
    private AIServiceImpl aiService;

    // ── 공통 픽스처 ──
    private static final String AI_SERVER_URL = "http://test-ai-server:8000";
    private static final Long LOST_ITEM_ID = 1L;
    private static final Long FOUND_ITEM_ID = 2L;

    @BeforeEach
    void setUp() {
        // Setup: @Value 필드는 ReflectionTestUtils로 주입 (Spring 컨텍스트 없이)
        ReflectionTestUtils.setField(aiService, "aiServerUrl", AI_SERVER_URL);
        ReflectionTestUtils.setField(aiService, "backendBaseUrl", "http://localhost:8080");
    }

    // =================================================================
    // TC-U-01 ~ TC-U-03: getMatchesForLostItem() — 정상 흐름
    // =================================================================
    @Nested
    @DisplayName("getMatchesForLostItem() - 분실물 매칭 조회")
    class GetMatchesForLostItemTests {

        @Test
        @DisplayName("TC-U-01: [정상] AI 서버가 매칭 결과를 정상 반환할 때 → 결과를 그대로 반환한다")
        void getMatchesForLostItem_success_returnsMatches() {
            // ── Setup ──
            AIDto.MatchItemResDto match1 = AIDto.MatchItemResDto.builder()
                    .found_id(10L).score(0.95).build();
            AIDto.MatchItemResDto match2 = AIDto.MatchItemResDto.builder()
                    .found_id(20L).score(0.87).build();
            AIDto.LostItemRegisterResDto mockResponse = AIDto.LostItemRegisterResDto.builder()
                    .lost_id(LOST_ITEM_ID)
                    .matches(List.of(match1, match2))
                    .build();

            given(restTemplate.getForObject(
                    eq(AI_SERVER_URL + "/api/ai/matches/" + LOST_ITEM_ID),
                    eq(AIDto.LostItemRegisterResDto.class)
            )).willReturn(mockResponse);

            // ── Call ──
            AIDto.LostItemRegisterResDto result = aiService.getMatchesForLostItem(LOST_ITEM_ID);

            // ── Assertion ──
            assertThat(result).isNotNull();
            assertThat(result.getLost_id()).isEqualTo(LOST_ITEM_ID);
            assertThat(result.getMatches()).hasSize(2);
            assertThat(result.getMatches().get(0).getFound_id()).isEqualTo(10L);
            assertThat(result.getMatches().get(0).getScore()).isEqualTo(0.95);

            // RestTemplate 호출 횟수 검증 (행위 검증)
            then(restTemplate).should(times(1))
                    .getForObject(anyString(), eq(AIDto.LostItemRegisterResDto.class));
        }

        @Test
        @DisplayName("TC-U-02: [경계값] AI 서버가 빈 매칭 목록을 반환할 때 → 빈 리스트를 반환한다")
        void getMatchesForLostItem_emptyMatches_returnsEmptyList() {
            // ── Setup ──
            AIDto.LostItemRegisterResDto mockResponse = AIDto.LostItemRegisterResDto.builder()
                    .lost_id(LOST_ITEM_ID)
                    .matches(Collections.emptyList())
                    .build();

            given(restTemplate.getForObject(anyString(), eq(AIDto.LostItemRegisterResDto.class)))
                    .willReturn(mockResponse);

            // ── Call ──
            AIDto.LostItemRegisterResDto result = aiService.getMatchesForLostItem(LOST_ITEM_ID);

            // ── Assertion ──
            assertThat(result).isNotNull();
            assertThat(result.getMatches()).isEmpty();
        }

        @Test
        @DisplayName("TC-U-03: [오류-타임아웃] AI 서버 타임아웃 발생 시 → 예외를 삼키고 빈 결과를 반환한다 (Fail-Safe)")
        void getMatchesForLostItem_timeout_returnsFallbackEmptyResult() {
            // ── Setup ──
            given(restTemplate.getForObject(anyString(), eq(AIDto.LostItemRegisterResDto.class)))
                    .willThrow(new ResourceAccessException("Connection timed out: connect"));

            // ── Call ──
            AIDto.LostItemRegisterResDto result = aiService.getMatchesForLostItem(LOST_ITEM_ID);

            // ── Assertion ──
            // AI 서버 장애 시 서비스 전체가 중단되지 않고 빈 결과로 graceful 처리되어야 함
            assertThat(result).isNotNull();
            assertThat(result.getLost_id()).isEqualTo(LOST_ITEM_ID);
            assertThat(result.getMatches()).isEmpty();
        }

        @Test
        @DisplayName("TC-U-04: [오류-서버다운] AI 서버 연결 자체 실패 시 → 예외를 삼키고 빈 결과를 반환한다")
        void getMatchesForLostItem_serverDown_returnsFallbackEmptyResult() {
            // ── Setup ──
            given(restTemplate.getForObject(anyString(), eq(AIDto.LostItemRegisterResDto.class)))
                    .willThrow(new RestClientException("Connection refused"));

            // ── Call ──
            AIDto.LostItemRegisterResDto result = aiService.getMatchesForLostItem(LOST_ITEM_ID);

            // ── Assertion ──
            assertThat(result).isNotNull();
            assertThat(result.getMatches()).isEmpty();
        }
    }

    // =================================================================
    // TC-U-05 ~ TC-U-08: registerItemToAIAsync() — 비동기 AI 등록
    // =================================================================
    @Nested
    @DisplayName("registerItemToAIAsync() - 분실물/습득물 AI 서버 비동기 등록")
    class RegisterItemToAIAsyncTests {

        private Item createLostItem() {
            Item item = new Item() {};
            ReflectionTestUtils.setField(item, "id", LOST_ITEM_ID);
            ReflectionTestUtils.setField(item, "status", ItemStatus.LOST);
            return item;
        }

        private Item createFoundItem() {
            Item item = new Item() {};
            ReflectionTestUtils.setField(item, "id", FOUND_ITEM_ID);
            ReflectionTestUtils.setField(item, "status", ItemStatus.FOUND);
            return item;
        }

        @Test
        @DisplayName("TC-U-05: [정상-LOST] LOST 아이템 → AI 서버의 /api/ai/lost 엔드포인트를 호출한다")
        void registerItemToAIAsync_lostItem_callsLostEndpoint() throws InterruptedException {
            // ── Setup ──
            Item lostItem = createLostItem();
            List<String> imagePaths = List.of("https://s3.amazonaws.com/bucket/image1.jpg");

            AIDto.LostItemRegisterResDto mockResponse = AIDto.LostItemRegisterResDto.builder()
                    .lost_id(LOST_ITEM_ID)
                    .matches(Collections.emptyList())
                    .build();

            given(restTemplate.postForObject(
                    contains("/api/ai/lost"),
                    any(AIDto.LostItemRegisterReqDto.class),
                    eq(AIDto.LostItemRegisterResDto.class)
            )).willReturn(mockResponse);

            // ── Call ──
            aiService.registerItemToAIAsync(lostItem, imagePaths);

            // 비동기 스레드 완료 대기 (Thread 기반이므로 충분한 여유 시간 부여)
            Thread.sleep(500);

            // ── Assertion ──
            then(restTemplate).should(times(1))
                    .postForObject(contains("/api/ai/lost"), any(), eq(AIDto.LostItemRegisterResDto.class));
            then(restTemplate).should(never())
                    .postForObject(contains("/api/ai/found"), any(), any());
        }

        @Test
        @DisplayName("TC-U-06: [정상-FOUND] FOUND 아이템 → AI 서버의 /api/ai/found 엔드포인트를 호출한다")
        void registerItemToAIAsync_foundItem_callsFoundEndpoint() throws InterruptedException {
            // ── Setup ──
            Item foundItem = createFoundItem();
            List<String> imagePaths = List.of("https://s3.amazonaws.com/bucket/image2.jpg");

            AIDto.FoundItemRegisterResDto mockResponse = AIDto.FoundItemRegisterResDto.builder()
                    .registered_found_id(FOUND_ITEM_ID)
                    .updated_lost_items(Collections.emptyList())
                    .build();

            given(restTemplate.postForObject(
                    contains("/api/ai/found"),
                    any(AIDto.FoundItemRegisterReqDto.class),
                    eq(AIDto.FoundItemRegisterResDto.class)
            )).willReturn(mockResponse);

            // ── Call ──
            aiService.registerItemToAIAsync(foundItem, imagePaths);
            Thread.sleep(500);

            // ── Assertion ──
            then(restTemplate).should(times(1))
                    .postForObject(contains("/api/ai/found"), any(), eq(AIDto.FoundItemRegisterResDto.class));
            then(restTemplate).should(never())
                    .postForObject(contains("/api/ai/lost"), any(), any());
        }

        @Test
        @DisplayName("TC-U-07: [오류-비동기내 예외] AI 서버 호출 중 예외 발생 시 → 예외가 메인 스레드로 전파되지 않는다")
        void registerItemToAIAsync_aiServerThrows_exceptionNotPropagatedToCallerThread() throws InterruptedException {
            // ── Setup ──
            Item lostItem = createLostItem();
            List<String> imagePaths = List.of("https://s3.amazonaws.com/bucket/image.jpg");

            given(restTemplate.postForObject(anyString(), any(), any()))
                    .willThrow(new RestClientException("AI server is down"));

            // ── Call & Assertion ──
            // 비동기 내부 예외가 메인 스레드로 전파되어서는 안 됨 (assertThatCode 로 검증)
            assertThatCode(() -> {
                aiService.registerItemToAIAsync(lostItem, imagePaths);
                Thread.sleep(500); // 비동기 완료 대기
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("TC-U-08: [경계값] 이미지 경로 목록이 복수일 때 → 올바른 요청 DTO에 전체 목록이 포함된다")
        void registerItemToAIAsync_multipleImages_allPathsIncluded() throws InterruptedException {
            // ── Setup ──
            Item lostItem = createLostItem();
            List<String> imagePaths = List.of(
                    "https://s3.amazonaws.com/bucket/img1.jpg",
                    "https://s3.amazonaws.com/bucket/img2.jpg",
                    "https://s3.amazonaws.com/bucket/img3.jpg"
            );

            given(restTemplate.postForObject(anyString(), any(), any())).willReturn(null);

            // ── Call ──
            aiService.registerItemToAIAsync(lostItem, imagePaths);
            Thread.sleep(500);

            // ── Assertion ──
            // captor를 활용하여 전달된 요청 DTO 검증
            then(restTemplate).should(times(1))
                    .postForObject(
                            contains("/api/ai/lost"),
                            argThat(req -> {
                                if (req instanceof AIDto.LostItemRegisterReqDto lostReq) {
                                    return lostReq.getImage_url().size() == 3
                                            && lostReq.getLost_id().equals(LOST_ITEM_ID);
                                }
                                return false;
                            }),
                            any()
                    );
        }
    }
}
