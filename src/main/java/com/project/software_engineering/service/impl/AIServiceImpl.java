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

    @Override
    public void registerItemToAIAsync(Item item, List<String> imagePaths) {
        new Thread(() -> {
            try {
                // 이미 전체 S3 URL 형태이므로 그대로 리스트로 전송
                List<String> imageUrlPayload = imagePaths;

                if (item.getStatus() == ItemStatus.LOST) {
                    AIDto.LostItemRegisterReqDto req = AIDto.LostItemRegisterReqDto.builder()
                            .image_url(imageUrlPayload)
                            .lost_id(item.getId())
                            .build();

                    AIDto.LostItemRegisterResDto res = restTemplate.postForObject(
                            aiServerUrl + "/api/ai/lost", req, AIDto.LostItemRegisterResDto.class);
                    log.info("AI Server Response (LOST): {}", res);
                } else if (item.getStatus() == ItemStatus.FOUND) {
                    AIDto.FoundItemRegisterReqDto req = AIDto.FoundItemRegisterReqDto.builder()
                            .image_url(imageUrlPayload)
                            .found_id(item.getId())
                            .build();

                    AIDto.FoundItemRegisterResDto res = restTemplate.postForObject(
                            aiServerUrl + "/api/ai/found", req, AIDto.FoundItemRegisterResDto.class);
                    log.info("AI Server Response (FOUND): {}", res);
                }
            } catch (Exception e) {
                log.error("Failed to register item to AI server. Item ID: {}", item.getId(), e);
            }
        }).start();
    }

    @Override
    public AIDto.LostItemRegisterResDto getMatchesForLostItem(Long itemId) {
        try {
            String url = aiServerUrl + "/api/ai/matches/" + itemId;
            return restTemplate.getForObject(url, AIDto.LostItemRegisterResDto.class);
        } catch (Exception e) {
            log.error("Failed to get matches from AI server. Item ID: {}", itemId, e);
            return AIDto.LostItemRegisterResDto.builder()
                    .lost_id(itemId)
                    .matches(Collections.emptyList())
                    .build();
        }
    }
}
