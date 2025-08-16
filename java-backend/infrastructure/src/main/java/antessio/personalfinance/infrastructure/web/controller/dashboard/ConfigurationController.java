package antessio.personalfinance.infrastructure.web.controller.dashboard;

import antessio.personalfinance.domain.model.AccountType;
import antessio.personalfinance.infrastructure.web.controller.dto.AccountDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/configurations")
@RequiredArgsConstructor
public class ConfigurationController {


    @GetMapping("/accounts")
    public ResponseEntity<List<AccountDTO>> getAccounts() {

        return ResponseEntity.ok(
                Stream.of(AccountType.values())
                        .map(accountType -> new AccountDTO(accountType.name(), accountType.getDescription()))
                        .toList());
    }

} 