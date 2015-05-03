package DynamoDB;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;

/**
 * example from http://www.javacodegeeks.com/2013/08/amazon-dynamodb.html
 */
public class ObjectPersistenceCRUDExample {
	static AmazonDynamoDBClient client;
	private DynamoDBMapper mapper;
	private static int PRODUCT_ID;

	public static void main(String[] args) throws IOException {
		ObjectPersistenceCRUDExample demo = new ObjectPersistenceCRUDExample();
		demo.init();
		demo.createTable("ProductCatalog");
		for (int i = 0; i < 100; i++) {
			System.out.println(i);
			PRODUCT_ID = PRODUCT_ID + i;
			demo.insert();
		}
		demo.getAllRows();
		CatalogItem itemRetrieved = demo.load(PRODUCT_ID);
		demo.update(itemRetrieved);
		CatalogItem updatedItem = demo.load(PRODUCT_ID);
		demo.delete(updatedItem);
		demo.load(updatedItem.getId());
		System.out.println("Example complete!");
	}

	private void init() {
		PRODUCT_ID = new Random().nextInt(1000);
		AWSCredentials credentials = new ClasspathPropertiesFileCredentialsProvider()
		.getCredentials();
		client = new AmazonDynamoDBClient(credentials);
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		client.setRegion(usWest2);
		mapper = new DynamoDBMapper(client);
	}

	private void createTable(String tableName) {
		try {
			CreateTableRequest createTableRequest = new CreateTableRequest()
			.withTableName(tableName);
			createTableRequest.withKeySchema(new KeySchemaElement()
			.withAttributeName("Id").withKeyType(KeyType.HASH));
			createTableRequest
			.withAttributeDefinitions(new AttributeDefinition()
			.withAttributeName("Id").withAttributeType(
					ScalarAttributeType.N));
			createTableRequest
			.withProvisionedThroughput(new ProvisionedThroughput()
			.withReadCapacityUnits(10L).withWriteCapacityUnits(
					10L));
			TableDescription createdTableDescription = client.createTable(
					createTableRequest).getTableDescription();
			System.out.println("Created Table: " + createdTableDescription);
			// Wait for it to become active
			waitForTableToBecomeAvailable(tableName);
		} catch (AmazonServiceException e) {
			e.printStackTrace();
		} catch (AmazonClientException e) {
			e.printStackTrace();
		}
	}

	private void waitForTableToBecomeAvailable(String tableName) {
		System.out.println("Waiting for " + tableName + " to become ACTIVE...");
		long startTime = System.currentTimeMillis();
		long endTime = startTime + (10 * 60 * 1000);
		while (System.currentTimeMillis() < endTime) {
			try {
				Thread.sleep(1000 * 20);
			} catch (Exception e) {
			}
			try {
				DescribeTableRequest request = new DescribeTableRequest()
				.withTableName(tableName);
				TableDescription tableDescription = client.describeTable(
						request).getTable();
				String tableStatus = tableDescription.getTableStatus();
				System.out.println("  - current state: " + tableStatus);
				if (tableStatus.equals(TableStatus.ACTIVE.toString()))
					return;
			} catch (AmazonServiceException ase) {
				if (ase.getErrorCode().equalsIgnoreCase(
						"ResourceNotFoundException") == false)
					throw ase;
			}
		}
		throw new RuntimeException("Table " + tableName + " never went active");
	}

	private void insert() {
		CatalogItem item = new CatalogItem();
		item.setId(PRODUCT_ID);
		item.setTitle("Book PRODUCT_ID");
		item.setISBN("611-1111111111");
		item.setBookAuthors(new HashSet(Arrays.asList("Author1", "Author2")));
		// Save the item (book).
		mapper.save(item);
	}

	private void update(CatalogItem itemRetrieved) {
		itemRetrieved.setISBN("622-2222222222");
		itemRetrieved.setBookAuthors(new HashSet(Arrays.asList("Author1", "Author3")));
		mapper.save(itemRetrieved);
		System.out.println("Item updated:");
		System.out.println(itemRetrieved);
	}

	private void delete(CatalogItem updatedItem) {
		// Delete the item.
		mapper.delete(updatedItem);
	}

	private CatalogItem load(int id) {
		// Retrieve the updated item.
		DynamoDBMapperConfig config = new DynamoDBMapperConfig(
				DynamoDBMapperConfig.ConsistentReads.CONSISTENT);
		CatalogItem updatedItem = mapper.load(CatalogItem.class, id, config);
		if (updatedItem == null) {
			System.out.println("Done - Sample item is deleted.");
		} else {
			System.out.println("Retrieved item:");
			System.out.println(updatedItem);
		}
		return updatedItem;
	}

	private void getAllRows() {
		ScanRequest scanRequest = new ScanRequest()
		.withTableName("ProductCatalog");
		scanRequest.setLimit(10);
		HashMap scanFilter = new HashMap();
		Condition condition = new Condition().withComparisonOperator(
				ComparisonOperator.EQ.toString()).withAttributeValueList(
						new AttributeValue().withS("611-1111111111"));
		scanFilter.put("ISBN", condition);
		Condition condition2 = new Condition().withComparisonOperator(
				ComparisonOperator.LE.toString()).withAttributeValueList(
						new AttributeValue().withN("1000"));
		scanFilter.put("Id", condition2);
		scanRequest.withScanFilter(scanFilter);
		try {
			System.out.println("Scan Request: " + scanRequest);
			ScanResult scanResponse = client.scan(scanRequest);
			for (Map<String, AttributeValue> item : scanResponse.getItems()) {
				System.out.println(item.get("Id").getN() + " , " +
						item.get("ISBN").getS() + " , " +
						item.get("Authors").getSS() + " , " +
						item.get("Title").getS());
			}
			System.out.println("Scan Response: " + scanResponse);
			System.out.println("Count: " + scanResponse.getCount());
			System.out.println("Scanned Count: "
					+ scanResponse.getScannedCount());
			System.out.println("Items: " + scanResponse.getItems());
		} catch (AmazonServiceException e) {
			e.printStackTrace();
		} catch (AmazonClientException e) {
			e.printStackTrace();
		}
	}
}
