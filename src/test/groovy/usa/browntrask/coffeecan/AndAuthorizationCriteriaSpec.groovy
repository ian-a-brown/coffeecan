package usa.browntrask.coffeecan

class AndAuthorizationCriteriaSpec extends AbstractJoinAuthorizationCriteriaCheck<AndAuthorizationCriteria<TestEntity>> {

    @Override
    protected AndAuthorizationCriteria<TestEntity> createJoinAuthorizationCriteria(
            final AuthorizationCriteria<TestEntity>... joinedCriteria) {
        return new AndAuthorizationCriteria<TestEntity>(joinedCriteria)
    }

    @Override
    protected boolean expectedMatchResult(boolean stringCompare, boolean integerCompare) {
        return stringCompare && integerCompare
    }
}
