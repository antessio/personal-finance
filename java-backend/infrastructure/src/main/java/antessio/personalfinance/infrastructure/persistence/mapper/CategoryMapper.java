package antessio.personalfinance.infrastructure.persistence.mapper;

import antessio.personalfinance.domain.model.Category;
import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    
    public Category toDomain(CategoryEntity entity) {
        return new Category(
            entity.getCategoryId(),
            entity.getName(),
            entity.getMacroCategory(),
            entity.getUserOwner(),
            entity.getMatchers(),
            entity.getInsertedAt(),
            entity.getUpdatedAt()
        );
    }

    public CategoryEntity toEntity(Category domain) {
        return new CategoryEntity(
            domain.getId().id(),
            domain.getName(),
            domain.getMacroCategory(),
            domain.getUserOwner(),
            domain.getMatchers(),
            domain.getInsertedAt(),
            domain.getUpdatedAt()
        );
    }
} 