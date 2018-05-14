package usa.browntrask.coffeecan;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class TestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private final Long id;

    @Column(name = "string_field")
    private String stringField;

    @Column(name = "integer_field")
    private Integer integerField;

    @OneToOne(mappedBy = "child")
    private TestParentEntity parent;

    @ManyToOne
    @JoinColumn(name = "shared_parent_id")
    private TestParentEntity sharedParent;

    public TestEntity() {
        id = null;
    }

    public TestEntity(final Long id) {
        this.id = id;
    }

    public TestParentEntity getParent() {
        return parent;
    }

    public void setParent(final TestParentEntity parent) {
        this.parent = parent;
    }

    public String getStringField() {
        return stringField;
    }

    public void setStringField(final String stringField) {
        this.stringField = stringField;
    }

    public Long getId() {
        return id;
    }

    public Integer getIntegerField() {
        return integerField;
    }

    public void setIntegerField(final Integer integerField) {
        this.integerField = integerField;
    }

    public TestParentEntity getSharedParent() {
        return sharedParent;
    }

    public void setSharedParent(final TestParentEntity sharedParent) {
        this.sharedParent = sharedParent;
    }
}
