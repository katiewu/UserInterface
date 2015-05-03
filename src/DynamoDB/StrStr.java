package DynamoDB;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/**
 * for experiment only, populate a string-string database table
 * @author dichenli
 *
 */
@DynamoDBTable(tableName="StrStr")
public class StrStr {

	
	String id; //binary data
	String url;
	
	@DynamoDBHashKey(attributeName="id")
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    @DynamoDBAttribute(attributeName="url")
    public String getTitle() { return url; }    
    public void setTitle(String url) { this.url = url; }
    
    @Override
    public String toString() {
       return url;            
    }


}
