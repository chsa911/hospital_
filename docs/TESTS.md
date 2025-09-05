# Tests

## Types
- **Repository tests** (`@DataJpaTest`): check mappings & queries
- **Service tests**: check business logic (registration, billing, cancellations)
- **Integration tests** (`@SpringBootTest`): optional, slower

## How to run
```bash
./gradlew test
./gradlew test --tests "*BillingOutstandingTest"
```

## Coverage
- CRUD hospitals/patients
- Registration lists (idempotent)
- Stay create/cancel + quarter summaries
- Billing generate, list, outstanding balance (paid ignored)
