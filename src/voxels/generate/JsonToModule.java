package voxels.generate;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

import com.google.gson.*;
import com.sudoplay.joise.module.*;

public class JsonToModule {
	private static Pattern somethingType = Pattern.compile("^(Fractal|Basis|Interpolation|Block)Type\\.[A-Z]+$");
	private JsonObject source;
	private Map<String, Module> modules = new HashMap<>();
	private Deque<Map<String, Value>> propogatingVariables = new LinkedList<Map<String, Value>>();
	private Deque<Map<String, Value>> localonlyVariables = new LinkedList<Map<String, Value>>();
	
	private class Value {
		public final Class<?> type;
		public final Object value;
		
		public Value(JsonElement arg) {
			this(guessType(arg), arg);
		}
		
		public Value(Class<?> type, JsonElement data) {
			this.type = type;
			this.value = getValue(data, type);
		}
		
		public Value(Class<?> type, Object value) {
			this.type = type;
			this.value = value;
		}
		
		public String toString() {
			return "["+type.getTypeName()+": "+value.toString()+"]";
		}
	}
	
	private class Args {
		public final Class<?>[] types;
		public final Object[] values;
		
		public Args(Class<?>[] types, Object[] values) {
			this.types = types;
			this.values = values;
		}

		public Args(Value arg) {
			this.types = new Class[]{arg.type};
			this.values = new Object[]{arg.value};
		}
	}
	
	public JsonToModule(Reader rawSource, long seed) {
		source = new JsonParser().parse(rawSource).getAsJsonObject();
		Map<String, Value> globals = new HashMap<>();
		globals.put("seed", new Value(long.class, seed));
		propogatingVariables.addFirst(globals);
	}

	@SuppressWarnings("unchecked")
	private Module parseModule(JsonObject data) {
		propogatingVariables.addFirst(new HashMap<String, Value>());
		String name = data.get("%module").getAsString();
		if(name.startsWith("@")) {
			Map<String, Value> newLocals = new HashMap<>();
			for(Map.Entry<String, JsonElement> e: data.entrySet()) {
				if(e.getKey().startsWith("#") || e.getKey().startsWith("%")) continue;
				if(e.getKey().startsWith("$")) {
					propogatingVariables.getFirst().put(e.getKey().substring(1), parseArg(e.getValue()));
				} else {
					newLocals.put(e.getKey(), parseArg(e.getValue()));
				}
			}
			localonlyVariables.addFirst(newLocals);
			Module m = parseModule(source.getAsJsonObject(name));
			localonlyVariables.removeFirst();
			return m;
		}
		JsonArray argsData = data.getAsJsonArray("%args"); // might be null?
		Module module = null;
		try {
			Class<? extends Module> moduleClass = (Class<? extends Module>) Class.forName("com.sudoplay.joise.module.Module"+name);
			if(argsData == null) {
				module = moduleClass.newInstance(); // no args
			} else {
				Args args = parseArgs(argsData);
				Constructor<? extends Module> moduleConstructor = moduleClass.getConstructor(args.types);
				module = moduleConstructor.newInstance(args.values);
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Module"+name + " not found", e);
		} catch (InstantiationException | InvocationTargetException e) {
			throw new RuntimeException("Module"+name + " cannot be instantized", e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Bad arguments for Module"+name + " constructor: " + argsData, e);
		} catch (IllegalAccessException | SecurityException e) {
			throw new RuntimeException(e);
		}
		List<Tuple<Method, Object[]>> commands = new ArrayList<>();
		int endOfSet = 0;
		for(Map.Entry<String, JsonElement> e: data.entrySet()) {
			String command = e.getKey();
			if(command.startsWith("#") || command.startsWith("%")) continue;
			if(command.startsWith("$")) {
				propogatingVariables.getFirst().put(e.getKey().substring(1), parseArg(e.getValue()));
				continue;
			}
			boolean isCmd = command.startsWith(">");
			if(!isCmd) {
				command = "set" + command.substring(0, 1).toUpperCase() + command.substring(1);
			} else {
				command = command.substring(1);
			}
			Args args = parseArgs(e.getValue());
			Method m;
			try {
				m = module.getClass().getMethod(command, args.types);
			} catch (NoSuchMethodException ex) {
				for(int i = 0; i < args.types.length; i++) {
					if(args.types[i].equals(double.class)) {
						args.types[i] = long.class; // TODO: make it work for mixed double / long
						args.values[i] = (long)((double)args.values[i]); // TODO: actually reparse the data so we don't lose precision
					}
				}
				try {
					m = module.getClass().getMethod(command, args.types);
				} catch(NoSuchMethodException ex2) {
					throw new RuntimeException(command+" on "+module.getClass().getName()+" with "+Arrays.toString(args.types)+" does not exist", ex2);
				}
			} catch(SecurityException ex) {
				throw new RuntimeException(ex);
			}
			if(!isCmd) {
				commands.add(new Tuple<Method, Object[]>(m, args.values));
			} else {
				commands.add(0, new Tuple<Method, Object[]>(m, args.values));
			}
		}
		for(Tuple<Method, Object[]> t: commands) {
			try {
				t.o1.invoke(module, t.o2);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
				throw new RuntimeException(e1);
			}
		}
		propogatingVariables.removeFirst();
		return module;
	}
	
	private class Tuple<T1, T2> {
		public final T1 o1;
		public final T2 o2;
		private Tuple(T1 o1, T2 o2) {
			this.o1 = o1;
			this.o2 = o2;
		}
	}
	
	private static Class<?> guessType(JsonElement e) {
		if(e.isJsonObject()) {
			return Module.class;
		} else {
			JsonPrimitive arg = e.getAsJsonPrimitive();
			if(arg.isBoolean()) return boolean.class;
			if(arg.isNumber()) return double.class; //TODO: is it a long or a double?
			String a = arg.getAsString();
			Matcher m = somethingType.matcher(a);
			if(m.matches()) {
				String tname = null;
				switch(m.group(1)) {
					case "Fractal": tname = "com.sudoplay.joise.module.ModuleFractal$"; break;
					case "Interpolation":
					case "Basis": tname = "com.sudoplay.joise.module.ModuleBasisFunction$"; break;
					case "Block": return double.class;
				}
				tname += m.group(1)+"Type";
				try {
					return Class.forName(tname);
				} catch (ClassNotFoundException ex) {
					throw new RuntimeException("Type "+tname+" not found", ex);
				}
			}
			return Module.class;
		}
	}
	
	private Object getValue(JsonElement arg, Class<?> type) {
		if(type.equals(boolean.class)) return arg.getAsBoolean();
		if(type.equals(long.class)) return arg.getAsLong();
		if(type.equals(double.class)) {
			if(arg.getAsJsonPrimitive().isNumber()) return arg.getAsDouble();
			 return voxels.block.BlockType.valueOf(arg.getAsString().split("\\.")[1]).dataValue;
		}
		if(type.equals(Module.class)) {
			if(arg.isJsonObject()) return parseModule(arg.getAsJsonObject());
			return getModule(arg.getAsString());
		}
		if(type.equals(ModuleFractal.FractalType.class)) return ModuleFractal.FractalType.valueOf(arg.getAsString().split("\\.")[1]);
		if(type.equals(ModuleBasisFunction.BasisType.class)) return ModuleBasisFunction.BasisType.valueOf(arg.getAsString().split("\\.")[1]);
		if(type.equals(ModuleBasisFunction.InterpolationType.class)) return ModuleBasisFunction.InterpolationType.valueOf(arg.getAsString().split("\\.")[1]);
		throw new IllegalArgumentException("I don't know how to deal with a "+type.getCanonicalName());
	}
	
	private Value parseArg(JsonElement arg) {
		if(arg.isJsonPrimitive() && arg.getAsJsonPrimitive().isString() && arg.getAsString().startsWith("$")) {
			String varName = arg.getAsString().substring(1);
			if(varName.startsWith("(") && varName.endsWith(")")) {
				return parseMath(varName.substring(1, varName.length() - 1));
			}
			return getVariable(varName);
		}
		return new Value(arg);
	}

	private static final Pattern parensRegex = Pattern.compile("\\(([^)]*)\\)");
	private static final Pattern expRegex = Pattern.compile("(?:^|[\\s+\\-*\\/^])([^\\s+\\-*\\/^]+)\\s*(\\^)\\s*([^\\s+\\-*\\/^]+)(?:$|[\\s+\\-*\\/^])");
	private static final Pattern multDivRegex = Pattern.compile("(?:^|[\\s+\\-*\\/^])([^\\s+\\-*\\/^]+)\\s*([*\\/])\\s*([^\\s+\\-*\\/^]+)(?:$|[\\s+\\-*\\/^])");
	private static final Pattern plusMinusRegex = Pattern.compile("(?:^|[\\s+\\-*\\/^])([^\\s+\\-*\\/^]+)\\s*([+\\-])\\s*([^\\s+\\-*\\/^]+)(?:$|[\\s+\\-*\\/^])");
	private Value parseMath(String expr) {
		expr = expr.trim();
		for(Matcher parens = parensRegex.matcher(expr); parens.matches(); parens = parensRegex.matcher(expr)) {
			Value inner = parseMath(parens.group(1));
			expr = expr.substring(0, parens.start()) + inner.value + expr.substring(parens.end());
		}
		for(Matcher exp = expRegex.matcher(expr); exp.matches(); exp = expRegex.matcher(expr)) {
			Value base = parseMath(exp.group(1));
			Value exponent = parseMath(exp.group(3));
			double result = Math.pow(((Number)base.value).doubleValue(), ((Number)exponent.value).doubleValue());
			expr = expr.substring(0, exp.start()) + result + expr.substring(exp.end());
		}
		for(Matcher md = multDivRegex.matcher(expr); md.matches(); md = multDivRegex.matcher(expr)) {
			Value base = parseMath(md.group(1));
			Value exponent = parseMath(md.group(3));
			double result;
			if(md.group(2).equals("*")) {
				result = ((Number)base.value).doubleValue() * ((Number)exponent.value).doubleValue();
			} else {
				result = ((Number)base.value).doubleValue() / ((Number)exponent.value).doubleValue();
			}
			expr = expr.substring(0, md.start()) + result + expr.substring(md.end());
		}
		for(Matcher pm = plusMinusRegex.matcher(expr); pm.matches(); pm = plusMinusRegex.matcher(expr)) {
			Value base = parseMath(pm.group(1));
			Value exponent = parseMath(pm.group(3));
			double result;
			if(pm.group(2).equals("+")) {
				result = ((Number)base.value).doubleValue() + ((Number)exponent.value).doubleValue();
			} else {
				result = ((Number)base.value).doubleValue() - ((Number)exponent.value).doubleValue();
			}
			expr = expr.substring(0, pm.start()) + result + expr.substring(pm.end());
		}
		if(expr.startsWith("$")) {
			return getVariable(expr.substring(1));
		}
		return new Value(double.class, Double.parseDouble(expr));
	}

	private Value getVariable(String varName) {
		if(!localonlyVariables.isEmpty() && localonlyVariables.getFirst().containsKey(varName)) {
			return localonlyVariables.getFirst().get(varName);
		}
		for(Map<String, Value> scope: propogatingVariables) {
			if(scope.containsKey(varName)) return scope.get(varName);
		}
		System.out.println(localonlyVariables);
		System.out.println(propogatingVariables);
		throw new IllegalArgumentException("A variable by the name of "+varName+" does not appear to exist in this context");
	}

	private Args parseArgs(JsonElement argsData) {
		if(!argsData.isJsonArray()) {
			return new Args(parseArg(argsData));
		}
		JsonArray argsArray = argsData.getAsJsonArray();
		Class<?>[] argsTypes = new Class[argsArray.size()];
		Object[] argsValues = new Object[argsArray.size()];
		for(int i = 0; i < argsTypes.length; i++) {
			Value arg = parseArg(argsArray.get(i));
			argsTypes[i] = arg.type;
			argsValues[i] = arg.value;
		}
		return new Args(argsTypes, argsValues);
	}

	public Module getModule(String name) {
		if(modules.containsKey(name)) return modules.get(name);
		if(source.get(name) != null) return parseModule(source.getAsJsonObject(name));
		throw new IllegalArgumentException("I've never heard of a module called "+name);
	}
}
