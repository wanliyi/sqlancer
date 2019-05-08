package lama.tablegen.sqlite3;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.junit.jupiter.api.Test;

import lama.Expression.Constant;
import lama.QueryGenerator;
import lama.schema.SQLite3DataType;

class TestCastToNumeric {

	@Test
	void testLong() {
		long numbers[] = new long[] { 0, 1, 123, Long.MAX_VALUE, Long.MIN_VALUE };
		for (long number : numbers) {
			assertEquals(castLongConstant(number), number);
		}
	}

	class StringTestTriple {
		String value;
		SQLite3DataType type;
		Object expectedCastValue;

		public StringTestTriple(String value, SQLite3DataType type, Object expectedCastValue) {
			this.value = value;
			this.type = type;
			this.expectedCastValue = expectedCastValue;
		}
	}

	@Test
	void testString() {
		List<StringTestTriple> triples = new ArrayList<>();
		triples.add(new StringTestTriple("", SQLite3DataType.INT, 0L));
		triples.add(new StringTestTriple("a", SQLite3DataType.INT, 0L));
		triples.add(new StringTestTriple("123a", SQLite3DataType.INT, 123L));
		triples.add(new StringTestTriple("3", SQLite3DataType.INT, 3L));
		triples.add(new StringTestTriple("-3", SQLite3DataType.INT, -3L));
		triples.add(new StringTestTriple("-3.0", SQLite3DataType.INT, -3L));
		triples.add(new StringTestTriple("0.0", SQLite3DataType.INT, 0L));
		triples.add(new StringTestTriple("+0", SQLite3DataType.INT, 0L));
		triples.add(new StringTestTriple("+9", SQLite3DataType.INT, 9L));
		triples.add(new StringTestTriple("++9", SQLite3DataType.INT, 0L));
		triples.add(new StringTestTriple("+-9", SQLite3DataType.INT, 0L));
		triples.add(new StringTestTriple("3.0e+5", SQLite3DataType.INT, 300000L));

		triples.add(new StringTestTriple("-3.2", SQLite3DataType.REAL, -3.2d));
		triples.add(new StringTestTriple("10e9", SQLite3DataType.REAL, 10000000000.0));
		triples.add(new StringTestTriple("-0.0", SQLite3DataType.REAL, 0.0d));

		for (StringTestTriple triple : triples) {
			Constant castVal = QueryGenerator.castToNumeric(Constant.createTextConstant(triple.value));
			assertEquals(triple.value.toString(), triple.expectedCastValue, castVal.getValue());
		}
	}

	@Test
	void testBinary() {
		List<StringTestTriple> triples = new ArrayList<>();
		triples.add(new StringTestTriple("112B3980", SQLite3DataType.INT, 0L)); // +9�
		for (StringTestTriple triple : triples) {
			Constant castVal = QueryGenerator
					.castToNumeric(Constant.createBinaryConstant(DatatypeConverter.parseHexBinary(triple.value)));
			assertEquals(triple.value.toString(), triple.expectedCastValue, castVal.getValue());
		}
	}

	private long castLongConstant(long constant) {
		return QueryGenerator.castToNumeric(Constant.createIntConstant(constant)).asInt();
	}

}
