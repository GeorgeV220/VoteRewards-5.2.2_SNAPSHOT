package com.georgev22.voterewards.database.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDB {

    private final MongoClient mongoClient;
    private final MongoDatabase mongoDatabase;
    private final MongoCollection<Document> collection;

    /**
     * @param host           Host of the MongoDB (must contain port) format: localhost:27077
     * @param username       MongoDB username
     * @param password       User password
     * @param databaseName   database name (duh)
     * @param collectionName collection name (duh vol2)
     */
    public MongoDB(String host, int port, String username, String password, String databaseName, String collectionName) {
        mongoClient = MongoClients.create("mongodb://" + username + ":" + password + "@" + host + ":" + port + "/?authSource=" + databaseName);
        mongoDatabase = mongoClient.getDatabase(databaseName);
        collection = mongoDatabase.getCollection(collectionName);
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public MongoCollection<Document> getCollection() {
        return collection;
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }
}
