package usa.browntrask.coffeecan

class OrAuthorizationCriteriaSpec extends AbstractJoinAuthorizationCriteriaCheck<OrAuthorizationCriteria<TestEntity>> {

    @Override
    protected OrAuthorizationCriteria<TestEntity> createJoinAuthorizationCriteria(
            final AuthorizationCriteria<TestEntity>... joinedCriteria) {
        return new OrAuthorizationCriteria<TestEntity>(joinedCriteria)
    }

    @Override
    protected boolean expectedMatchResult(boolean stringCompare, boolean integerCompare) {
        return stringCompare || integerCompare
    }
}
