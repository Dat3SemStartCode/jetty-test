/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.webtrade.jettydemo;

import dk.webtrade.jettydemo.entity.Person;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author thomas
 */
public class DataFacade {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("pu", null);

    public EntityManager getManager() {
        return emf.createEntityManager();
    }

    public List<Person> getAllPersons() {
        EntityManager em = emf.createEntityManager();
        List<Person> persons = em.createQuery("SELECT p FROM Person p", Person.class).getResultList();
        em.close();
        return persons;
    }

    public Person createPerson(Person p) {
        EntityManager em = getManager();
        em.getTransaction().begin();
        em.persist(p);
        em.getTransaction().commit();
        em.close();
        return p;
    }

    public Person getPerson(long id) {
        EntityManager em = getManager();
        return em.find(Person.class, id);
    }

    public static void main(String[] args) {
        DataFacade df = new DataFacade();
        List<Person> persons = df.getAllPersons();
        persons.forEach(p -> {
            System.out.println("Person: " + p);
        });
        persons.forEach((person) -> {
            System.out.println("PESON: " + person.getFirstName());
        });
//        Person p1 = df.createPerson(new Person("Henriette", "Dellerup"));
//        Person p2 = df.createPerson(new Person("Kasandra", "Black"));
//        Person p3 = df.createPerson(new Person("Kunta", "Kinte"));
//        System.out.println("New Persons: 1:"+p1+" 2: "+p2+" 3: "+p3);
    }
}
