package voxels.generate;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

import com.google.gson.*;
import com.sudoplay.joise.module.*;

public class JsonToModule {
	static Pattern somethingType = Pattern.compile("^(Fractal|Basis|Interpolation|Block)Type\\.[A-Z]+$");
	private JsonObject source;
	private Map<String, Module> modules = new HashMap<>();
	private Deque<Map<String, Value>> propogatingVariables = new LinkedList<Map<String, Value>>();
	private Deque<Map<String, Value>> localonlyVariables = new LinkedList<Map<String, Value>>();
	
	public JsonToModule(Reader rawSource, long seed) {
		source = new JsonParser().parse(rawSource).getAsJsonObject();
		Map<String, Value> globals = new HashMap<>();
		globals.put("seed", new Value(long.class, seed));
		propogatingVariables.addFirst(globals);
	}

	@SuppressWarnings("unchecked") Module parseModule(JsonObject data) {
		propogatingVariables.addFirst(new HashMap<String, Value>());
		String name = data.get("%module").getAsString();
		if(name.startsWith("@")) {
			Map<String, Value> newLocals = new HashMap<>();
			for(Map.Entry<String, JsonElement> e: data.entrySet()) {
				if(e.getKey().startsWith("#") || e.getKey().startsWith("%")) continue;
				if(e.getKey().startsWith("$")) {
					propogatingVariables.getFirst().put(e.getKey().substring(1), Value.parseValue(e.getValue(), this));
				} else {
					newLocals.put(e.getKey(), Value.parseValue(e.getValue(), this));
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
		for(Map.Entry<String, JsonElement> e: data.entrySet()) {
			String command = e.getKey();
			if(command.startsWith("#") || command.startsWith("%")) continue;
			if(command.startsWith("$")) {
				propogatingVariables.getFirst().put(e.getKey().substring(1), Value.parseValue(e.getValue(), this));
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
				for(int i = 0; i < args.types.length; i++) { //TODO: don't use such an outrageous hack
					if(args.types[i].equals(double.class)) {
						args.types[i] = long.class; // TODO: make it work for mixed double / long
						args.values[i] = (long)((double)args.values[i]); // TODO: actually re-parse the data so we don't lose precision
					}
					if(args.types[i].equals(Module.class)) {
						args.types[i] = ModuleCellGen.class;
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
	
	Value getVariable(String varName) {
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
			return new Args(Value.parseValue(argsData, this));
		}
		JsonArray argsArray = argsData.getAsJsonArray();
		Class<?>[] argsTypes = new Class[argsArray.size()];
		Object[] argsValues = new Object[argsArray.size()];
		for(int i = 0; i < argsTypes.length; i++) {
			Value arg = Value.parseValue(argsArray.get(i), this);
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
