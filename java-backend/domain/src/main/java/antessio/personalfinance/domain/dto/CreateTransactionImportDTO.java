package antessio.personalfinance.domain.dto;

public record CreateTransactionImportDTO(String userOwner, String sourceType, String filePath) {

}
