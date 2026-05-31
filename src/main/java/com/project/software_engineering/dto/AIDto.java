package com.project.software_engineering.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class AIDto {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LostItemRegisterReqDto {
        private List<String> image_url; // 항상 List로 전송하여 AI 서버에서 파싱하기 쉽게 일관성 유지
        private Long lost_id;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MatchItemResDto {
        private Long found_id;
        private Double score;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LostItemRegisterResDto {
        private Long lost_id;
        private List<MatchItemResDto> matches;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FoundItemRegisterReqDto {
        private List<String> image_url; // 항상 List로 전송
        private Long found_id;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdatedLostItemResDto {
        private Long lost_id;
        private List<MatchItemResDto> current_top5;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FoundItemRegisterResDto {
        private Long registered_found_id;
        private List<UpdatedLostItemResDto> updated_lost_items;
    }
}
