package antessio.personalfinance.infrastructure.web.controller.dashboard;

import antessio.personalfinance.domain.model.*;
import antessio.personalfinance.infrastructure.web.controller.common.PaginatedResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {


    @GetMapping
    public ResponseEntity<PaginatedResult<AccountType>> getAccounts() {
        return ResponseEntity.ok(new PaginatedResult<>(List.of(AccountType.values()), false));
    }


}

