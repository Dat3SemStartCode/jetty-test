package dk.webtrade.jettydemo.entity;
import java.io.Serializable;
import javax.persistence.*;
/**
 *
 * @author thomas
 */
@Entity
@Table(name="person")
public class Person implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id @GeneratedValue
    @Column(name = "personId")
    private long id;
    String firstName;
    String lastName;
    

    @Override
    public String toString() {
        return "id=" + id + ", firstName=" + firstName + ", lastName=" + lastName;
    }

    public Person() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
}
