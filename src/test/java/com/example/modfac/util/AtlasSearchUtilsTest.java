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
    /**
     * Unit tests for the {@link AtlasSearchUtils} class.
     * <p>
     * This test class verifies the functionality of methods in the AtlasSearchUtils class,
     * including listing all search indexes and deleting specific search indexes.
     * It uses Mockito to mock dependencies and JUnit for assertions.
     */

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

    /**
     * Initializes mocks and sets up the test environment before each test case.
     * <p>
     * This method opens Mockito annotations for the test class and sets the
     * "databaseName" field of the {@link AtlasSearchUtils} instance to "test-db".
     * It also configures the mocked {@link MongoClient} to return a mocked
     * {@link MongoDatabase} when the "test-db" database is requested.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(atlasSearchUtils, "databaseName", "test-db");
    
        when(mongoClient.getDatabase("test-db")).thenReturn(mockDatabase);
    }

    /**
     * Tests the {@link AtlasSearchUtils#listAllSearchIndexes(String)} method to ensure it
     * correctly retrieves all search indexes for a given collection.
     * <p>
     * This test sets up a mocked MongoDB environment, including a collection with
     * predefined indexes. It verifies that the method returns the expected list of
     * indexes with the correct names.
     */
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

    /**
     * Tests the {@link AtlasSearchUtils#deleteSearchIndex(String, String)} method to ensure it
     * correctly executes the "dropSearchIndex" command on the specified collection and index.
     * <p>
     * This test sets up a mocked MongoDB environment and verifies that the method
     * calls the "runCommand" method on the database with the expected arguments.
     */
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

    /**
     * Tests the {@link AtlasSearchUtils#deleteSearchIndex(String, String)} method to ensure it
     * throws an exception when the "dropSearchIndex" command fails.
     * <p>
     * This test sets up a mocked MongoDB environment where the "runCommand" method
     * throws a {@link RuntimeException}. It verifies that the method under test
     * correctly propagates the exception.
     */
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

