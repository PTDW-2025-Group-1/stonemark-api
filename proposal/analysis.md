# Analysis of the `proposal` module

This document provides an analysis of the `proposal` module in the stonemark-api project, with a focus on scalability, reliability, and performance.

### Module Overview

The `proposal` module is responsible for managing "Mark Occurrence Proposals". It allows users to create, submit, and query proposals related to marking occurrences on monuments.

**Key Components:**

*   **`MarkOccurrenceProposalController`**: Exposes REST endpoints for all proposal-related operations.
*   **`MarkOccurrenceProposalService`**: The core service for business logic related to proposals.
*   **`MarkOccurrenceProposalSubmissionService`**: A dedicated service for handling the submission workflow.
*   **`MarkOccurrenceProposal`**: The JPA entity representing a proposal.
*   **`MarkOccurrenceProposalRepository`**: The Spring Data repository for database interactions.
*   **Dependencies**: The module depends on `content`, `detection`, and `shared` modules, indicating a modular architecture.

The module follows a standard layered architecture, which is good for separation of concerns and maintainability.

### Recommendations for Scalability, Reliability, and Performance

| Area          | Recommendation                                                              | Priority | Justification                                                                                                                                                           |
| :------------ | :-------------------------------------------------------------------------- | :------- | :---------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Scalability** | **Make proposal submission asynchronous.**                                  | High     | The submission process might involve heavy operations. Making it asynchronous will improve API responsiveness and allow for better resource management under load.             |
|               | **Ensure proper database indexing.**                                        | High     | Queries on `userId`, `status`, etc., will become slow as the number of proposals grows. Indexes are crucial for maintaining read performance.                        |
|               | **Consider database read replicas.**                                        | Medium   | For read-heavy workloads (e.g., public feeds of proposals), read replicas can distribute the load and improve scalability.                                            |
| **Reliability** | **Ensure the submission process is transactional.**                         | High     | The submission process likely modifies multiple entities. Transactions guarantee data consistency by ensuring that all operations complete successfully or none do. |
|               | **Make the submit endpoint idempotent.**                                    | High     | Prevents duplicate processing if a user retries a submission, which is a common scenario in distributed systems.                                                      |
|               | **Implement robust error handling for async processes.**                    | Medium   | If submission is asynchronous, failures need to be handled gracefully with retries and dead-letter queues to prevent data loss.                                       |
| **Performance** | **Continue using DTO projections for all list queries.**                  | High     | Fetching only the necessary data from the database significantly reduces I/O and memory usage, which is a key performance optimization.                               |
|               | **Implement caching for frequently accessed, non-volatile data.**           | Medium   | Caching proposal details (especially for closed proposals) or user statistics can reduce database load and improve response times.                                    |
|               | **Analyze and optimize complex database queries.**                          | Medium   | As the application evolves, some queries might become complex. Regularly analyzing and optimizing them will prevent performance degradation.                             |

### Action Plan

1.  **Refactor `MarkOccurrenceProposalSubmissionService`**:
    *   Modify the `submit` method to publish a `ProposalSubmittedEvent` to a message broker (e.g., RabbitMQ, Kafka).
    *   Create a new listener that consumes this event and performs the actual submission logic asynchronously.
    *   Ensure the `submit` method in the service is annotated with `@Transactional`.

2.  **Database Optimization**:
    *   Add `@Index` annotations to the `MarkOccurrenceProposal` entity for `userId` and `status` fields.
    *   Review the query for `getStatsByUser` and other complex queries to ensure they are performant.

3.  **Caching**:
    *   Introduce caching (e.g., using Spring's `@Cacheable`) for the `findById` and `getUserStats` methods in the `MarkOccurrenceProposalService`.

By implementing these recommendations, the `proposal` module will be more scalable, reliable, and performant, ready to handle a growing number of users and proposals.
