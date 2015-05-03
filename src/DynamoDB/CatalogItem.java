package DynamoDB;

/**
 * example from http://www.javacodegeeks.com/2013/08/amazon-dynamodb.html
 */
import java.util.Set;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
@DynamoDBTable(tableName="ProductCatalog")
public class CatalogItem {
    private Integer id;
    private String title;
    private String ISBN;
    private Set bookAuthors;

    @DynamoDBHashKey(attributeName="Id")
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    @DynamoDBAttribute(attributeName="Title")
    public String getTitle() { return title; }    
    public void setTitle(String title) { this.title = title; }

    @DynamoDBAttribute(attributeName="ISBN")
    public String getISBN() { return ISBN; }    
    public void setISBN(String ISBN) { this.ISBN = ISBN;}

    @DynamoDBAttribute(attributeName = "Authors")
    public Set getBookAuthors() { return bookAuthors; }    
    public void setBookAuthors(Set bookAuthors) { this.bookAuthors = bookAuthors; }

    @Override
    public String toString() {
       return "Book [ISBN=" + ISBN + ", bookAuthors=" + bookAuthors
       + ", id=" + id + ", title=" + title + "]";            
    }
}
