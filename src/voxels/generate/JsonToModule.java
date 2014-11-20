package voxels.generate;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

import com.google.gson.*;

import voxels.util.*;

import com.sudoplay.joise.module.*;

public class JsonToModule {
	private static Pattern somethingType = Pattern.compile("^(Fractal|Basis|Interpolation|Block)Type\\.[A-Z]+$");
	private JsonObject source;
	private Map<String, Module> modules = new HashMap<>();
	private long seed;
	
	public JsonToModule(Reader rawSource, long seed) {
		source = new JsonParser().parse(rawSource).getAsJsonObject();
		this.seed = seed;
	}
	
	public void parse() {
		for(Map.Entry<String, JsonElement> module: source.entrySet()) {
			modules.put(module.getKey(), parseModule(module.getValue().getAsJsonObject()));
		}
	}

	@SuppressWarnings("unchecked")
	private Module parseModule(JsonObject data) {
		String name = data.get("$module").getAsString();
		JsonArray argsData = data.getAsJsonArray("$args"); // might be null?
		Module module = null;
		try {
			Class<? extends Module> moduleClass = (Class<? extends Module>) Class.forName("com.sudoplay.joise.module.Module"+name);
			if(argsData == null) {
				module = moduleClass.newInstance(); // no args
			} else {
				Class<?>[] argsTypes = parseArgs(argsData);
				Constructor<? extends Module> moduleConstructor = moduleClass.getConstructor(argsTypes);
				Object[] args = getArgs(argsData, argsTypes);
				module = moduleConstructor.newInstance(args);
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
			if(command.startsWith("$") || command.startsWith("#")) continue;
			boolean isSet = !command.startsWith("%");
			if(isSet) {
				command = "set" + command.substring(0, 1).toUpperCase() + command.substring(1);
			} else {
				command = command.substring(1);
			}
			Class<?>[] argsTypes;
			if(e.getValue().isJsonArray()) {
				argsTypes = parseArgs(e.getValue().getAsJsonArray());
			} else {
				argsTypes = new Class[]{parseArg(e.getValue())};
			}
			Method m;
			try {
				m = module.getClass().getMethod(command, argsTypes);
			} catch (NoSuchMethodException ex) {
				for(int i = 0; i < argsTypes.length; i++) {
					if(argsTypes[i].equals(double.class)) argsTypes[i] = long.class; // TODO: make it work for mixed double / long
				}
				try {
					m = module.getClass().getMethod(command, argsTypes);
				} catch(NoSuchMethodException ex2) {
					throw new RuntimeException(command+" on "+module.getClass().getName()+" with "+Arrays.toString(argsTypes)+" does not exist", ex2);
				}
			} catch(SecurityException ex) {
				throw new RuntimeException(ex);
			}
			Object[] args;
			if(e.getValue().isJsonArray()) {
				args = getArgs(e.getValue().getAsJsonArray(), argsTypes);
			} else {
				args = new Object[]{getArg(e.getValue(), argsTypes[0])};
			}
			if(isSet) {
				commands.add(endOfSet++, new Tuple<Method, Object[]>(m, args));
			}
		}
		for(Tuple<Method, Object[]> t: commands) {
			try {
				t.o1.invoke(module, t.o2);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
				throw new RuntimeException(e1);
			}
		}
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
	
	private Object getArg(JsonElement arg, Class<?> type) {
		if(type.equals(boolean.class)) return arg.getAsBoolean();
		if(type.equals(long.class)) {
			if(arg.getAsJsonPrimitive().isNumber()) return arg.getAsLong();
			return seed;
		}
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

	private Object[] getArgs(JsonArray argsData, Class<?>[] argsTypes) {
		Object[] args = new Object[argsData.size()];
		for(int i = 0; i < args.length; i++) {
			args[i] = getArg(argsData.get(i), argsTypes[i]);
		}
		return args;
	}
	
	private static Class<?> parseArg(JsonElement e) {
		if(e.isJsonObject()) {
			return Module.class;
		} else {
			JsonPrimitive arg = e.getAsJsonPrimitive();
			if(arg.isBoolean()) return boolean.class;
			if(arg.isNumber()) return double.class; //TODO: is it a long or a double?
			String a = arg.getAsString();
			if(a.equals("$seed")) return long.class;
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

	private static Class<?>[] parseArgs(JsonArray argsData) {
		Class<?>[] argsTypes = new Class[argsData.size()];
		for(int i = 0; i < argsTypes.length; i++) {
			argsTypes[i] = parseArg(argsData.get(i));
		}
		return argsTypes;
	}

	public Module getModule(String name) {
		if(modules.containsKey(name)) return modules.get(name);
		if(source.get(name) != null) return parseModule(source.getAsJsonObject(name));
		throw new IllegalArgumentException("I've never heard of a module called "+name);
	}
}
