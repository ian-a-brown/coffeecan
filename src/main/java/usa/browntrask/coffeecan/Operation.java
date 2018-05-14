package usa.browntrask.coffeecan;

/**
 * Criteria matching operations for CoffeeCan.
 *
 * @author Ian Brown
 * @since 2018/02/18
 * @version 1.0.0
 */
public enum Operation {
    EQUALS("=");

    private String operator;

    Operation(final String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }
}
