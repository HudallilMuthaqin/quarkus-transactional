package org.testing.transactional.mapper;

import org.mapstruct.*;
import org.testing.transactional.dto.UserDTO;
import org.testing.transactional.model.User;
import org.testing.transactional.dto.UserSummaryDTO;

import java.util.List;

/**
 * MapStruct mapper for converting between User entity and UserDTO.
 * Follows DRY principles and provides clean separation between layers.
 */
@Mapper(
        componentModel = "cdi",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    /**
     * Maps User entity to UserDTO.
     * Automatically maps fullName by combining firstName and lastName.
     */
    @Mapping(target = "fullName", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    UserDTO toDTO(User user);

    /**
     * Maps list of User entities to list of UserDTOs.
     */
    List<UserDTO> toDTOList(List<User> users);

    /**
     * Maps UserDTO to User entity for create operations.
     * Ignores generated fields like id, createdAt, updatedAt, version.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    User toEntity(UserDTO userDTO);

    /**
     * Maps UserDTO to User entity for update operations.
     * Updates existing entity while preserving audit fields.
     */
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    User toEntityForUpdate(UserDTO userDTO, @MappingTarget User existingUser);

    /**
     * Partially updates User entity from UserDTO.
     * Only maps non-null values from DTO to entity.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromDTO(UserDTO userDTO, @MappingTarget User user);

    /**
     * Maps User entity to a summary DTO containing only essential information.
     * Useful for list views or search results.
     */
//    @Mapping(target = "phone", ignore = true)
//    @Mapping(target = "position", ignore = true)
//    @Mapping(target = "salary", ignore = true)
//    @Mapping(target = "createdBy", ignore = true)
//    @Mapping(target = "updatedBy", ignore = true)
//    @Mapping(target = "version", ignore = true)
//    @Mapping(target = "fullName", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
//    UserDTO toSummaryDTO(User user);

    /**
     * Maps User entity to UserSummaryDTO (ringkasan).
     * Misalnya untuk list view atau dropdown.
     */
    @Mapping(target = "fullName", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    UserSummaryDTO toSummaryDTO(User user);

    /**
     * Maps list of User entities to summary DTOs.
     */
    List<UserDTO> toSummaryDTOList(List<User> users);

    /**
     * Custom method to handle full name creation.
     * This method is used by MapStruct expressions.
     */
    default String createFullName(String firstName, String lastName) {
        if (firstName == null && lastName == null) {
            return null;
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    /**
     * After mapping method to ensure data consistency.
     * Called after each mapping operation.
     */
    @AfterMapping
    default void afterMapping(@MappingTarget UserDTO userDTO, User user) {
        // Ensure fullName is properly set
        if (userDTO.getFullName() == null && user != null) {
            userDTO.setFullName(createFullName(user.getFirstName(), user.getLastName()));
        }

        // Set default active status if null
        if (userDTO.getActive() == null) {
            userDTO.setActive(true);
        }
    }

    /**
     * After mapping method for entity mapping.
     * Called after each entity mapping operation.
     */
    @AfterMapping
    default void afterMapping(@MappingTarget User user, UserDTO userDTO) {
        // Set default active status if null
        if (user.getActive() == null) {
            user.setActive(true);
        }
    }
}