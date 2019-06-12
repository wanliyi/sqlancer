package lama.sqlite3.ast;

import java.util.Optional;

import lama.Randomly;
import lama.sqlite3.SQLite3CollateHelper;
import lama.sqlite3.gen.SQLite3Cast;
import lama.sqlite3.schema.SQLite3DataType;
import lama.sqlite3.schema.SQLite3Schema.Column.CollateSequence;

public class UnaryOperation extends SQLite3Expression {

	// For the purposes of the previous sentence, a column name preceded by one or
	// more unary "+" operators is still considered a column name.
	@Override
	public CollateSequence getImplicitCollateSequence() {
		if (operation == UnaryOperator.PLUS) {
			if (SQLite3CollateHelper.shouldGetSubexpressionAffinity(expression)) {
				return expression.getImplicitCollateSequence();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}



	/**
	 * Supported unary prefix operators are these:
	 * 
	 * - + ~ NOT
	 * 
	 * @see https://www.sqlite.org/lang_expr.html
	 *
	 */
	public enum UnaryOperator {
		MINUS("-") {
			@Override
			public SQLite3Constant apply(SQLite3Constant constant) {
				if (constant.isNull()) {
					return SQLite3Constant.createNullConstant();
				}
				if (constant.getDataType() == SQLite3DataType.TEXT
						|| constant.getDataType() == SQLite3DataType.BINARY) {
					constant = SQLite3Cast.castToNumericFromNumOperand(constant);
				}
				if (constant.getDataType() == SQLite3DataType.INT) {
					if (constant.asInt() == Long.MIN_VALUE) {
						// SELECT - -9223372036854775808; -- 9.22337203685478e+18
						return SQLite3Constant.createRealConstant(-(double) Long.MIN_VALUE);
					} else {
						return SQLite3Constant.createIntConstant(-constant.asInt());
					}
				}
				if (constant.getDataType() == SQLite3DataType.REAL) {
					return SQLite3Constant.createRealConstant(-constant.asDouble());
				}
				throw new AssertionError(constant);
			}
		},
		PLUS("+") {
			@Override
			public SQLite3Constant apply(SQLite3Constant constant) {
				return constant;
			}

		},
		NEGATE("~") {
			@Override
			public SQLite3Constant apply(SQLite3Constant constant) {
				SQLite3Constant intValue = SQLite3Cast.castToInt(constant);
				if (intValue.isNull()) {
					return intValue;
				}
				return SQLite3Constant.createIntConstant(~intValue.asInt());
			}
		},
		NOT("NOT") {
			@Override
			public SQLite3Constant apply(SQLite3Constant constant) {
				Optional<Boolean> boolVal = SQLite3Cast.isTrue(constant);
				if (boolVal.isPresent()) {
					Boolean negated = !boolVal.get();
					return SQLite3Constant.createBoolean(negated);
				} else {
					return SQLite3Constant.createNullConstant();
				}
			}
		};

		private String textRepresentation;

		private UnaryOperator(String textRepresentation) {
			this.textRepresentation = textRepresentation;
		}

		@Override
		public String toString() {
			return getTextRepresentation();
		}

		public String getTextRepresentation() {
			return textRepresentation;
		}

		public UnaryOperation.UnaryOperator getRandomOperator() {
			return Randomly.fromOptions(values());
		}

		public abstract SQLite3Constant apply(SQLite3Constant constant);

	}

	private final UnaryOperation.UnaryOperator operation;
	private final SQLite3Expression expression;

	public UnaryOperation(UnaryOperation.UnaryOperator operation, SQLite3Expression expression) {
		this.operation = operation;
		this.expression = expression;
	}

	public UnaryOperation.UnaryOperator getOperation() {
		return operation;
	}

	public SQLite3Expression getExpression() {
		return expression;
	}

	@Override
	public SQLite3Constant getExpectedValue() {
		if (expression.getExpectedValue() == null) {
			return null;
		} else {
			return operation.apply(expression.getExpectedValue());
		}
	}

	@Override
	public CollateSequence getExplicitCollateSequence() {
		return expression.getExplicitCollateSequence();
	}

}