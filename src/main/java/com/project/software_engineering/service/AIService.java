package com.project.software_engineering.service;

import com.project.software_engineering.domain.Item;
import com.project.software_engineering.dto.AIDto;
import java.util.List;

public interface AIService {
    void registerItemToAIAsync(Item item, List<String> imagePaths);
    AIDto.LostItemRegisterResDto getMatchesForLostItem(Long itemId);
}
