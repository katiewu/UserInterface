package DynamoDB;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import Utils.IOUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
/**
 * for experiment only, populate a string-string database table
 * @author dichenli
 *
 */
public class StrStrPopulator implements Populator {
	static String tableName = "StrStr"; //need to sync with @DynamoDBTable(tableName="XXX")
	static String keyName = "id";
	static long readCapacity = 1L;
	static long writeCapacity = 1L;
	
	File input;
	DynamoTable table;
	Scanner sc;
	
	public StrStrPopulator(String fileName) throws Exception {
		this(new File(fileName));
	}
	
	public StrStrPopulator(File input) throws Exception {
		if(input == null) {
			throw new IllegalArgumentException();
		}
		if(!Utils.IOUtils.fileExists(input)) {
			throw new IllegalArgumentException();
		}
		sc = IOUtils.getScanner(input);
		if(sc == null) {
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	public void createTable() throws Exception {
		DynamoTable.creatTable(this);
	}
	
	@Override
	public void populate() {
		while(sc.hasNextLine()) {
			StrStr item = parseStrStr(sc.nextLine());
			if(item != null) {
				DynamoTable.insert(item);	
			}
		}
		sc.close();
	}
	
	@Override
	public String getTableName() {
		return tableName;
	}
	
	/**
	 * Create a table with "Hash" key type and String key as the table key
	 * @param key the key of the table
	 */
	@Override
	public CreateTableRequest createTableRequest() {
		CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
				.withKeySchema(new KeySchemaElement().withAttributeName(keyName).withKeyType(KeyType.HASH))
				.withAttributeDefinitions(new AttributeDefinition().withAttributeName(keyName).withAttributeType(ScalarAttributeType.S))
				.withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(readCapacity).withWriteCapacityUnits(writeCapacity));
		return createTableRequest;
	}
	
	
	private StrStr parseStrStr(String line) {
		if(line == null) {
			System.out.println("null line");
			return null;
		}
		
		String[] splited = line.split("\t");
		if(splited == null || splited.length != 2) {
			System.out.println("bad line: " + line);
			return null;
		}
		String docID = splited[0];
		String url = splited[1];
		if(docID.equals("") || url.equals("")) {
			System.out.println("bad line: " + line);
			return null;
		}
		
		StrStr item = new StrStr();
		item.setId(docID);
		item.setTitle(url);
		return item;
	}
	
	public static void main(String[] args) throws Exception {
		StrStrPopulator instance = new StrStrPopulator("/Users/dichenli/Documents/course materials/eclipse/DynamoDB555/id-1.txt");
		instance.createTable();
		instance.populate();
	}

	

	

	

}
