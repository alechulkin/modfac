package com.example.modfac.util;

import com.mongodb.client.*;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
class AtlasSearchUtilsTest {

    @InjectMocks
    private AtlasSearchUtils atlasSearchUtils;

    @Mock
    private MongoClient mongoClient;

    @Mock
    private MongoDatabase mockDatabase;

    @Mock
    private MongoCollection<Document> mockCollection;

    @Mock
    private ListIndexesIterable<Document> mockIndexesIterable;

    @Mock
    private MongoCursor<Document> mockCursor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(atlasSearchUtils, "databaseName", "test-db");

        when(mongoClient.getDatabase("test-db")).thenReturn(mockDatabase);
    }

    @Test
    void testListAllSearchIndexes_shouldReturnList() {
        String collectionName = "test-collection";
        Document index1 = new Document("name", "index1");
        Document index2 = new Document("name", "index2");

        // Setup mocks
        when(mockDatabase.getCollection(collectionName)).thenReturn(mockCollection);
        when(mockCollection.listIndexes()).thenReturn(mockIndexesIterable);
        when(mockIndexesIterable.iterator()).thenReturn(mockCursor);
        when(mockCursor.hasNext()).thenReturn(true, true, false);
        when(mockCursor.next()).thenReturn(index1, index2);

        // Act
        List<Document> result = atlasSearchUtils.listAllSearchIndexes(collectionName);

        // Assert
        assertEquals(2, result.size());
        assertEquals("index1", result.get(0).getString("name"));
        assertEquals("index2", result.get(1).getString("name"));
    }

    @Test
    void testDeleteSearchIndex_shouldCallRunCommand() {
        String collectionName = "test-collection";
        String indexName = "test-index";

        when(mongoClient.getDatabase("test-db")).thenReturn(mockDatabase);

        // Act
        atlasSearchUtils.deleteSearchIndex(indexName, collectionName);

        // Assert
        verify(mockDatabase, times(1)).runCommand(
                argThat(arg -> {
                    if (!(arg instanceof Document doc)) return false;
                    return collectionName.equals(doc.getString("dropSearchIndex")) &&
                            indexName.equals(doc.getString("name"));
                })
        );
    }

    @Test
    void testDeleteSearchIndex_shouldThrowExceptionWhenFails() {
        String collectionName = "test-collection";
        String indexName = "test-index";

        when(mockDatabase.runCommand(any(Document.class)))
                .thenThrow(new RuntimeException("Command failed"));

        assertThrows(RuntimeException.class, () ->
                atlasSearchUtils.deleteSearchIndex(indexName, collectionName)
        );
    }
}

