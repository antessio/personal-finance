package antessio.personalfinance.domain.model;

import antessio.personalfinance.common.Id;

public class CategoryId extends Id<Long> {

    public CategoryId(Long id) {
        super(id);
    }

    public Long id(){
        return getId();
    }
}
