package voxels.generate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.sudoplay.joise.module.Module;
import com.sudoplay.joise.module.ModuleBasisFunction;
import com.sudoplay.joise.module.ModuleFractal;

class Value {
	public final Class<?> type;
	public final Object value;
	
	public Value(JsonElement data, JsonToModule jtm) {
		this.type = guessType(data);
		this.value = getValue(data, type, jtm);
	}
	
	public Value(Class<?> type, Object value) {
		this.type = type;
		this.value = value;
	}
	
	public String toString() {
		return "["+type.getTypeName()+": "+value.toString()+"]";
	}
	
	private static Class<?> guessType(JsonElement e) {
		if(e.isJsonObject()) {
			return Module.class;
		} else {
			JsonPrimitive arg = e.getAsJsonPrimitive();
			if(arg.isBoolean()) return boolean.class;
			if(arg.isNumber()) return double.class; //TODO: is it a long or a double?
			String a = arg.getAsString();
			Matcher m = JsonToModule.somethingType.matcher(a);
			if(m.matches()) {
				String typeName = null;
				switch(m.group(1)) {
					case "Fractal": typeName = "com.sudoplay.joise.module.ModuleFractal$"; break;
					case "Interpolation":
					case "Basis": typeName = "com.sudoplay.joise.module.ModuleBasisFunction$"; break;
					case "Block": return double.class;
				}
				typeName += m.group(1)+"Type";
				try {
					return Class.forName(typeName);
				} catch (ClassNotFoundException ex) {
					throw new RuntimeException("Type "+typeName+" not found", ex);
				}
			}
			return Module.class;
		}
	}
	
	private Object getValue(JsonElement arg, Class<?> type, JsonToModule jtm) {
		if(type.equals(boolean.class)) return arg.getAsBoolean();
		if(type.equals(long.class)) return arg.getAsLong();
		if(type.equals(double.class)) {
			if(arg.getAsJsonPrimitive().isNumber()) return arg.getAsDouble();
			 return voxels.block.BlockType.valueOf(arg.getAsString().split("\\.")[1]).dataValue;
		}
		if(Module.class.isAssignableFrom(type)) {
			if(arg.isJsonObject()) return jtm.parseModule(arg.getAsJsonObject());
			return jtm.getModule(arg.getAsString());
		}
		if(type.equals(ModuleFractal.FractalType.class)) return ModuleFractal.FractalType.valueOf(arg.getAsString().split("\\.")[1]);
		if(type.equals(ModuleBasisFunction.BasisType.class)) return ModuleBasisFunction.BasisType.valueOf(arg.getAsString().split("\\.")[1]);
		if(type.equals(ModuleBasisFunction.InterpolationType.class)) return ModuleBasisFunction.InterpolationType.valueOf(arg.getAsString().split("\\.")[1]);
		throw new IllegalArgumentException("I don't know how to deal with a "+type.getCanonicalName());
	}
	
	private static final Pattern parenthesesRegex = Pattern.compile("\\(([^)]*)\\)");
	private static final Pattern exponentRegex = Pattern.compile("(?:^|[\\s+\\-*\\/^])([^\\s+\\-*\\/^]+)\\s*(\\^)\\s*([^\\s+\\-*\\/^]+)(?:$|[\\s+\\-*\\/^])");
	private static final Pattern multiplyDivideRegex = Pattern.compile("(?:^|[\\s+\\-*\\/^])([^\\s+\\-*\\/^]+)\\s*([*\\/])\\s*([^\\s+\\-*\\/^]+)(?:$|[\\s+\\-*\\/^])");
	private static final Pattern plusMinusRegex = Pattern.compile("(?:^|[\\s+\\-*\\/^])([^\\s+\\-*\\/^]+)\\s*([+\\-])\\s*([^\\s+\\-*\\/^]+)(?:$|[\\s+\\-*\\/^])");
	private static Value parseMath(String expr, JsonToModule jtm) {
		expr = expr.trim();
		for(Matcher parens = parenthesesRegex.matcher(expr); parens.matches(); parens = parenthesesRegex.matcher(expr)) {
			Value inner = parseMath(parens.group(1), jtm);
			expr = expr.substring(0, parens.start()) + inner.value + expr.substring(parens.end());
		}
		for(Matcher exp = exponentRegex.matcher(expr); exp.matches(); exp = exponentRegex.matcher(expr)) {
			Value base = parseMath(exp.group(1), jtm);
			Value exponent = parseMath(exp.group(3), jtm);
			double result = Math.pow(((Number)base.value).doubleValue(), ((Number)exponent.value).doubleValue());
			expr = expr.substring(0, exp.start()) + result + expr.substring(exp.end());
		}
		for(Matcher md = multiplyDivideRegex.matcher(expr); md.matches(); md = multiplyDivideRegex.matcher(expr)) {
			Value base = parseMath(md.group(1), jtm);
			Value exponent = parseMath(md.group(3), jtm);
			double result;
			if(md.group(2).equals("*")) {
				result = ((Number)base.value).doubleValue() * ((Number)exponent.value).doubleValue();
			} else {
				result = ((Number)base.value).doubleValue() / ((Number)exponent.value).doubleValue();
			}
			expr = expr.substring(0, md.start()) + result + expr.substring(md.end());
		}
		for(Matcher pm = plusMinusRegex.matcher(expr); pm.matches(); pm = plusMinusRegex.matcher(expr)) {
			Value base = parseMath(pm.group(1), jtm);
			Value exponent = parseMath(pm.group(3), jtm);
			double result;
			if(pm.group(2).equals("+")) {
				result = ((Number)base.value).doubleValue() + ((Number)exponent.value).doubleValue();
			} else {
				result = ((Number)base.value).doubleValue() - ((Number)exponent.value).doubleValue();
			}
			expr = expr.substring(0, pm.start()) + result + expr.substring(pm.end());
		}
		if(expr.startsWith("$")) {
			return jtm.getVariable(expr.substring(1));
		}
		return new Value(double.class, Double.parseDouble(expr));
	}
	
	public static Value parseValue(JsonElement data, JsonToModule jtm) {
		if(data.isJsonPrimitive() && data.getAsJsonPrimitive().isString() && data.getAsString().startsWith("$")) {
			String varName = data.getAsString().substring(1);
			if(varName.startsWith("(") && varName.endsWith(")")) {
				return parseMath(varName.substring(1, varName.length() - 1), jtm);
			}
			return jtm.getVariable(varName);
		}
		return new Value(data, jtm);
	}
}