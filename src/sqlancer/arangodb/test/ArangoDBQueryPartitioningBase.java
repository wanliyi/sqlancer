package sqlancer.arangodb.test;

import java.util.List;

import sqlancer.Randomly;
import sqlancer.arangodb.ArangoDBProvider;
import sqlancer.arangodb.ArangoDBSchema;
import sqlancer.arangodb.ast.ArangoDBExpression;
import sqlancer.arangodb.ast.ArangoDBSelect;
import sqlancer.arangodb.gen.ArangoDBFilterExpressionGenerator;
import sqlancer.common.ast.newast.Node;
import sqlancer.common.gen.ExpressionGenerator;
import sqlancer.common.oracle.TernaryLogicPartitioningOracleBase;
import sqlancer.common.oracle.TestOracle;

public class ArangoDBQueryPartitioningBase
        extends TernaryLogicPartitioningOracleBase<Node<ArangoDBExpression>, ArangoDBProvider.ArangoDBGlobalState>
        implements TestOracle {

    protected ArangoDBSchema schema;
    protected List<ArangoDBSchema.ArangoDBColumn> targetColumns;
    protected ArangoDBFilterExpressionGenerator expressionGenerator;
    protected ArangoDBSelect<ArangoDBExpression> select;

    protected ArangoDBQueryPartitioningBase(ArangoDBProvider.ArangoDBGlobalState state) {
        super(state);
    }

    @Override
    protected ExpressionGenerator<Node<ArangoDBExpression>> getGen() {
        return expressionGenerator;
    }

    @Override
    public void check() throws Exception {
        schema = state.getSchema();
        generateTargetColumns();
        expressionGenerator = new ArangoDBFilterExpressionGenerator(state).setColumns(targetColumns);
        initializeTernaryPredicateVariants();
        select = new ArangoDBSelect<>();
        select.setFromColumns(targetColumns);
        select.setProjectionColumns(Randomly.nonEmptySubset(targetColumns));
    }

    private void generateTargetColumns() {
        ArangoDBSchema.ArangoDBTables targetTables;
        targetTables = schema.getRandomTableNonEmptyTables();
        List<ArangoDBSchema.ArangoDBColumn> allColumns = targetTables.getColumns();
        targetColumns = Randomly.nonEmptySubset(allColumns);
    }
}
