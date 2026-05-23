package com.project.software_engineering.dto;

import com.project.software_engineering.domain.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;


public class ItemDto {
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
    public static class CreateReqDto {
        private String title;
        private String content;
        private Location location;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Category category;
        private ItemStatus status;

        public Item toEntity(){
            return Item.of(getTitle(), getContent(), getLocation(), getStartTime(), getEndTime(), getCategory(), getStatus());
        }
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
    public static class UpdateReqDto extends DefaultDto.UpdateReqDto {
        private String title;
        private String content;
        private ItemStatus status;
        private Category category;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Location location;
    }

    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class DetailResDto extends DefaultDto.DetailResDto{
        String title;
        String content;
        private ItemStatus status;
        private Category category;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Location location;
        private Long userId;
        private String userUsername;
        private List<String> imageUrls;
    }

    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class ListReqDto extends DefaultDto.ListReqDto{
        String title;
        private Location location;
    }
}
