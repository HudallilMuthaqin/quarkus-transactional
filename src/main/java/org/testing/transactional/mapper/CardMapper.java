package org.testing.transactional.mapper;

import org.mapstruct.*;
import org.testing.transactional.dto.CardDTO;
import org.testing.transactional.model.Card;

/**
 * MapStruct mapper for converting between Card entity and CardDTO.
 * Follows DRY principles and provides clean separation between layers.
 */
@Mapper(
        componentModel = "cdi",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CardMapper {

    CardDTO toDTO(Card card);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Card toEntity(CardDTO cardDTO);
}
