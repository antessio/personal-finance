package antessio.personalfinance.domain.ports;

import antessio.personalfinance.domain.model.AutomaticSkip;

import java.util.List;
import java.util.Optional;

public interface AutomaticSkipRepository {

    void add(String userOwner, String regex);
    void remove(String userOwner, String regex);
    Optional<AutomaticSkip> get(String userOwner);

    void add(String userOwner, List<String> regex);
}
