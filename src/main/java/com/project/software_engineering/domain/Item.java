package com.project.software_engineering.domain;

import com.project.software_engineering.dto.DefaultDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Getter @Setter
public class Item extends AuditingFields{
    @Column(nullable = false)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private Location location;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Images> images = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Category category; // 카테고리 추가
    @Enumerated(EnumType.STRING)
    private ItemStatus status; // 상태 관리 추가

    @Column(name = "user_id")
    private Long userId;

    protected Item() {}
    private Item(String title, String content, Location location, LocalDateTime startTime, LocalDateTime endTime, Category category, ItemStatus status) {
        this.title = title;
        this.content = content;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.category = category;
        this.status = status;
    }

    public static Item of(String title, String content, Location location, LocalDateTime startTime, LocalDateTime endTime, Category category, ItemStatus status){
        return new Item(title, content, location, startTime, endTime, category, status);
    }

    public DefaultDto.CreateResDto toCreateResDto() {
        return DefaultDto.CreateResDto.builder().id(getId()).build();
    }

    public void addImage(Images image) {
        this.images.add(image);
        image.setItem(this);
    }
}
