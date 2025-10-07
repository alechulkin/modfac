package com.example.modfac.config;

import com.example.modfac.model.Employee;
import com.example.modfac.model.User;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class MongoDBIndexConfig {
    /**
     * Configuration class for managing MongoDB indexes.
     * <p>
     * This class is responsible for creating and ensuring the existence of
     * necessary indexes for the Employee and User collections in MongoDB.
     * It also handles the creation of Atlas Search indexes if supported by the
     * connected MongoDB cluster.
     * </p>
     * <p>
     * The indexes are created during the initialization phase of the application
     * to ensure optimal query performance and data integrity.
     * </p>
     */

    private final MongoTemplate mongoTemplate;
    private final MongoClient mongoClient;
    
    @Value("${spring.data.mongodb.database}")
    private String databaseName;
    
    /**
         * Initializes the necessary indexes for the MongoDB collections.
         * <p>
         * This method ensures that the required indexes for the Employee and User collections
         * are created in the MongoDB database. It also attempts to create an Atlas Search index
         * for the Employee collection if the connected MongoDB cluster supports it.
         * </p>
         * <p>
         * The indexes are created to optimize query performance and maintain data integrity.
         * </p>
         */
        @PostConstruct
        public void initializeIndexes() {
            IndexOperations employeeIndexes = mongoTemplate.indexOps(Employee.class);
            createMongoIndexes(employeeIndexes);
            createAtlasSearchIndex();
        }

    /**
         * Creates the necessary MongoDB indexes for the specified collection.
         * <p>
         * This method ensures that the required indexes for the Employee and User collections
         * are created in the MongoDB database. It defines unique constraints and sorting
         * for specific fields to optimize query performance and maintain data integrity.
         * </p>
         *
         * @param employeeIndexes the {@link IndexOperations} instance for the Employee collection
         */
        private void createMongoIndexes(IndexOperations employeeIndexes) {
            employeeIndexes
                .ensureIndex(new Index()
                    .on("PHONE_NUMBER", Sort.Direction.ASC)
                    .unique()
                    .named("phone_idx"));
            employeeIndexes
                .ensureIndex(new Index()
                    .on("LEAVE_INFO.LEAVE_TYPE", Sort.Direction.ASC)
                    .on("id", Sort.Direction.ASC)
                    .unique()
                    .named("leave_type_id_idx"));
            mongoTemplate.indexOps(User.class)
                .ensureIndex(new Index()
                    .on("USERNAME", Sort.Direction.ASC)
                    .unique()
                    .named("username_idx"));
        }

    /**
         * Creates an Atlas Search index for the employees collection if supported by the MongoDB cluster.
         * <p>
         * This method checks if the connected MongoDB cluster supports Atlas Search by attempting to run the
         * "listSearchIndexes" command. If supported, it verifies whether the "name-search-index" already exists.
         * If the index does not exist, it creates a new Atlas Search index with autocomplete mappings for the
         * FIRST_NAME and LAST_NAME fields.
         * </p>
         * <p>
         * The method logs appropriate messages for each step, including whether Atlas Search is supported,
         * whether the index already exists, and the outcome of the index creation process. If any errors occur
         * during these operations, they are logged as warnings or errors.
         * </p>
         */
        private void createAtlasSearchIndex() {
            try {
                MongoDatabase database = mongoClient.getDatabase(databaseName);
    
                boolean atlasSearchSupported = false;
    
                Document commandResult = null;
                // Verify if the MongoDB server supports the "listSearchIndexes" command
                try {
                    commandResult = database.runCommand(new Document("listSearchIndexes", "employees"));
                    atlasSearchSupported = true; // Command succeeded, Atlas Search is supported
                } catch (Exception e) {
                    log.warn("Atlas Search is not supported on the connected MongoDB cluster: {}", e.getMessage());
                }
    
                // If not supported, we skip the Atlas Search index creation
                if (!atlasSearchSupported) {
                    log.info("Skipping Atlas Search index creation as the cluster does not support it.");
                    return;
                }
    
                // Check if the Atlas search index already exists
                boolean indexExists = false;
                try {
                    // Run the listing command again, assuming the server supports it
                    if (commandResult.containsKey("indexes")) {
                        @SuppressWarnings("unchecked")
                        List<Document> indexes = (List<Document>) commandResult.get("indexes");
                        for (Document index : indexes) {
                            if ("name-search-index".equals(index.getString("name"))) {
                                indexExists = true;
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error listing search indexes: {}", e.getMessage());
                    // Continue with index creation attempt even if listing fails
                }
    
                // Create the search index if it doesn't exist
                if (!indexExists) {
                    String indexDefinition = "{"
                            + "\"name\": \"name-search-index\","
                            + "\"mappings\": {"
                            + "  \"dynamic\": false,"
                            + "  \"fields\": {"
                            + "    \"FIRST_NAME\": {"
                            + "      \"type\": \"autocomplete\""
                            + "    },"
                            + "    \"LAST_NAME\": {"
                            + "      \"type\": \"autocomplete\""
                            + "    }"
                            + "  }"
                            + "}"
                            + "}";
    
                    Document indexDoc = Document.parse(indexDefinition);
                    database.runCommand(new Document("createSearchIndex", "employees")
                            .append("definition", indexDoc));
    
                    log.info("Created Atlas Search index 'name-search-index' for employees collection");
                }
            } catch (Exception e) {
                log.error("Unexpected error occurred while creating Atlas Search index: {}", e.getMessage(), e);
            }
        }
}
