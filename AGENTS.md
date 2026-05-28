# AI Coding Agent Guidelines for Airbnb Project

## Architecture Overview
This is a Spring Boot REST API mimicking Airbnb functionality with layered architecture:
- **Controllers** handle HTTP requests (e.g., `HotelController` for admin hotel CRUD under `/admin/hotels`)
- **Services** implement business logic via interfaces + implementations (e.g., `HotelService` -> `HotelServiceImplementation`)
- **Repositories** extend JpaRepository for data access
- **Entities** model domain objects with JPA annotations

Key entities: `Hotel` (owns `Room`s), `Booking` (links `Hotel`, `Room`, `User`, `Guest`s), `Inventory` (tracks availability per date with `bookedCount`, `reservedCount`, `totalCount`, `surgeFactor`).

Data flows: Booking initialization via `BookingService.initializeBooking()`, hotel search via `InventoryService.searchHotels()` using `HotelMinPriceRepository.findHotelByAvailableInventory()`.

## Developer Workflows
- **Build**: `./mvnw clean compile` (includes Lombok annotation processing)
- **Run**: `./mvnw spring-boot:run` (starts on port 8080 with context `/api/v1`, connects to PostgreSQL)
- **Test**: `./mvnw test` (runs JPA tests)
- **Debug**: Enable `spring.jpa.show-sql=true` in `application.properties` for query logging

## Project Conventions
- **Mapping**: Use `ModelMapper` for entity-DTO conversions (configured in `MapperConfig`); avoid manual mapping
- **DTOs**: All requests/responses use DTOs (e.g., `HotelDTO`, `BookingRequestDTO`); builders for complex DTOs like `HotelInfoDto`
- **Entities**: Lombok `@Getter/@Setter`; `@Builder` for `Booking`, `Inventory`; arrays for `photos`/`amenities` (PostgreSQL TEXT[])
- **Services**: Interface + `Implementation` suffix (e.g., `HotelServiceImplementation`); inject via `@RequiredArgsConstructor`
- **Logging**: `@Slf4j` in controllers/services; log key operations (e.g., `log.info("Creating new hotel with name: {}", hotelDto.getName())`)
- **Exceptions**: Throw `ResourceNotFoundException` for missing entities; handled globally in `GlobalExceptionHandler`
- **Pricing**: Strategy pattern with decorators (e.g., `SurgePricingStrategy` wraps base price with `surgeFactor`)
- **Inventory**: Unique constraint on `(hotel_id, room_id, date)`; initialize yearly on hotel activation via `InventoryService.initializeRoomForAYear()`

## Integration Points
- **Database**: PostgreSQL with `ddl-auto=update`; defer datasource init for SQL scripts
- **Dependencies**: Spring Boot 4.0.5, Lombok, ModelMapper 3.2.6; no MapStruct
- **Endpoints**: Admin operations require authentication (implied); public search uses pageable results

Reference: `src/main/java/com/yash/projects/airBnb/` for structure; `pom.xml` for builds; `application.properties` for config.</content>
<parameter name="filePath">C:\Users\yashk\Desktop\My_Java_Projects\airBnb\AGENTS.md
