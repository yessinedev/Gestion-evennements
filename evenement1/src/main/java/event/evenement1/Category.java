package event.evenement1;
import jakarta.xml.bind.annotation.XmlRootElement;
@XmlRootElement
public class Category {
	    private String id;
	    private String name;

	    public Category(String id, String name) {
	        this.id = id;
	        this.name = name;
	    }

	    // Getters et setters
	    public String getId() {
	        return id;
	    }

	    public void setId(String id) {
	        this.id = id;
	    }

	    public String getName() {
	        return name;
	    }

	    public void setName(String name) {
	        this.name = name;
	    }
	}

