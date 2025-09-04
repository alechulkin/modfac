package com.example.modfac.util;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AtlasSearchUtils {

    private MongoClient mongoClient;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    /**
     * List all search indexes in the database
     */
    public List<Document> listAllSearchIndexes(String collectionName) {
        List<Document> indexes = new ArrayList<>();
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        for (Document index : database.getCollection(collectionName).listIndexes()) {
            indexes.add(index);
        }

        return indexes;
    }

    /**
     * Delete a search index
     */
    public void deleteSearchIndex(String indexName, String collectionName) {
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        try {
            database.runCommand(new Document("dropSearchIndex", collectionName)
                    .append("name", indexName));
            log.info("Deleted search index '{}' from collection '{}'", indexName, collectionName);
        } catch (Exception e) {
            log.error("Failed to delete search index: {}", e.getMessage(), e);
            throw e;
        }
    }
}
