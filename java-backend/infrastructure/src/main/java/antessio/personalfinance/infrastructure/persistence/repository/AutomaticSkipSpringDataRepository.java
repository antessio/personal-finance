package antessio.personalfinance.infrastructure.persistence.repository;

import antessio.personalfinance.infrastructure.persistence.entity.AutomaticSkipEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AutomaticSkipSpringDataRepository extends JpaRepository<AutomaticSkipEntity, String> {
    List<AutomaticSkipEntity> findAllByUserOwner(String userOwner);
}

