package usa.browntrask.coffeecan

import spock.lang.Specification
import spock.lang.Unroll

class BaseCapabilitySpec extends Specification {

    BaseCapability capability

    AuthorizationCriteriaBuilder<TestEntity> builder

    def setup() {
        capability = new BaseCapability() {};
        builder = new AuthorizationCriteriaBuilder<>(TestEntity.class)
    }

    @Unroll("Cannot change definition of standard action #action")
    def "Cannot change the definition of a standard action"() {
        when:
        capability.registerAction(action, Arrays.asList("action1", "action2"))

        then:
        RegisterActionException registerActionException = thrown()
        registerActionException.message.contains(action);

        where:
        action << [Capability.CREATE, Capability.CRUD, Capability.DELETE, Capability.MANAGE, Capability.READ, Capability.UPDATE]
    }

    def "Can register an action"() {
        given:
        capability.registerAction(action, modifyActions);

        when:
        def registeredActions = capability.registeredActions()

        then:
        modifyActions == registeredActions[action]

        where:
        action = "modify"
        modifyActions = Arrays.asList(Capability.READ, Capability.UPDATE)
    }

    def "Can re-register an action as the same set of actions"() {
        given:
        capability.registerAction(action, modifyActions);

        and:
        capability.registerAction(action, modifyActions.reverse());

        when:
        def registeredActions = capability.registeredActions()

        then:
        modifyActions == registeredActions[action]

        where:
        action = "modify"
        modifyActions = Arrays.asList(Capability.READ, Capability.UPDATE)
    }

    def "Cannot re-register an action as a different set of actions"() {
        given:
        capability.registerAction(action, modifyActions);

        when:
        capability.registerAction(action, modifyActions + "another action");

        then:
        RegisterActionException registerActionException = thrown()
        registerActionException.message.contains(action)

        where:
        action = "modify"
        modifyActions = Arrays.asList(Capability.READ, Capability.UPDATE)
    }

    @Unroll("#action is allowed on resource by default")
    def "Access to resources is allowed by default"() {
        given:
        TestEntity resource = new TestEntity()

        when:
        boolean allowed = capability.allows(action, resource);

        then:
        allowed

        where:
        action << Capability.STANDARD_ACTIONS
    }

    @Unroll("#action is not denied on resource by default")
    def "Access to resources is not denied by default"() {
        given:
        TestEntity resource = new TestEntity()

        when:
        boolean denied = capability.denies(action, resource);

        then:
        !denied

        where:
        action << Capability.STANDARD_ACTIONS
    }

    @Unroll("#action on resource is allowed for #stringField, #integerField? #expectedAllowed")
    def "Simple action on resource is controlled by can"() {
        given:
        TestEntity resource = new TestEntity(stringField: "A", integerField: 1)

        when:
        capability.can(
                action,
                TestEntity.class,
                builder.compare("stringField", Operation.EQUALS, stringField)
                        .and()
                        .compare("integerField", Operation.EQUALS, integerField)
                        .build())

        then:
        expectedAllowed == capability.allows(action, resource)

        where:
        action            | stringField | integerField || expectedAllowed
        Capability.CREATE | "A"         | 1            || true
        Capability.READ   | "A"         | 2            || false
        Capability.UPDATE | "B"         | 1            || false
        Capability.DELETE | "C"         | 3            || false
        "user-defined"    | "A"         | 1            || true
    }

    @Unroll("#actions on resource is allowed for #stringField, #integerField? #expectedAllowed")
    def "Simple actions on resource are controlled by can"() {
        given:
        TestEntity resource = new TestEntity(stringField: "A", integerField: 1)

        when:
        capability.can(
                actions,
                TestEntity.class,
                builder.compare("stringField", Operation.EQUALS, stringField)
                        .and()
                        .compare("integerField", Operation.EQUALS, integerField)
                        .build())

        then:
        actions.forEach { action ->
            expectedAllowed == capability.allows(action, resource)
        }

        where:
        actions = [Capability.CREATE, Capability.READ, Capability.UPDATE, Capability.DELETE]

        stringField | integerField | expectedAllowed
        "A"         | 1            | true
        "B"         | 1            | false
        "A"         | 2            | false
    }

    @Unroll("#action on resource is not allowed for #stringField, #integerField? #expectedAllowed")
    def "Simple action on resource is controlled by cannot"() {
        given:
        TestEntity resource = new TestEntity(stringField: "A", integerField: 1)

        when:
        capability.cannot(
                action,
                TestEntity.class,
                builder.compare("stringField", Operation.EQUALS, stringField)
                        .and()
                        .compare("integerField", Operation.EQUALS, integerField)
                        .build())

        then:
        expectedAllowed == capability.allows(action, resource)

        where:
        action            | stringField | integerField || expectedAllowed
        Capability.CREATE | "A"         | 1            || false
        Capability.READ   | "A"         | 2            || true
        Capability.UPDATE | "B"         | 1            || true
        Capability.DELETE | "C"         | 3            || true
        "user-defined"    | "A"         | 1            || false
    }

    @Unroll("#actions on resource is not allowed for #stringField, #integerField? #expectedAllowed")
    def "Simple actions on resource are controlled by cannot"() {
        given:
        TestEntity resource = new TestEntity(stringField: "A", integerField: 1)

        when:
        capability.cannot(
                actions,
                TestEntity.class,
                builder.compare("stringField", Operation.EQUALS, stringField)
                        .and()
                        .compare("integerField", Operation.EQUALS, integerField)
                        .build())

        then:
        actions.forEach { action ->
            expectedAllowed == capability.allows(action, resource)
        }

        where:
        actions = [Capability.CREATE, Capability.READ, Capability.UPDATE, Capability.DELETE]

        stringField | integerField | expectedAllowed
        "A"         | 1            | false
        "B"         | 1            | true
        "A"         | 2            | true
    }

    @Unroll("#action on resource is allowed by CRUD can? #expectedAllowed")
    def "CRUD action on resource is controlled by can"() {
        given:
        TestEntity resource = new TestEntity(stringField: "A", integerField: 1)

        when:
        capability.can(
                Capability.CRUD,
                TestEntity.class,
                builder.compare("stringField", Operation.EQUALS, stringField)
                        .or()
                        .compare("integerField", Operation.EQUALS, integerField)
                        .build())

        then:
        expectedAllowed == capability.allows(action, resource)

        where:
        action            | stringField | integerField || expectedAllowed
        Capability.CREATE | "A"         | 1            || true
        Capability.READ   | "A"         | 2            || true
        Capability.UPDATE | "B"         | 1            || true
        Capability.DELETE | "A"         | 3            || true
        "user-defined"    | "A"         | 1            || false
    }

    @Unroll("#action on resource is not allowed by CRUD cannot? #expectedAllowed")
    def "CRUD action on resource is controlled by cannot"() {
        given:
        TestEntity resource = new TestEntity(stringField: "A", integerField: 1)

        when:
        capability.cannot(
                Capability.CRUD,
                TestEntity.class,
                builder.compare("stringField", Operation.EQUALS, stringField)
                        .or()
                        .compare("integerField", Operation.EQUALS, integerField)
                        .build())

        then:
        expectedAllowed == capability.allows(action, resource)

        where:
        action            | stringField | integerField || expectedAllowed
        Capability.CREATE | "A"         | 1            || false
        Capability.READ   | "A"         | 2            || false
        Capability.UPDATE | "B"         | 1            || false
        Capability.DELETE | "A"         | 3            || false
        "user-defined"    | "A"         | 1            || true
    }

    @Unroll("#action on resource is allowed by MANAGE can? #expectedAllowed")
    def "MANAGE action on resource is controlled by can"() {
        given:
        TestEntity resource = new TestEntity(stringField: "A", integerField: 1)

        when:
        capability.can(
                Capability.MANAGE,
                TestEntity.class,
                builder.compare("stringField", Operation.EQUALS, stringField)
                        .or()
                        .compare("integerField", Operation.EQUALS, integerField)
                        .build())

        then:
        expectedAllowed == capability.allows(action, resource)

        where:
        action            | stringField | integerField || expectedAllowed
        Capability.CREATE | "A"         | 1            || true
        Capability.READ   | "A"         | 2            || true
        Capability.UPDATE | "B"         | 1            || true
        Capability.DELETE | "A"         | 3            || true
        "user-defined"    | "A"         | 1            || true
    }

    @Unroll("#action on resource is not allowed by MANAGE can? #expectedAllowed")
    def "MANAGE action on resource is controlled by cannot"() {
        given:
        TestEntity resource = new TestEntity(stringField: "A", integerField: 1)

        when:
        capability.cannot(
                Capability.MANAGE,
                TestEntity.class,
                builder.compare("stringField", Operation.EQUALS, stringField)
                        .or()
                        .compare("integerField", Operation.EQUALS, integerField)
                        .build())

        then:
        expectedAllowed == capability.allows(action, resource)

        where:
        action            | stringField | integerField || expectedAllowed
        Capability.CREATE | "A"         | 1            || false
        Capability.READ   | "A"         | 2            || false
        Capability.UPDATE | "B"         | 1            || false
        Capability.DELETE | "A"         | 3            || false
        "user-defined"    | "A"         | 1            || false
    }
}
