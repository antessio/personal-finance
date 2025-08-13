package antessio.personalfinance.infrastructure.persistence.repository;

import antessio.personalfinance.domain.model.AutomaticSkip;
import antessio.personalfinance.domain.ports.AutomaticSkipRepository;
import antessio.personalfinance.infrastructure.persistence.entity.AutomaticSkipEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AutomaticSkipSpringDataRepositoryAdapter implements AutomaticSkipRepository {
    private final AutomaticSkipSpringDataRepository automaticSkipSpringDataRepository;

    public AutomaticSkipSpringDataRepositoryAdapter(AutomaticSkipSpringDataRepository automaticSkipSpringDataRepository) {
        this.automaticSkipSpringDataRepository = automaticSkipSpringDataRepository;
    }

    @Override
    public void add(String userOwner, String regex) {
        automaticSkipSpringDataRepository.save(new AutomaticSkipEntity(regex, userOwner));
    }

    @Override
    public void remove(String userOwner, String regex) {
        automaticSkipSpringDataRepository.delete(new AutomaticSkipEntity(regex, userOwner));
    }

    @Override
    public Optional<AutomaticSkip> get(String userOwner) {
        return Optional.of(new AutomaticSkip(automaticSkipSpringDataRepository.findAllByUserOwner(userOwner)
                .stream()
                .map(AutomaticSkipEntity::getSkipMatcher)
                .collect(Collectors.toSet())));

    }

    @Override
    public void add(String userOwner, List<String> regex) {
        automaticSkipSpringDataRepository.saveAll(
                regex.stream()
                        .map(r -> new AutomaticSkipEntity(r, userOwner))
                        .toList());

    }
}
