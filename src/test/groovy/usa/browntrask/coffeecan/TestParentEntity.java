package usa.browntrask.coffeecan;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.ALL;

@Entity
public class TestParentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private final Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "child_id")
    private TestEntity child;

    @OneToMany(cascade = ALL, mappedBy = "sharedParent")
    @OrderBy("integer_field ASC")
    private List<TestEntity> children = new ArrayList<>();

    public TestParentEntity() {
        id = null;
    }

    public TestParentEntity(final Long id) {
        this.id = id;
    }

    public TestEntity getChild() {
        return child;
    }

    public void setChild(final TestEntity child) {
        this.child = child;
    }

    public Long getId() {
        return id;
    }

    public List<TestEntity> getChildren() {
        return children;
    }

    public void setChildren(final List<TestEntity> children) {
        this.children = children;
    }
}
