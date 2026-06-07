package com.project.software_engineering.service.impl;

import com.project.software_engineering.domain.Item;
import com.project.software_engineering.domain.ItemStatus;
import com.project.software_engineering.dto.AIDto;
import com.project.software_engineering.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AIServiceImpl implements AIService {

    private final RestTemplate restTemplate;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Value("${backend.base.url}")
    private String backendBaseUrl;

    // 아이템 등록 후 AI 서버에 비동기로 전송
    @Override
    public void registerItemToAIAsync(Item item, List<String> imagePaths) {
        new Thread(() -> {
            try {
                if (item.getStatus() == ItemStatus.LOST) {
                    AIDto.LostItemRegisterReqDto req = AIDto.LostItemRegisterReqDto.builder()
                            .image_url(imagePaths)
                            .lost_id(item.getId())
                            .build();
                    AIDto.LostItemRegisterResDto res = restTemplate.postForObject(
                            aiServerUrl + "/api/ai/lost", req, AIDto.LostItemRegisterResDto.class);
                    log.info("AI 서버 응답 (LOST): {}", res);

                } else if (item.getStatus() == ItemStatus.FOUND) {
                    AIDto.FoundItemRegisterReqDto req = AIDto.FoundItemRegisterReqDto.builder()
                            .image_url(imagePaths)
                            .found_id(item.getId())
                            .build();
                    AIDto.FoundItemRegisterResDto res = restTemplate.postForObject(
                            aiServerUrl + "/api/ai/found", req, AIDto.FoundItemRegisterResDto.class);
                    log.info("AI 서버 응답 (FOUND): {}", res);
                }
            } catch (Exception e) {
                log.error("AI 서버 등록 실패. Item ID: {}", item.getId(), e);
            }
        }).start();
    }

    // AI 서버에서 분실물 매칭 결과 조회
    @Override
    public AIDto.LostItemRegisterResDto getMatchesForLostItem(Long itemId) {
        try {
            return restTemplate.getForObject(aiServerUrl + "/api/ai/matches/" + itemId, AIDto.LostItemRegisterResDto.class);
        } catch (Exception e) {
            log.error("AI 서버 매칭 조회 실패. Item ID: {}", itemId, e);
            return AIDto.LostItemRegisterResDto.builder()
                    .lost_id(itemId)
                    .matches(Collections.emptyList())
                    .build();
        }
    }
}
